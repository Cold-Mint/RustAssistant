package com.coldmint.rust.pro.fragments

import com.coldmint.rust.pro.EditActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.database.file.FileTable
import com.coldmint.rust.core.database.file.HistoryRecord
import com.coldmint.rust.core.interfaces.FileFinderListener
import com.coldmint.rust.core.tool.FileFinder2
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.UnitAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentAllUnitsBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * @author Cold Mint
 * @date 2022/1/13 21:32
 */
class AllUnitsFragment(

) :
    BaseFragment<FragmentAllUnitsBinding>() {
    var fragmentActivity: FragmentActivity? = null
    var modClass: ModClass? = null
    var fileDatabase: FileDataBase? = null
    var whenNumberChanged: ((Int) -> Unit)? = null
    val executorService = Executors.newSingleThreadExecutor()
    val dataList: ArrayList<SourceFile> = ArrayList()

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.unitList.layoutManager = StableLinearLayoutManager(requireContext())
        val divider = MaterialDividerItemDecoration(
            requireContext(),
            MaterialDividerItemDecoration.VERTICAL
        )

        viewBinding.unitList.addItemDecoration(
            divider
        )
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            loadFiles()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
        loadFiles()
    }


    /**
     * 打开编辑器活动
     * @param file SourceFileClass
     */
    fun openEditActivity(file: SourceFile) {
        val handler = Handler(Looper.getMainLooper())
        executorService.submit {
            val bundle = Bundle()
            val path = file.file.absolutePath
            bundle.putString("path", path)
            bundle.putString("modPath", modClass!!.modFile.absolutePath)
            addFileToHistory(file, handler = handler, whenAddComplete = {
                val intent = Intent(requireContext(), EditActivity::class.java)
                intent.putExtra("data", bundle)
                fragmentActivity?.startActivityForResult(intent, 2)
            })
        }
    }

    /**
     * 添加文件到历史记录
     * @param file SourceFileClass
     * @param useThread Boolean
     */
    fun addFileToHistory(
        file: SourceFile,
        useThread: Boolean = false,
        handler: Handler? = null,
        whenAddComplete: () -> Unit
    ) {
        val useHandler = handler ?: Handler(Looper.getMainLooper())
        val funData: () -> Unit = {
            val path = file.file.absolutePath
            val name = file.getName(
                AppSettings.getValue(
                    AppSettings.Setting.AppLanguage,
                    Locale.getDefault().language
                )
            ) + " (" + file.file.name + ")"
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val historyDao = fileDatabase!!.getHistoryDao()
            val newHistoryRecord = HistoryRecord(
                path,
                name,
                formatter.format(System.currentTimeMillis())
            )
            if (historyDao.findHistoryByPath(path) == null) {
                historyDao.insert(
                    newHistoryRecord
                )
            } else {
                historyDao.update(newHistoryRecord)
            }
            useHandler.post {
                whenAddComplete.invoke()
            }
        }
        if (useThread) {
            executorService.submit {
                funData.invoke()
            }
        } else {
            funData.invoke()
        }
    }

    /**
     * 加载列表
     * @param file File
     */
    fun loadFiles(file: File = modClass!!.modFile) {
        val handler = Handler(Looper.getMainLooper())
        executorService.submit {
            handler.post {
                viewBinding.swipeRefreshLayout.isVisible = false
                viewBinding.progressBar.isVisible = true
                viewBinding.unitError.isVisible = false
            }
            dataList.clear()
            val fileFinder2 = FileFinder2(file)
            val data = modClass!!.modConfigurationManager?.readData()
            val sourceFileFilteringRule =
                Regex(data?.sourceFileFilteringRule ?: ".+\\.ini|.+\\.template")
            fileFinder2.setFinderListener(object : FileFinderListener {
                override fun whenFindFile(file: File): Boolean {
                    if (fileDatabase == null || modClass == null)
                    {
                        return false
                    }
                    //此处在这里判断的目的是将所有文件录入数据库
                    if (file.name.matches(sourceFileFilteringRule)) {
                        val sourceFileClass = SourceFile(file, modClass!!)
                        fileDatabase!!.addValuesFromSourceFile(requireContext(), sourceFileClass)
                        dataList.add(sourceFileClass)
                    }
                    val fileTable = FileDataBase.createFileInfoFromFile(file)
                    if (fileDatabase!!.getFileInfoDao().findFileInfoByPath(file.absolutePath) == null
                    ) {
                        fileDatabase!!.getFileInfoDao().insert(fileTable)
                    } else {
                        fileDatabase!!.getFileInfoDao().update(fileTable)
                    }
                    return true
                }

                override fun whenFindFolder(folder: File): Boolean {
                    return true
                }

            })
            fileFinder2.onStart()
            if (dataList.isEmpty()) {
                notFindUnits(handler)
            } else {
                handler.post {
                    viewBinding.swipeRefreshLayout.isVisible = true
                    viewBinding.progressBar.isVisible = false
                    viewBinding.unitError.isVisible = false
                    if (isAdded) {
                        val adapter = UnitAdapter(requireContext(), dataList, "")
                        adapter.setItemEvent { i, unitItemBinding, viewHolder, sourceFileClass ->
                            unitItemBinding.root.setOnClickListener {
                                openEditActivity(sourceFileClass)
                            }
                        }
                        adapter.setItemChangeEvent { changeType, i, sourceFileClass, i2 ->
                            whenNumberChanged?.invoke(i2)
                            if (i2 == 0) {
                                loadFiles()
                            }
                        }
                        FastScrollerBuilder(viewBinding.unitList).useMd2Style()
                            .setPopupTextProvider(adapter).build()
                        viewBinding.unitList.adapter = adapter
                        whenNumberChanged?.invoke(dataList.size)
                    }
                }
            }
        }
    }


    /**
     * 没有找到单位
     * @param handler Handler?
     */
    fun notFindUnits(handler: Handler? = null, key: String? = null, searchMode: Boolean = false) {
        val temHandler = handler ?: Handler(Looper.getMainLooper())
        temHandler.post {
            viewBinding.swipeRefreshLayout.isVisible = false
            viewBinding.progressBar.isVisible = false
            viewBinding.unitError.isVisible = true
            if (key != null) {
                val source =
                    String.format(getString(R.string.not_find_units_name), key)
                val spannableString = SpannableString(source)
                val action = getString(R.string.not_find_units_action)
                spannableString.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        loadFiles()
                    }
                }, source.indexOf(action), source.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                viewBinding.unitError.movementMethod = LinkMovementMethod.getInstance()
                viewBinding.unitError.text = spannableString
            } else {
                if (searchMode) {
                    viewBinding.unitError.text = getString(R.string.search_tip)
                    viewBinding.unitError.setOnClickListener {
                        loadFiles()
                    }
                } else {
                    viewBinding.unitError.setText(R.string.not_find_units)
                }
            }
            whenNumberChanged?.invoke(0)
        }
    }


    /**
     * 高级搜索
     */
    fun advancedSearch(file: File = modClass!!.modFile, configuration: SearchConfiguration) {
        val handler = Handler(Looper.getMainLooper())
        executorService.submit {
            handler.post {
                viewBinding.swipeRefreshLayout.isVisible = false
                viewBinding.progressBar.isVisible = true
                viewBinding.unitError.isVisible = false
            }
            dataList.clear()
            val data = modClass!!.modConfigurationManager?.readData()
            val sourceFileFilteringRule = data?.sourceFileFilteringRule ?: ".+\\.ini|.+\\.template"
            val language = AppSettings
                .getValue(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
            val fileFinder2 = FileFinder2(file)
            fileFinder2.findMode = true
            if (configuration.advancedSearch) {
                //高级搜索模式下，文件查找规则可以被文件输入内容代理
                val findRule = if (configuration.fileRule.isNullOrBlank()) {
                    fileFinder2.asRe = true
                    sourceFileFilteringRule
                } else {
                    fileFinder2.asRe = configuration.useRe
                    configuration.fileRule
                }
                fileFinder2.findRule = findRule
            } else {
                fileFinder2.findRule = sourceFileFilteringRule
                fileFinder2.asRe = true
            }
            val fileContent =
                if (configuration.isCode && configuration.fileContent != null && configuration.fileContent.isNotBlank()) {
                    val codeInfo = CodeDataBase.getInstance(requireContext()).getCodeDao()
                        .findCodeByTranslate(configuration.fileContent)
                    codeInfo?.code ?: configuration.fileContent
                } else {
                    configuration.fileContent
                }
            fileFinder2.setFinderListener(object : FileFinderListener {
                override fun whenFindFile(file: File): Boolean {
                    val sourceFileClass = SourceFile(file)
                    if (sourceFileClass.getName(language).contains(configuration.unitName)) {
                        //是高级搜索模式，并且包含文件内容
                        if (configuration.advancedSearch) {
                            if (fileContent.isNullOrBlank()) {
                                dataList.add(sourceFileClass)
                            } else {
                                if (sourceFileClass.text.contains(fileContent)) {
                                    dataList.add(sourceFileClass)
                                }
                            }
                        } else {
                            dataList.add(sourceFileClass)
                        }
                    }
                    return true
                }

                override fun whenFindFolder(folder: File): Boolean {
                    return true
                }
            })
            fileFinder2.onStart()
            if (dataList.isEmpty()) {
                notFindUnits(handler, searchMode = true)
            } else {
                handler.post {
                    viewBinding.swipeRefreshLayout.isVisible = true
                    viewBinding.progressBar.isVisible = false
                    viewBinding.unitError.isVisible = true
                    viewBinding.unitError.text = getString(R.string.search_tip)
                    viewBinding.unitError.setOnClickListener {
                        loadFiles()
                    }
                    val adapter = UnitAdapter(requireContext(), dataList, configuration.unitName)
                    adapter.setItemEvent { i, unitItemBinding, viewHolder, sourceFileClass ->
                        unitItemBinding.root.setOnClickListener {
                            openEditActivity(sourceFileClass)
                        }
                    }
                    adapter.setItemChangeEvent { changeType, i, sourceFileClass, i2 ->
                        whenNumberChanged?.invoke(i2)
                        if (i2 == 0) {
                            loadFiles()
                        }
                    }
                    viewBinding.unitList.adapter = adapter
                    whenNumberChanged?.invoke(dataList.size)
                }
            }
        }
    }

    /**
     * 过滤数据
     * @param key String
     */
    fun filter(key: String) {
        val handler = Handler(Looper.getMainLooper())
        executorService.submit {
            if (dataList.isEmpty()) {
                notFindUnits(handler)
            } else {
                val newList = ArrayList<SourceFile>()
                val language = AppSettings
                    .getValue(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
                dataList.forEach {
                    if (it.getName(language).contains(key)) {
                        newList.add(it)
                    }
                }
                if (newList.isEmpty()) {
                    notFindUnits(handler, key)
                } else {
                    handler.post {
                        viewBinding.swipeRefreshLayout.isVisible = true
                        viewBinding.progressBar.isVisible = false
                        viewBinding.unitError.isVisible = true
                        viewBinding.unitError.text = getString(R.string.not_find_units_action)
                        viewBinding.unitError.setOnClickListener {
                            loadFiles()
                        }
                        val adapter = UnitAdapter(requireContext(), newList, key)
                        adapter.setItemEvent { i, unitItemBinding, viewHolder, sourceFileClass ->
                            unitItemBinding.root.setOnClickListener {
                                openEditActivity(sourceFileClass)
                            }
                        }
                        adapter.setItemChangeEvent { changeType, i, sourceFileClass, i2 ->
                            whenNumberChanged?.invoke(i2)
                            if (i2 == 0) {
                                loadFiles()
                            }
                        }
                        viewBinding.unitList.adapter = adapter
                        whenNumberChanged?.invoke(newList.size)
                    }
                }
            }
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentAllUnitsBinding {
        return FragmentAllUnitsBinding.inflate(layoutInflater)
    }

    /**
     * 搜索配置类
     * @property unitName String
     * @property fileRule String?
     * @property useRe Boolean
     * @property fileContent String?
     * @property advancedSearch Boolean
     * @constructor
     */
    data class SearchConfiguration(
        val unitName: String,
        val fileRule: String? = null,
        val useRe: Boolean = false,
        val fileContent: String? = null,
        val advancedSearch: Boolean = false,
        val isCode: Boolean = false
    )
}