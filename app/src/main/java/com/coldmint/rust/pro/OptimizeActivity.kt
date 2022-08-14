package com.coldmint.rust.pro

import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.core.tool.FileSignatureCache
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.interfaces.LineParserEvent
import android.text.SpannableString
import android.text.style.ClickableSpan
import com.afollestad.materialdialogs.MaterialDialog
import android.text.Spanned
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.adapters.OptimizeAdapter
import com.coldmint.rust.pro.databean.OptimizeGroup
import com.coldmint.rust.pro.databean.OptimizeItem
import com.coldmint.rust.pro.databinding.ActivityOptimizeBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import java.io.File
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.HashMap

class OptimizeActivity : BaseActivity<ActivityOptimizeBinding>() {
    private var mFolder: File? = null
    private val mOptimizeGroups: MutableList<OptimizeGroup> = ArrayList()
    private val mOptimizeItem: MutableList<List<OptimizeItem<*>>> = ArrayList()

    //优化组名称映射
    private val mGroupNameMap = HashMap<String, OptimizeGroup>()
    private val mItemNameMap = HashMap<String, MutableList<OptimizeItem<*>>>()
    private var noteNum = 0
    private var emptyLineNum = 0
    private var firstNoEmptyLine = true
    private var mOptimizeAdapter: OptimizeAdapter? = null
    private var work = false
    private var hasError = false
    private lateinit var mFileSignatureCache: FileSignatureCache

    /**
     * 处理优化内容
     */
    private fun processingOptimizationContent() {
        if (work) {
            return
        }
        Thread {
            if (mOptimizeGroups.size > 0) {
                work = true
                runOnUiThread {
                    viewBinding.optimizationButton.setBackgroundColor(
                        GlobalMethod.getThemeColor(
                            this,
                            R.attr.colorPrimaryVariant
                        )
                    )
                    viewBinding.optimizationButton.text = getString(R.string.optimizationing)
                }
                val optimizeGroupArray = mOptimizeGroups.toTypedArray()
                for (group in optimizeGroupArray) {
                    val groupName = group.groupName
                    if (group.isEnabled) {
                        val optimizeItemList = mItemNameMap[groupName]!!
                        val optimizeItemArray = optimizeItemList.toTypedArray()
                        for (item in optimizeItemArray) {
                            if (item.isEnabled) {
                                //如果子级被启用
                                if (groupName == getString(R.string.empty_file) || groupName == getString(
                                        R.string.empty_folder
                                    ) || groupName == getString(R.string.backup_file)
                                ) {
                                    val file = item.`object` as File
                                    file.delete()
                                    val folder = file.parentFile
                                    if (folder.list().size == 0) {
                                        folder.delete()
                                    }
                                } else if (groupName == getString(R.string.empty_line)) {
                                    firstNoEmptyLine = true
                                    val file = item.`object` as File
                                    val text = FileOperator.readFile(file)
                                    if (text != null) {
                                        val newText = StringBuilder()
                                        val lineParser = LineParser(text)
                                        lineParser.analyse(object : LineParserEvent {
                                            override fun processingData(
                                                lineNum: Int,
                                                lineData: String,
                                                isEnd: Boolean
                                            ): Boolean {
                                                if (!lineData.isEmpty()) {
                                                    if (firstNoEmptyLine) {
                                                        firstNoEmptyLine = false
                                                    } else {
                                                        newText.append('\n')
                                                    }
                                                    newText.append(lineData)
                                                }
                                                return true
                                            }

                                        })
                                        FileOperator.writeFile(file, newText.toString())
                                    }
                                } else if (groupName == getString(R.string.note)) {
                                    firstNoEmptyLine = true
                                    val file = item.`object` as File
                                    val text = FileOperator.readFile(file)
                                    if (text != null) {
                                        val newText = StringBuilder()
                                        val lineParser = LineParser(text)
                                        lineParser.analyse(object : LineParserEvent {
                                            override fun processingData(
                                                lineNum: Int,
                                                lineData: String,
                                                isEnd: Boolean
                                            ): Boolean {
                                                if (!lineData.startsWith("#")) {
                                                    if (firstNoEmptyLine) {
                                                        firstNoEmptyLine = false
                                                    } else {
                                                        newText.append('\n')
                                                    }
                                                    newText.append(lineData)
                                                }
                                                return true
                                            }

                                        })
                                        FileOperator.writeFile(file, newText.toString())
                                    }
                                }
                                optimizeItemList.remove(item)
                            }
                        }
                        if (optimizeItemList.size == 0) {
                            mOptimizeGroups.remove(group)
                        }
                    }
                }
                runOnUiThread {
                    if (mOptimizeGroups.size > 0) {
                        mOptimizeAdapter!!.notifyDataSetChanged()
                        viewBinding.optimizationButton.setBackgroundColor(
                            GlobalMethod.getColorPrimary(
                                this
                            )
                        )
                        viewBinding.optimizationButton.text = getString(R.string.optimization)
                    } else {
                        viewBinding.optimizationList.isVisible = false
                        viewBinding.progressBar.isVisible = false
                        viewBinding.optimizationButton.isVisible = false
                        viewBinding.textView.isVisible = true
                        viewBinding.textView.text = getString(R.string.no_optimized_content)
                    }
                }
                work = false
            }
        }.start()
    }

    /**
     * 分析文件（处理单个文件）
     *
     * @param file
     */
    private fun analyzeFile(file: File) {
        if (mFileSignatureCache!!.isChange(file)) {
            hasError = false
            val type = FileOperator.getFileType(file)
            when (type) {
                "ini", "txt" -> {
                    val content = FileOperator.readFile(file)
                    if (content == null || content.isEmpty()) {
                        //文件为空
                        val group = mGroupNameMap[getString(R.string.empty_file)]
                        if (group != null) {
                            val optimizeItem: OptimizeItem<*> = OptimizeItem(
                                file.name,
                                group,
                                file
                            )
                            optimizeItem.description = SpannableString(file.absolutePath)
                            addItem(getString(R.string.empty_file), optimizeItem)
                        }
                    } else {
                        noteNum = 0
                        emptyLineNum = 0
                        val lineParser = LineParser(content)
                        val location = getString(R.string.location_info)
                        val name = file.name
                        val isEmptyLine = getString(R.string.is_empty_line)
                        val lineBuilder = StringBuilder()
                        val noteBuilder = StringBuilder()
                        lineParser.analyse(object : LineParserEvent {
                            override fun processingData(
                                lineNum: Int,
                                lineData: String,
                                isEnd: Boolean
                            ): Boolean {
                                if (lineData.isEmpty()) {
                                    //行数据为空
                                    lineBuilder.append(String.format(location, name, lineNum + 1))
                                    lineBuilder.append(isEmptyLine)
                                    lineBuilder.append("\n\n")
                                    emptyLineNum++
                                } else if (lineData.startsWith("#")) {
                                    noteBuilder.append(String.format(location, name, lineNum + 1))
                                    noteBuilder.append(lineData)
                                    noteBuilder.append("\n\n")
                                    noteNum++
                                }
                                return true
                            }

                        })
                        if (!lineBuilder.toString().isEmpty()) {
                            val tip =
                                String.format(getString(R.string.empty_line_prompt), emptyLineNum)
                            val action = getString(R.string.show_details)
                            val group = mGroupNameMap[getString(R.string.empty_line)]
                            if (group != null) {
                                val optimizeItem: OptimizeItem<*> = OptimizeItem(
                                    file.name,
                                    group,
                                    file
                                )
                                val spannableString = SpannableString(tip)
                                spannableString.setSpan(
                                    object : ClickableSpan() {
                                        override fun onClick(widget: View) {
                                            val materialDialog = MaterialDialog(
                                                this@OptimizeActivity,
                                                MaterialDialog.DEFAULT_BEHAVIOR
                                            )
                                            materialDialog.title(R.string.details, null)
                                            materialDialog.message(
                                                null,
                                                lineBuilder.toString(),
                                                null
                                            )
                                            materialDialog.positiveButton(
                                                R.string.close,
                                                null,
                                                null
                                            )
                                            materialDialog.show()
                                        }
                                    },
                                    tip.indexOf(action),
                                    tip.length,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                )
                                optimizeItem.description = spannableString
                                addItem(getString(R.string.empty_line), optimizeItem)
                            }
                        }
                        if (!noteBuilder.toString().isEmpty()) {
                            //note_prompt
                            val tip = String.format(getString(R.string.note_prompt), noteNum)
                            val action = getString(R.string.show_details)
                            val group = mGroupNameMap[getString(R.string.note)]
                            if (group != null) {
                                val optimizeItem: OptimizeItem<*> = OptimizeItem(
                                    file.name,
                                    group,
                                    file
                                )
                                val spannableString = SpannableString(tip)
                                spannableString.setSpan(
                                    object : ClickableSpan() {
                                        override fun onClick(widget: View) {
                                            val materialDialog = MaterialDialog(
                                                this@OptimizeActivity,
                                                MaterialDialog.DEFAULT_BEHAVIOR
                                            )
                                            materialDialog.title(R.string.details, null)
                                            materialDialog.message(
                                                null,
                                                noteBuilder.toString(),
                                                null
                                            )
                                            materialDialog.negativeButton(
                                                R.string.edit,
                                                null
                                            ) { materialDialog: MaterialDialog? ->
                                                val bundle = Bundle()
                                                bundle.putString("path", file.absolutePath)
                                                bundle.putString(
                                                    "modPath",
                                                    FileOperator.getSuperDirectory(file)
                                                )
                                                val intent =
                                                    Intent(
                                                        this@OptimizeActivity,
                                                        EditActivity::class.java
                                                    )
                                                intent.putExtra("data", bundle)
                                                this@OptimizeActivity.startActivity(intent)
                                                null
                                            }
                                            materialDialog.positiveButton(
                                                R.string.close,
                                                null,
                                                null
                                            )
                                            materialDialog.show()
                                        }
                                    },
                                    tip.indexOf(action),
                                    tip.length,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                )
                                optimizeItem.description = spannableString
                                addItem(getString(R.string.note), optimizeItem)
                            }

                        }
                    }
                }
                "bak" -> {
                    //backup_file 备份文件
                    val group = mGroupNameMap[getString(R.string.backup_file)]
                    if (group != null) {
                        val optimizeItem: OptimizeItem<*> = OptimizeItem(
                            file.name,
                            group,
                            file
                        )
                        optimizeItem.description = SpannableString(file.absolutePath)
                        addItem(getString(R.string.backup_file), optimizeItem)
                    }

                }
            }
            if (!hasError) {
                mFileSignatureCache!!.putFile(file)
            }
        }
    }

    /**
     * 分析文件夹（包括子文件）
     *
     * @param file 文件
     */
    private fun analyseFolder(file: File?) {
        if (file!!.isDirectory) {
            val files = file.listFiles()
            if (files.isNotEmpty()) {
                for (f in files) {
                    if (f.isDirectory) {
                        analyseFolder(f)
                    } else {
                        //分析文件
                        analyzeFile(f)
                    }
                }
            } else {
                //文件夹为空
                val group = mGroupNameMap[getString(R.string.empty_folder)]
                if (group != null) {
                    val optimizeItem: OptimizeItem<*> =
                        OptimizeItem(file.name, group, file)
                    optimizeItem.description = SpannableString(file.absolutePath)
                    addItem(getString(R.string.empty_folder), optimizeItem)
                }

            }
        } else {
            //分析文件
            analyzeFile(file)
        }
    }

    /**
     * 添加组
     *
     * @param groupName 组名
     * @return 是否添加成功
     */
    private fun addGroup(groupName: String): Boolean {
        return if (mGroupNameMap.containsKey(groupName)) {
            false
        } else {
            val optimizeGroup = OptimizeGroup(groupName)
            val optimizeItems: MutableList<OptimizeItem<*>> =
                ArrayList()
            mGroupNameMap[groupName] = optimizeGroup
            mOptimizeGroups.add(optimizeGroup)
            mItemNameMap[groupName] = optimizeItems
            true
        }
    }

    /**
     * 添加子项目
     *
     * @param groupName    组名
     * @param optimizeItem 子项目
     * @return 是否添加成功
     */
    private fun addItem(groupName: String, optimizeItem: OptimizeItem<*>): Boolean {
        hasError = true
        return if (mGroupNameMap.containsKey(groupName)) {
            val optimizeItemList = mItemNameMap[groupName]!!
            optimizeItemList.add(optimizeItem)
        } else {
            false
        }
    }

    /**
     * 加载优化内容
     */
    private fun loadOptimizationContent(folder: File?) {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            handler.post {
                viewBinding.optimizationList.isVisible = false
                viewBinding.progressBar.isVisible = true
                viewBinding.textView.isVisible = true
                viewBinding.textView.text = getString(R.string.load_optimized_item)
                viewBinding.optimizationButton.isVisible = false
            }
            mOptimizeGroups.clear()
            mOptimizeItem.clear()
            mGroupNameMap.clear()
            mItemNameMap.clear()
            //添加组
            addGroup(getString(R.string.empty_file))
            addGroup(getString(R.string.empty_folder))
            addGroup(getString(R.string.empty_line))
            addGroup(getString(R.string.note))
            addGroup(getString(R.string.backup_file))
            //分析文件夹
            analyseFolder(folder)
            mFileSignatureCache.save()
            //将子项目添加到父级
            if (mOptimizeGroups.size > 0) {
                val optimizeGroups = mOptimizeGroups.toTypedArray()
                for (group in optimizeGroups) {
                    val optimizeItemList: List<OptimizeItem<*>> = mItemNameMap[group.groupName]!!
                    if (optimizeItemList.size > 0) {
                        mOptimizeItem.add(optimizeItemList)
                    } else {
                        mOptimizeGroups.remove(group)
                    }
                }
                if (mOptimizeGroups.size > 0) {
                    mOptimizeAdapter =
                        OptimizeAdapter(mOptimizeGroups, mOptimizeItem, this@OptimizeActivity)
                    handler.post {
                        viewBinding.optimizationList.isVisible = true
                        viewBinding.progressBar.isVisible = false
                        viewBinding.textView.isVisible = false
                        viewBinding.optimizationList.setAdapter(mOptimizeAdapter)
                        viewBinding.optimizationButton.isVisible = true
                    }
                } else {
                    handler.post {
                        viewBinding.optimizationList.isVisible = false
                        viewBinding.progressBar.isVisible = false
                        viewBinding.optimizationButton.isVisible = false
                        viewBinding.textView.isVisible = true
                        viewBinding.textView.text = getString(R.string.no_optimized_content)
                    }
                }
            }
        }.start()
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityOptimizeBinding {
        return ActivityOptimizeBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setTitle(R.string.optimization)
            setReturnButton()
            val intent = intent
            val bundle = intent.getBundleExtra("data")
            if (bundle == null) {
                Toast.makeText(this, "无效请求", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val modPath = bundle.getString("modPath")
                val file = File(modPath)
                if (file.exists() && file.isDirectory) {
                    val modClass = ModClass(File(modPath))
                    mFileSignatureCache =
                        FileSignatureCache(this@OptimizeActivity, modClass.modName)
                    mFileSignatureCache.setRootFolder(modClass.modFile)
                    mFolder = file
                    loadOptimizationContent(mFolder)
                } else {
                    Toast.makeText(this, "文件不合法，必须是文件夹", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            viewBinding.optimizationButton.setOnClickListener { processingOptimizationContent() }
        }
    }
}