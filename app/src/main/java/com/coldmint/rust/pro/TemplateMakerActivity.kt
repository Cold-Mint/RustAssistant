package com.coldmint.rust.pro


import com.coldmint.rust.pro.base.BaseActivity
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import android.os.Bundle
import android.content.Intent
import org.json.JSONException
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.coldmint.rust.core.TemplatePackage
import com.coldmint.rust.core.interfaces.LineParserEvent
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.adapters.AttachFileAdapter
import com.coldmint.rust.pro.adapters.TemplateMakerAdapter
import com.coldmint.rust.pro.adapters.TemplateMakerPagerAdapter

import com.coldmint.rust.pro.databinding.ActivityTemplateMakerBinding
import com.coldmint.rust.pro.databinding.AttachFilesBinding
import com.coldmint.rust.pro.tool.AppSettings
import org.json.JSONArray
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

//模板制作器
class TemplateMakerActivity : BaseActivity<ActivityTemplateMakerBinding>() {
    private lateinit var mJsonObject: JSONObject
    private lateinit var makerAdapter: TemplateMakerAdapter
    private var mFileName: String? = null
    private lateinit var makerView: RecyclerView
    private val attachFiles = ArrayList<File>()
    private var iconPath: String? = ""
    private lateinit var attachFilesBinding: AttachFilesBinding

    /**
     * 选择文件方法
     * @param path String? 路径
     * @param requestCode Int 请求码
     */
    fun selectFile(path: String?, requestCode: Int) {
        val startIntent =
            Intent(this@TemplateMakerActivity, FileManagerActivity::class.java)
        val fileBundle = Bundle()
        fileBundle.putString("type", "selectFile")
        if (path != null) {
            fileBundle.putString("path", path)
        }
        startIntent.putExtra("data", fileBundle)
        startActivityForResult(startIntent, requestCode)
    }

    fun createTemplateData(text: String) {
        try {
            mJsonObject.put("name", mFileName)
            mJsonObject.put("data", text)
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }

        val codeData: MutableList<CodeData> = ArrayList()
        var section: String? = null
        val lineParser = LineParser(text)
        lineParser.analyse(object : LineParserEvent {
            override fun processingData(
                lineNum: Int,
                lineData: String,
                isEnd: Boolean
            ): Boolean {
                if (lineData.startsWith("[") && lineData.endsWith("]")) {
                    section = lineData.substring(1, lineData.length - 1)
                }
                codeData.add(CodeData(lineData, section))
                return true
            }
        })
        makerAdapter = TemplateMakerAdapter(this, codeData)
    }


    fun loadTemplateData(templateRootPath: String, json: String) {
        val jsonObject = JSONObject(json)
        val text = jsonObject.getString("data")
        try {
            mJsonObject.put("name", jsonObject.getString("name"))
            mJsonObject.put("data", text)
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }

        val codeData: MutableList<CodeData> = ArrayList()
        var section: String? = null
        val lineParser = LineParser(text)
        lineParser.analyse(object : LineParserEvent {
            override fun processingData(
                lineNum: Int,
                lineData: String,
                isEnd: Boolean
            ): Boolean {
                if (lineData.startsWith("[") && lineData.endsWith("]")) {
                    section = lineData.substring(1, lineData.length - 1)
                }
                codeData.add(CodeData(lineData, section))
                return true
            }
        })
        makerAdapter = TemplateMakerAdapter(this, codeData)
        makerAdapter.setActionArray(jsonObject.getJSONArray("action"))
        if (jsonObject.has("icon")) {
            iconPath = templateRootPath + jsonObject.getString("icon")
            val iconFile = File(iconPath)
            if (iconFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(iconPath)
                attachFilesBinding.iconView.setImageBitmap(bitmap)
            }
        }
        if (jsonObject.has("attachFile")) {
            val attachFile = jsonObject.getJSONArray("attachFile")
            val len = attachFile.length() - 1
            if (len > -1) {
                for (i in 0..len) {
                    val string = attachFile.getString(i)
                    attachFiles.add(File(templateRootPath + string))
                }
                attachFilesBinding.filesList.adapter = getFileAdapter()
            }
        }

    }

    fun getFileAdapter(): AttachFileAdapter {
        val fileAdapter = AttachFileAdapter(this, attachFiles)
        fileAdapter.setItemEvent { i, attachFileItemBinding, viewHolder, file ->
            attachFileItemBinding.rootLayout.setOnLongClickListener {
                fileAdapter.showDeleteItemDialog(file.name, viewHolder.adapterPosition)
                true
            }
        }
        return fileAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_template, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val filePath = File(data?.getStringExtra("File"))
                if (attachFiles.contains(filePath)) {
                    Snackbar.make(
                        viewBinding.viewPager,
                        R.string.add_attach_error,
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                } else {
                    attachFiles.add(filePath)
                    val fileAdapter = getFileAdapter()
                    attachFilesBinding.filesList.adapter = fileAdapter
                }
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                val filePath = data!!.getStringExtra("File")
                val type = FileOperator.getFileType(File(filePath))
                if (type == "png" || type == "jpg") {
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    attachFilesBinding.iconView.setImageBitmap(bitmap)
                    iconPath = filePath
                } else {
                    Snackbar.make(
                        viewBinding.viewPager,
                        R.string.add_icon_error,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * 添加模板到模板库
     */
    fun addTemplate(
        templateRootPath: String,
        environmentLanguage: String,
        handler: Handler
    ): Boolean {
        try {
            val templateFolder = "$templateRootPath/$mFileName/"
            val resourcePath = templateFolder + "resource/"
            mJsonObject.put("language", environmentLanguage)
            val resoureceFile = File(resourcePath)
            if (!resoureceFile.exists()) {
                resoureceFile.mkdirs()
            }
            if (iconPath!!.isNotEmpty()) {
                val templateIcon = templateFolder + "icon.png"
                FileOperator.copyFile(
                    File(iconPath),
                    File(templateIcon)
                )
                val relativePath = FileOperator.getRelativePath(
                    templateIcon,
                    templateRootPath
                )
                if (relativePath != null) {
                    mJsonObject.put("icon", relativePath)
                }
            }

            //拷贝附加文件
            if (attachFiles.size > 0) {
                val attachFileArray = JSONArray()
                for (path in attachFiles) {
                    val sourceFile = path
                    val newFile = File(resourcePath + sourceFile.name)
                    val md5 = FileOperator.getMD5(newFile)
                    val newMd5 = FileOperator.getMD5(sourceFile)
                    if (md5 != newMd5) {
                        newFile.delete()
                        if (!FileOperator.copyFile(sourceFile, newFile)) {
                            handler.post {
                                Snackbar.make(
                                    viewBinding.viewPager,
                                    R.string.copy_file_error,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    attachFileArray.put(
                        FileOperator.getRelativePath(
                            newFile,
                            File(templateRootPath)
                        )
                    )
                }
                mJsonObject.put("attachFile", attachFileArray)
            }
            mJsonObject.put("action", makerAdapter.getActionArray())
            val f = File("$templateFolder$mFileName.json")
            FileOperator.writeFile(f, mJsonObject.toString(4))
            return true
        } catch (exception: JSONException) {
            exception.printStackTrace()
            return false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_action -> {
                val arrayList = ArrayList<String>()
                val pathList = ArrayList<String>()
                val environmentLanguage =
                    appSettings.getValue(
                        AppSettings.Setting.AppLanguage,
                        Locale.getDefault().language
                    )
                val templateDirectory = appSettings.getValue(
                    AppSettings.Setting.TemplateDirectory,
                    this.filesDir.absolutePath + "/template/"
                )
                val directory = File(templateDirectory)
                if (directory.exists() && directory.isDirectory) {
                    val files = directory.listFiles()
                    if (files.isNotEmpty()) {
                        for (file in files) {
                            val templateClass = TemplatePackage(file)
                            if (templateClass.isTemplate) {
                                arrayList.add(
                                    templateClass.getName()
                                )
                                pathList.add(file.absolutePath)
                            }
                        }
                    }
                }
                val materialDialog =
                    MaterialDialog(this, BottomSheet()).title(R.string.select_template)
                        .positiveButton(R.string.edit_function)
                        .negativeButton(R.string.dialog_cancel)
                materialDialog.listItemsMultiChoice(items = arrayList) { dialog, indices, items ->
                    val handler: Handler = Handler(Looper.getMainLooper())
                    Thread {
                        for (item: CharSequence in items) {
                            val index = arrayList.indexOf(item)
                            addTemplate(pathList[index], environmentLanguage, handler)
                        }
                        handler.post {
                            materialDialog.dismiss()
                            MaterialDialog(this).show {
                                title(R.string.template_title).message(R.string.template_save_complete)
                                    .positiveButton(R.string.dialog_ok)
                                    .negativeButton(R.string.dialog_cancel).cancelable(false)
                                    .positiveButton { finish() }
                            }
                        }
                    }.start()

                }
                materialDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //命令内部类
    class CodeData(val code: String, val section: String?)


    override fun getViewBindingObject(): ActivityTemplateMakerBinding {
        return ActivityTemplateMakerBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setSupportActionBar(viewBinding.toolbar)
            setReturnButton()
            viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)
            makerView = RecyclerView(this@TemplateMakerActivity)
            mJsonObject = JSONObject()
            val intent = intent
            val bundle = intent.getBundleExtra("data")
            if (bundle == null) {
                showError("未知的bundle")
                return
            } else {
                val path = bundle.getString("path")
                if (path == null) {
                    showError("未知的Path")
                    return
                }
                val loadTemplate: Boolean = bundle.getBoolean("loadTemplate", false)
                val file = File(path)
                if (!file.exists()) {
                    showError(getString(R.string.file_not_exist))
                    return
                }
                attachFilesBinding = AttachFilesBinding.inflate(layoutInflater)
                mFileName = FileOperator.getPrefixName(file)
                title = mFileName
                val str = FileOperator.readFile(file)
                if (str == null || str.isBlank()) {
                    showError(getString(R.string.empty_text))
                    return
                }
                if (loadTemplate) {
                    if (bundle.containsKey("templatePath")) {
                        val root = bundle.getString("templatePath")
                        if (root != null) {
                            loadTemplateData(root, str)
                        } else {
                            showError("加载错误，templatePath为空")
                        }
                    } else {
                        showError("加载错误，未设置templatePath")
                    }
                } else {
                    createTemplateData(str)
                }
                //添加
                val layoutManager = LinearLayoutManager(this@TemplateMakerActivity)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                makerView.layoutManager = layoutManager
                makerView.adapter = makerAdapter
                val attachFileView = attachFilesBinding.root
                val layoutManager2 = LinearLayoutManager(this@TemplateMakerActivity)
                attachFilesBinding.filesList.layoutManager = layoutManager2
                attachFilesBinding.addFileButton.setOnClickListener(View.OnClickListener
                { //选择文件
                    var thisPath: String = FileOperator.getSuperDirectory(file)
                    if (thisPath.startsWith(this.filesDir.absolutePath)) {
                        selectFile(null, 1)
                    } else {
                        selectFile(thisPath, 1)
                    }
                })
                attachFilesBinding.iconView.setOnClickListener(View.OnClickListener
                { //选择文件
                    var thisPath = FileOperator.getSuperDirectory(file)
                    if (thisPath.startsWith(this.filesDir.absolutePath)) {
                        selectFile(null, 2)
                    } else {
                        selectFile(thisPath, 2)
                    }
                })
                val titles = intArrayOf(R.string.action, R.string.attach)
                val views = arrayOf(makerView, attachFileView)
                viewBinding.viewPager.adapter =
                    TemplateMakerPagerAdapter(this@TemplateMakerActivity, titles, views)
            }
        }
    }
}