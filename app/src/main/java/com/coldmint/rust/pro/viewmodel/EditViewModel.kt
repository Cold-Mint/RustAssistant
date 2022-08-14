package com.coldmint.rust.pro.viewmodel

import android.app.Application
import android.os.Looper
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.coldmint.rust.core.*
import com.coldmint.rust.core.dataBean.CompileConfiguration
import com.coldmint.rust.core.database.code.ValueTypeInfo
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.database.file.FileTable
import com.coldmint.rust.core.database.file.HistoryRecord
import com.coldmint.rust.core.interfaces.CodeCompilerListener
import com.coldmint.rust.core.interfaces.CodeTranslatorListener
import com.coldmint.rust.core.interfaces.EnglishMode
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.base.BaseAndroidViewModel
import com.coldmint.rust.pro.livedata.OpenedSourceFileListLiveData
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 编辑器视图模型
 */
class EditViewModel(application: Application) : BaseAndroidViewModel(application), EnglishMode {

    val englishModeLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    /**
     * 目标文件
     * 在右侧（找不到资源）选择文件时产生的目标文件
     */
    var targetFile: File? = null

    /**
     * 模组类
     */
    var modClass: ModClass? = null

    /**
     * 是否正在复制文件
     */
    var processFiles = false

    /**
     * 是否需要检查自动保存
     */
    var needCheckAutoSave = true

    /**
     * 获取当前使用的apk包存放目录
     */
    val apkFolder by lazy {
        GameSynchronizer.getPackAgeFolder(
            getApplication(), AppSettings.getValue(
                AppSettings.Setting.GamePackage,
                GlobalMethod.DEFAULT_GAME_PACKAGE
            )
        )
    }

    /**
     * 现在打开的文件路径
     */
    private var nowFilePath: String? = null

    /**
     * 设置当前打开的目录
     * @param path String
     */
    fun setNowOpenFilePath(path: String) {
        nowFilePath = path
    }

    /**
     * 获取现在打开的文件路径（调用之前请先打开文件[EditViewModel.openFile]，若未打开文件抛出异常）
     * @return String?
     */
    fun getNowOpenFilePath(): String {
        val temPath = nowFilePath
        return if (temPath == null) {
            throw NullPointerException("无法获取最近打开的文件，请先打开文件")
            ""
        } else {
            temPath
        }
    }

    /**
     * 代码编辑器
     */
    val codeCompiler2: CodeCompiler2 by lazy {
        CodeCompiler2.getInstance(application)
    }

    val executorService: ExecutorService by lazy {
        Executors.newCachedThreadPool()
    }

    /**
     * 是否需要保存
     */
    val needSaveLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    /**
     * 是否正在加载
     */
    val loadingLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    /**
     * 打开的文件列表
     */
    val openedSourceFileListLiveData: OpenedSourceFileListLiveData by lazy {
        OpenedSourceFileListLiveData()
    }


    /**
     * 当前显示的代码
     */
    val codeLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    /**
     * 编译文件
     * 编译前请检查文本是否更改[OpenedSourceFile.isChanged]
     * @param openedSourceFile OpenedSourceFile 打开的文件对象
     * @param codeCompilerListener CodeCompilerListener? 代码编译监听器
     */
    fun compilerFile(
        openedSourceFile: OpenedSourceFile,
        codeCompilerListener: CodeCompilerListener? = null
    ) {
        codeCompiler2.compile(
            openedSourceFile.getEditText(),
            CompileConfiguration(
                getApplication(),
                openedSourceFile,
                modClass!!,
                apkFolder
            ),
            codeCompilerListener
        )
    }

    /**
     * 关闭文件
     * @param openedSourceFile OpenedSourceFile
     */
    fun closeFile(openedSourceFile: OpenedSourceFile) {
        var index =
            openedSourceFileListLiveData.indexOf(openedSourceFile)
        index -= 1
        if (index > -1) {
            codeLiveData.value =
                openedSourceFileListLiveData.getOpenedSourceFile(
                    index
                ).getEditText()
            nowFilePath = openedSourceFile.file.absolutePath
        }
        openedSourceFileListLiveData.remove(openedSourceFile)
    }

    /**
     * 打开文件
     * 打开文件到编辑器内
     * @param path String
     */
    fun openFile(path: String) {
        executorService.submit {
            val code = FileOperator.readFile(File(path)) ?: return@submit
            codeCompiler2.translation(code, object : CodeTranslatorListener {
                override fun beforeTranslate() {

                }

                override fun onTranslateComplete(code: String) {
                    val openedSourceFile = OpenedSourceFile(path)
                    openedSourceFile.setTranslation(code)
                    val index = openedSourceFileListLiveData.add(openedSourceFile)
                    if (index == -1) {
                        nowFilePath = path
                        codeLiveData.postValue(code)
                        addHistoryRecord(SourceFile(File(path)))
                    } else {
                        val oldOpenedSourceFile =
                            openedSourceFileListLiveData.getOpenedSourceFile(index)
                        //如果不是当前打开的文件
                        if (nowFilePath != oldOpenedSourceFile.file.absolutePath) {
                            nowFilePath = oldOpenedSourceFile.file.absolutePath
                            codeLiveData.postValue(oldOpenedSourceFile.getEditText())
                            openedSourceFileListLiveData.refresh()
                        }
                    }
                    loadingLiveData.postValue(false)
                }
            })
        }
    }

    /**
     * 获取与模组关联的文件数据库对象
     * @return FileDataBase?
     */
    private fun getFileDataBase(): FileDataBase? {
        val temModClass = modClass
        return if (temModClass == null) {
            null
        } else {
            FileDataBase.getInstance(getApplication(), temModClass.modName)
        }
    }


    /**
     * 检查文件是否需要保存
     * @param selectedIndex Int
     * @param text String
     */
    fun checkFilesIfNeedSave(selectedIndex: Int, text: String): Boolean {
        var need = false
        openedSourceFileListLiveData.value.forEachIndexed { index, openedSourceFile ->
            need = if (index == selectedIndex) {
                openedSourceFile.isChanged(text)
            } else {
                openedSourceFile.isNeedSave()
            }
            if (need) {
                return@forEachIndexed
            }
        }
        needSaveLiveData.value = need
        return need
    }


    /**
     * 移除文件在数据库内
     * @param file File
     */
    fun removeFileInDataBase(file: File) {
        executorService.submit {
            val fileDataBase = getFileDataBase() ?: return@submit
            fileDataBase.getFileInfoDao()
                .delete(
                    FileTable(
                        file.absolutePath,
                        FileOperator.getPrefixName(file),
                        FileOperator.getMD5(file) ?: "",
                        FileOperator.getFileType(file)
                    )
                )
        }
    }

    /**
     * 添加文件在数据库内
     * @param file File
     */
    fun addFileInDataBase(file: File) {
        executorService.submit {
            val fileDataBase = getFileDataBase() ?: return@submit
            val oldFileTable = fileDataBase.getFileInfoDao().findFileInfoByPath(file.absolutePath)
            val newFileTable = FileTable(
                file.absolutePath,
                FileOperator.getPrefixName(file),
                FileOperator.getMD5(file) ?: "",
                FileOperator.getFileType(file)
            )
            if (oldFileTable == null) {
                fileDataBase.getFileInfoDao().insert(newFileTable)
            } else {
                fileDataBase.getFileInfoDao().update(newFileTable)
            }
        }
    }

    /**
     * 保存单个文件
     * @param openedSourceFile OpenedSourceFile
     */
    fun saveOneFile(openedSourceFile: OpenedSourceFile) {
        compilerFile(openedSourceFile, object : CodeCompilerListener {
            override fun onCompilationComplete(
                compileConfiguration: CompileConfiguration,
                code: String
            ) {
                openedSourceFile.save(code)
            }

            override fun beforeCompilation() {

            }

            override fun onClickKeyNotFoundItem(
                lineNum: Int,
                columnNum: Int,
                view: View,
                code: String,
                section: String
            ) {
            }

            override fun onClickValueTypeErrorItem(
                lineNum: Int,
                columnNum: Int,
                view: View,
                valueType: ValueTypeInfo
            ) {

            }

            override fun onClickSectionIndexError(
                lineNum: Int,
                columnNum: Int,
                view: View,
                sectionName: String
            ) {
            }

            override fun onClickResourceErrorItem(
                lineNum: Int,
                columnNum: Int,
                view: View,
                resourceFile: File
            ) {
            }


            override fun onClickSectionErrorItem(
                lineNum: Int,
                view: View,
                displaySectionName: String
            ) {

            }

            override fun onClickSynchronizationGame(lineNum: Int, columnNum: Int, view: View) {
            }

            override fun onClickSectionNameErrorItem(
                lineNum: Int,
                columnNum: Int,
                view: View,
                sectionName: String,
                symbolIndex: Int?,
                needName: Boolean
            ) {

            }

            override fun onClickCodeIndexErrorItem(lineNum: Int, view: View, sectionName: String) {

            }

            override fun onShowCompilationResult(code: String): Boolean {
                return false
            }

        })
    }

    /**
     * 保存所有文件方法，内部会自动检查是否需要保存
     * @param selectedIndex Int 当前打开的文件位置
     * @param text String 当前编辑框内文本
     * @param funSaveOK 当需要保存，并且保存完成调用的函数
     */
    fun saveAllFile(selectedIndex: Int, text: String, funSaveOK: () -> Unit) {
        val codeCompilerListenerFun: (OpenedSourceFile) -> CodeCompilerListener = {
            object : CodeCompilerListener {
                override fun onCompilationComplete(
                    compileConfiguration: CompileConfiguration,
                    code: String
                ) {
                    it.save(code)
                }

                override fun beforeCompilation() {

                }

                override fun onClickKeyNotFoundItem(
                    lineNum: Int,
                    columnNum: Int,
                    view: View,
                    code: String,
                    section: String
                ) {

                }

                override fun onClickValueTypeErrorItem(
                    lineNum: Int,
                    columnNum: Int,
                    view: View,
                    valueType: ValueTypeInfo
                ) {

                }

                override fun onClickSectionIndexError(
                    lineNum: Int,
                    columnNum: Int,
                    view: View,
                    sectionName: String
                ) {

                }

                override fun onClickResourceErrorItem(
                    lineNum: Int,
                    columnNum: Int,
                    view: View,
                    resourceFile: File
                ) {
                }


                override fun onClickSectionErrorItem(
                    lineNum: Int,
                    view: View,
                    displaySectionName: String
                ) {

                }

                override fun onClickSynchronizationGame(lineNum: Int, columnNum: Int, view: View) {
                }

                override fun onClickSectionNameErrorItem(
                    lineNum: Int,
                    columnNum: Int,
                    view: View,
                    sectionName: String,
                    symbolIndex: Int?,
                    needName: Boolean
                ) {

                }

                override fun onClickCodeIndexErrorItem(
                    lineNum: Int,
                    view: View,
                    sectionName: String
                ) {
                }

                override fun onShowCompilationResult(code: String): Boolean {
                    return false
                }

            }
        }
        //是否需要保存
        var needSave = false
        openedSourceFileListLiveData.value.forEachIndexed { index, openedSourceFile ->
            if (index == selectedIndex) {
                if (openedSourceFile.isChanged(text)) {
                    needSave = true
                    compilerFile(
                        openedSourceFile,
                        codeCompilerListenerFun.invoke(openedSourceFile)
                    )
                }
            } else {
                if (openedSourceFile.isNeedSave()) {
                    needSave = true
                    compilerFile(
                        openedSourceFile,
                        codeCompilerListenerFun.invoke(openedSourceFile)
                    )
                }
            }
        }
        //当真正保存了调用
        if (needSave) {
            funSaveOK.invoke()
        }
    }


    /**
     * 添加或更新历史记录
     * @param file SourceFile
     */
    private fun addHistoryRecord(file: SourceFile) {
        val funData: () -> Unit = {
            val fileDataBase = getFileDataBase()
            if (fileDataBase != null) {
                val path = file.file.absolutePath
                val name = file.getName(
                    AppSettings.getValue(
                        AppSettings.Setting.AppLanguage,
                        Locale.getDefault().language
                    )
                ) + " (" + file.file.name + ")"
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val historyDao = fileDataBase.getHistoryDao()
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
            }
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            executorService.submit {
                funData.invoke()
            }
        } else {
            funData.invoke()
        }
    }

    /**
     * 转换为16进制
     *
     * @param num 十进制整数
     * @return 16进制数
     */
    fun convertDigital(num: Int): String {
        return if (num > 255) {
            "FF"
        } else {
            val builder = StringBuilder()
            val result = num / 16
            val remainder = num % 16
            when (result) {
                10 -> builder.append('A')
                11 -> builder.append('B')
                12 -> builder.append('C')
                13 -> builder.append('D')
                14 -> builder.append('E')
                15 -> builder.append('F')
                else -> builder.append(result)
            }
            when (remainder) {
                10 -> builder.append('A')
                11 -> builder.append('B')
                12 -> builder.append('C')
                13 -> builder.append('D')
                14 -> builder.append('E')
                15 -> builder.append('F')
                else -> builder.append(remainder)
            }
            builder.toString()
        }
    }

    /**
     * 加载数据
     */
    fun loadData() {
        val english = AppSettings
            .getValue(AppSettings.Setting.EnglishEditingMode, false)
        setEnglish(english)
    }

    override fun isEnglishMode(): Boolean {
        return codeCompiler2.isEnglishMode()
    }

    override fun setEnglish(englishMode: Boolean) {
        codeCompiler2.setEnglish(englishMode)
        englishModeLiveData.value = englishMode
    }

}