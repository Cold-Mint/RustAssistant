package com.coldmint.rust.pro.edit

import android.content.Context
import android.util.Log
import com.coldmint.rust.core.CodeCompiler2
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.SectionInfo
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.interfaces.EnglishMode
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.CompletionItemConverter
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.widget.CodeEditor
import java.util.ArrayList
import java.util.concurrent.Executors

/**
 * @author Cold Mint
 * @date 2022/1/25 17:32
 */
class RustAutoComplete(val context: Context) : EnglishMode {

    private val debugKey = "自动完成器"
    private var codeDataBase: CodeDataBase? = null
    private val executorService = Executors.newCachedThreadPool()
    private var fileDataBase: FileDataBase? = null
    private var codeEditor: CodeEditor? = null

    private var sectionNameMap: HashMap<String, String> = HashMap()

    private val lineParser by lazy {
        val tem = LineParser()
        tem.symbol = ","
        tem
    }
    private val identifiersPromptNumber: Int by lazy {
        AppSettings.getValue(AppSettings.Setting.IdentifiersPromptNumber, 40)
    }

    //类型转换器
    private val completionItemConverter: CompletionItemConverter by lazy {
        CompletionItemConverter.instance.init(context)
    }


    companion object {
        var keyWord = ""
    }

    /**
     * 是否为英文模式
     */
    private var isEnglishMode = false

    /**
     * 设置源文件目录
     * @param sourceFolder String
     */
    fun setSourceFolder(sourceFolder: String) {
        DebugHelper.printLog(debugKey, "已设置源文件目录" + sourceFolder, "设置源文件目录")
        completionItemConverter.setSourceFilePath(sourceFolder)
    }

    /**
     * 设置文件转换器配置
     * @param sourceFilePath String
     * @param rootCodeName String
     * @param modFolder String
     */
    fun setConfigurationFileConversion(
        sourceFilePath: String,
        rootCodeName: String,
        modFolder: String
    ) {
        completionItemConverter.configurationFileConversion(sourceFilePath, rootCodeName, modFolder)
    }

    /**
     *设置文件数据库
     * @param fileDataBase FileDataBase?
     */
    fun setFileDataBase(fileDataBase: FileDataBase?) {
        this.fileDataBase = fileDataBase
    }


    fun setCodeDataBase(codeDataBase: CodeDataBase) {
        this.codeDataBase = codeDataBase
    }

    /**
     * 设置代码编辑器
     * @param codeEditor CodeEditor?
     */
    fun setCodeEditor(codeEditor: CodeEditor?) {
        this.codeEditor = codeEditor
    }


    /**
     * 获取节类型(永远返回英文)
     * @param section String
     * @return String
     */
    fun getSectionType(section: String): String {
        if (sectionNameMap.containsKey(section)) {
            return sectionNameMap[section] ?: SourceFile.getSectionType(section)
        }
        val result = if (isEnglishMode) {
            SourceFile.getSectionType(section)
        } else {
            val sectionInfo = codeDataBase!!.getSectionDao()
                .findSectionInfoByTranslate(SourceFile.getSectionType(section))
            sectionInfo?.code ?: SourceFile.getSectionType(section)
        }
        sectionNameMap[section] = result
        return result
    }


    /**
     * 请求自动完成
     * @param prefix String?
     * @param completionPublisher CompletionPublisher
     * @param line Int
     * @param column Int
     */
    fun requireAutoComplete(
        prefix: String?,
        completionPublisher: CompletionPublisher,
        line: Int,
        column: Int
    ) {
        if (codeEditor == null) {
            throw NullPointerException("未绑定编辑框")
        }
        if (fileDataBase == null) {
            throw NullPointerException("未绑定文件数据库")

        }
        if (codeDataBase == null) {
            throw NullPointerException("未绑定代码数据库")
        }
        if (prefix == null) {
            return
        }

        keyWord = prefix
        val temCodeEditor: CodeEditor = codeEditor!!
        val lineData = temCodeEditor.text.getLineString(line)
        if (lineData.startsWith('[')) {
            //搜索节
            val name = lineData.subSequence(1, lineData.length).toString()
            val list = if (isEnglishMode) {
                codeDataBase!!.getSectionDao()
                    .searchSectionInfoByCode(name, limitNum = identifiersPromptNumber)
            } else {
                codeDataBase!!.getSectionDao()
                    .searchSectionInfoByTranslate(name, limitNum = identifiersPromptNumber)
            }
            list?.forEach {
                completionPublisher.addItem(completionItemConverter.sectionInfoToCompletionItem(it))
            }
        } else {
            val key = ":"
            val keyIndex = lineData.lastIndexOf(key)
            if (keyIndex > -1) {
                //检查是否可响应变量
                val start = "\${"
                val end = "}"
                val startIndex = lineData.lastIndexOf(start)
                if (startIndex > -1) {
                    val endIndex = lineData.lastIndexOf(end)
                    if (endIndex < startIndex) {
                        //如果}的位置小于${的位置，说明没有闭合
                        val keyWord =
                            lineData.subSequence(startIndex + start.length, lineData.length)
                                .toString()
                        val previousText =
                            lineData.subSequence(keyIndex + key.length, startIndex).toString()
                        if (keyWord.isNotBlank()) {
                            RustAnalyzer.localVariableNameList.forEach {
                                if (it.name.contains(keyWord)) {
                                    completionPublisher.addItem(
                                        completionItemConverter.localVariableNameToCompletionItem(
                                            it, previousText
                                        )
                                    )
                                }
                            }
                        } else {
                            RustAnalyzer.localVariableNameList.forEach {
                                completionPublisher.addItem(
                                    completionItemConverter.localVariableNameToCompletionItem(
                                        it, previousText
                                    )
                                )
                            }
                        }
                        RustAutoComplete.keyWord = keyWord
                        return
                    }
                }

//                    val value =
//                        lineData.subSequence(keyIndex + key.length, lineData.length).toString().trim()
//                    Log.d("值", value)
                //搜索值
                // frontIndex 前面冒号的位置
                val frontIndex = lineData.lastIndexOf(key, keyIndex - key.length)
                val keyValue = if (frontIndex > -1) {
                    lineData.subSequence(frontIndex + key.length, keyIndex)
                } else {
                    lineData.subSequence(0, keyIndex)
                }

                val codeInfo =
                    if (isEnglishMode) {
                        codeDataBase!!.getCodeDao().findCodeByCode(keyValue.toString())
                    } else {
                        codeDataBase!!.getCodeDao().findCodeByTranslate(keyValue.toString())
                    }
                DebugHelper.printLog(
                    debugKey,
                    "值[" + keyValue + "]英文模式[" + isEnglishMode + "]代码信息[" + codeInfo + "]关键字[" + prefix + "]",
                    "值检查"
                )
                if (codeInfo != null) {
                    val typeInfo = completionItemConverter.getValueType(codeInfo.type)
                    //获取代码的关联列表
                    if (typeInfo != null && typeInfo.list.isNotBlank()) {
                        DebugHelper.printLog(
                            debugKey,
                            "值类型[" + codeInfo.type + "]自动提示列表[" + typeInfo.list + "]", "关联提示"
                        )
                        lineParser.text = typeInfo.list
                        lineParser.analyse { lineNum, lineData, isEnd ->
                            //分析关联提示项目
                            val temCodeInfo =
                                codeDataBase!!.getCodeDao().findCodeByCode(lineData)
                            DebugHelper.printLog(
                                debugKey,
                                "值类型[" + codeInfo.type + "]项目[" + lineData + "]是代码[" + (temCodeInfo != null) + "]",
                                "关联提示列表分析"
                            )
                            if (temCodeInfo == null) {
                                if (lineData.startsWith("@file(") && lineData.endsWith(")")) {
                                    val fileType = lineData.subSequence(
                                        lineData.indexOf('(') + 1,
                                        lineData.indexOf(')')
                                    )

                                    val fileInfo = fileDataBase!!.getFileInfoDao()
                                        .searchFileInfoByNameAndType(
                                            prefix,
                                            fileType.toString(),
                                            identifiersPromptNumber
                                        )
                                    DebugHelper.printLog(
                                        debugKey,
                                        "值类型[" + codeInfo.type + "]项目[" + lineData + "]搜索了[" + fileType + "]类型的文件，返回了[" + (fileInfo?.size
                                            ?: -1) + "]个结果",
                                        "关联提示列表分析"
                                    )
                                    if (fileInfo != null && fileInfo.isNotEmpty()) {
                                        for (fileTable in fileInfo) {
                                            completionPublisher.addItem(
                                                completionItemConverter.fileTableToCompletionItem(
                                                    fileTable
                                                )
                                            )
                                        }
                                    }
                                } else if (lineData.startsWith("@customType(") && lineData.endsWith(
                                        ")"
                                    )
                                ) {
                                    //用户自动值类型提示
                                    val customType = lineData.subSequence(
                                        lineData.indexOf('(') + 1,
                                        lineData.indexOf(')')
                                    ).toString()
                                    fileDataBase!!.getValueDao()
                                        .searchValueByKey(
                                            prefix,
                                            customType,
                                            identifiersPromptNumber
                                        )
                                        ?.forEach {
                                            completionPublisher.addItem(
                                                completionItemConverter.valueTableToCompletionItem(
                                                    it
                                                )
                                            )
                                        }
                                } else if (lineData.startsWith("@type(") && lineData.endsWith(")")) {
                                    val type = lineData.subSequence(
                                        lineData.indexOf('(') + 1,
                                        lineData.indexOf(')')
                                    ).toString()
                                    val list = if (isEnglishMode
                                    ) {
                                        codeDataBase!!.getCodeDao().findCodeByCodeInType(
                                            prefix,
                                            type,
                                            identifiersPromptNumber
                                        )
                                    } else {
                                        codeDataBase!!.getCodeDao().findCodeByTranslateInType(
                                            prefix,
                                            type,
                                            identifiersPromptNumber
                                        )
                                    }
                                    DebugHelper.printLog(
                                        debugKey,
                                        "关联了值类型[" + type + "]获取[" + (list?.size ?: -1) + "]个结果",
                                        "值类型引用"
                                    )
                                    if (!list.isNullOrEmpty()) {
                                        list.forEach {
                                            completionPublisher.addItem(
                                                completionItemConverter.codeInfoToCompletionItem(
                                                    it
                                                )
                                            )
                                        }
                                    }
                                } else if (lineData.startsWith("@section") && lineData.endsWith(
                                        ")"
                                    )
                                ) {
                                    val section = lineData.subSequence(
                                        lineData.indexOf('(') + 1,
                                        lineData.indexOf(')')
                                    ).toString()
//                                        codeEditor!!.textAnalyzeResult.navigation.forEach {
//                                            if (section == getSectionType(it.label)) {
//                                                result.add(
//                                                    completionItemConverter.navigationItemToCompletionItem(
//                                                        it
//                                                    )
//                                                )
//                                            }
//                                        }
                                } else {
                                    //无法分析的关联项目
//                                        result.add(
//                                            CompletionItem(
//                                                lineData,
//                                                String.format(
//                                                    completionItemConverter.associatedTip,
//                                                    codeInfo.translate
//                                                )
//                                            )
//                                        )
                                }
                            } else {

                                val show = if (isEnglishMode) {
                                    temCodeInfo.code.contains(prefix)
                                } else {
                                    temCodeInfo.translate.contains(prefix)
                                }
                                DebugHelper.printLog(
                                    debugKey,
                                    "值类型[" + codeInfo.type + "]项目[" + lineData + "]是否包含" + prefix + "关键字[" + show + "]",
                                    "关联提示列表分析"
                                )
                                if (show) {
                                    completionPublisher.addItem(
                                        completionItemConverter.codeInfoToCompletionItem(
                                            temCodeInfo
                                        )
                                    )
                                }
                            }
                            true
                        }
                    }
                }
            } else {
                //如果不包含:搜索键
//                    val navigationList = temCodeEditor.textAnalyzeResult.navigation
//                    var section: String? = null
//                    if (navigationList != null && navigationList.isNotEmpty()) {
//                        for (navigation in navigationList) {
//                            if (navigation.line > line) {
//                                break
//                            } else {
//                                section = navigation.label
//                            }
//                        }
//                    }
//                    //如果不在任何节内
//                    if (section == null) {
//                        return@submit
//                    }
//                    val trueSection = getSectionType(section)
//                    val list = if (isEnglishMode) {
//                        codeDataBase!!.getCodeDao().findCodeByEnglishCodeKeyFromSection(
//                            prefix,
//                            trueSection,
//                            identifiersPromptNumber
//                        )
//                    } else {
//                        codeDataBase!!.getCodeDao()
//                            .findCodeByKeyFromSection(prefix, trueSection, identifiersPromptNumber)
//                    }
//                    if (list != null && list.isNotEmpty()) {
//                        list.forEach {
//                            result.add(
//                                completionItemConverter.codeInfoToCompletionItem(it)
//                            )
//                        }
//                    }
            }
        }
    }

    override fun isEnglishMode(): Boolean {
        return isEnglishMode
    }

    override fun setEnglish(englishMode: Boolean) {
        this.isEnglishMode = englishMode
        completionItemConverter.setEnglish(englishMode)
    }
}