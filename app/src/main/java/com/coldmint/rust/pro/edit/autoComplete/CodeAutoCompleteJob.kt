package com.coldmint.rust.pro.edit.autoComplete

import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.database.code.CodeDao
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.edit.RustAnalyzer
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.CompletionItemConverter
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference

/**
 * 代码自动完成项目（来自数据库）
 */
class CodeAutoCompleteJob : AutoCompleteJob {

    private val debugKey = "代码自动完成（数据库）"
    private var codeDataBase: CodeDataBase? = null
    private var fileDataBase: FileDataBase? = null
    private lateinit var codeDao: CodeDao
    private val identifiersPromptNumber: Int by lazy {
        AppSettings.getValue(AppSettings.Setting.IdentifiersPromptNumber, 40)
    }
    private var sectionNameMap: HashMap<String, String> = HashMap()


    private val lineParser by lazy {
        val tem = LineParser()
        tem.symbol = ","
        tem
    }

    /**
     * 设置代码数据库
     * @param codeDataBase CodeDataBase
     */
    fun setCodeDataBase(codeDataBase: CodeDataBase) {
        DebugHelper.printLog(debugKey, "已链接数据库，数据库状态:${codeDataBase.isOpen}。")
        this.codeDataBase = codeDataBase
        this.codeDao = codeDataBase.getCodeDao()
    }

    /**
     * 设置文件数据库
     * @param fileDataBase FileDataBase
     */
    fun setFileDataBase(fileDataBase: FileDataBase) {
        DebugHelper.printLog(debugKey, "已链接文件数据库，数据库状态:${fileDataBase.isOpen}。")
        this.fileDataBase = fileDataBase
    }

    /**
     * 是否为英文模式
     */
    private val isEnglishMode by lazy {
        AppSettings.getValue(AppSettings.Setting.EnglishEditingMode, false)
    }

    override fun getName(): String {
        return debugKey
    }


    override fun needPerform(
        contentReference: ContentReference,
        charPosition: CharPosition
    ): Boolean {
        return true
    }

    override fun respondingEmptyKeyword(
        contentReference: ContentReference,
        charPosition: CharPosition,
        completionPublisher: CompletionPublisher,
        lineData: String
    ) {
        val section = getSection(charPosition.getLine(), contentReference)
        if (section != null) {
            val trueSection = getSectionType(section)
            val list = codeDao.findCodeBySection(
                trueSection
            )
            list?.forEach { codeInfo ->
                completionPublisher.addItem(
                    CompletionItemConverter.codeInfoToCompletionItem(
                        codeInfo
                    )
                )
            }
            DebugHelper.printLog(
                debugKey,
                "${getName()}响应空白关键字，查询${trueSection}节，返回了${list?.size ?: -1}个内容。"
            )
        } else {
            DebugHelper.printLog(debugKey, "${getName()}响应空白关键字，无法获取节。")
        }
    }


    override fun requireAutoComplete(
        contentReference: ContentReference,
        charPosition: CharPosition,
        completionPublisher: CompletionPublisher,
        lineData: String,
        keyWord: String
    ) {
        if (codeDataBase == null) {
            DebugHelper.printLog(debugKey, "数据库为空，无法使用", isError = true)
            return
        }
        DebugHelper.printLog(
            debugKey,
            "行内容[" + lineData + "]关键字[" + keyWord + "]",
            "关联提示列表分析"
        )
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
                completionPublisher.addItem(CompletionItemConverter.sectionInfoToCompletionItem(it))
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
                                        CompletionItemConverter.localVariableNameToCompletionItem(
                                            it, previousText
                                        )
                                    )
                                }
                            }
                        } else {
                            RustAnalyzer.localVariableNameList.forEach {
                                completionPublisher.addItem(
                                    CompletionItemConverter.localVariableNameToCompletionItem(
                                        it, previousText
                                    )
                                )
                            }
                        }
//                        RustAutoComplete.keyWord = keyWord
                        return
                    }
                }

//                    val value =
//                        lineData.subSequence(keyIndex + key.length, lineData.length).toString().trim()
//                    LogCat.d("值", value)
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
                        codeDao.findCodeByCode(keyValue.toString())
                    } else {
                        codeDao.findCodeByTranslate(keyValue.toString())
                    }
                DebugHelper.printLog(
                    debugKey,
                    "值[" + keyValue + "]英文模式[" + isEnglishMode + "]代码信息[" + codeInfo + "]关键字[" + keyWord + "]",
                    "值检查"
                )
                if (codeInfo != null) {
                    val typeInfo = CompletionItemConverter.getValueType(codeInfo.type)
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
                                codeDao.findCodeByCode(lineData)
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
                                            keyWord,
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
                                                CompletionItemConverter.fileTableToCompletionItem(
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
                                            keyWord,
                                            customType,
                                            identifiersPromptNumber
                                        )
                                        ?.forEach {
                                            completionPublisher.addItem(
                                                CompletionItemConverter.valueTableToCompletionItem(
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
                                        codeDao.findCodeByCodeInType(
                                            keyWord,
                                            type,
                                            identifiersPromptNumber
                                        )
                                    } else {
                                        codeDao.findCodeByTranslateInType(
                                            keyWord,
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
                                                CompletionItemConverter.codeInfoToCompletionItem(
                                                    it
                                                )
                                            )
                                        }
                                    }
                                } else if (lineData.startsWith("@section") && lineData.endsWith(
                                        ")"
                                    )
                                ) {
//                                    val section = lineData.subSequence(
//                                        lineData.indexOf('(') + 1,
//                                        lineData.indexOf(')')
//                                    ).toString()
//                                        codeEditor!!.textAnalyzeResult.navigation.forEach {
//                                            if (section == getSectionType(it.label)) {
//                                                result.add(
//                                                    CompletionItemConverter.navigationItemToCompletionItem(
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
//                                                    CompletionItemConverter.associatedTip,
//                                                    codeInfo.translate
//                                                )
//                                            )
//                                        )
                                }
                            } else {
                                val show = if (isEnglishMode) {
                                    temCodeInfo.code.contains(keyWord)
                                } else {
                                    temCodeInfo.translate.contains(keyWord)
                                }
                                DebugHelper.printLog(
                                    debugKey,
                                    "值类型[" + codeInfo.type + "]项目[" + lineData + "]是否包含" + keyWord + "关键字[" + show + "]",
                                    "关联提示列表分析"
                                )
                                if (show) {
                                    completionPublisher.addItem(
                                        CompletionItemConverter.codeInfoToCompletionItem(
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
                val lineNumber = charPosition.getLine()
                val section = getSection(lineNumber, contentReference)
                //如果不在任何节内
                if (section == null) {
                    DebugHelper.printLog(debugKey, "不在任何节内，无法提示。", isError = true)
                    return
                }
                val trueSection = getSectionType(section)
                val list = if (isEnglishMode) {
                    codeDao.findCodeByEnglishCodeKeyFromSection(
                        keyWord,
                        trueSection,
                        identifiersPromptNumber
                    )
                } else {
                    codeDao
                        .findCodeByKeyFromSection(keyWord, trueSection, identifiersPromptNumber)
                }
                if (list != null && list.isNotEmpty()) {
                    list.forEach {
                        completionPublisher.addItem(
                            CompletionItemConverter.codeInfoToCompletionItem(it)
                        )
                    }
                } else {
                    DebugHelper.printLog(
                        debugKey,
                        "在${trueSection}节模糊搜索${keyWord}无结果。",
                        isError = true
                    )
                }
            }
        }
    }


    /**
     * 获取光标前的节名
     * @param lineNumber Int
     * @param contentReference ContentReference
     * @return String?
     */
    fun getSection(lineNumber: Int, contentReference: ContentReference): String? {
        return if (lineNumber > 0) {
            for (i in lineNumber downTo 0) {
                val lineData = contentReference.getLine(i)
                DebugHelper.printLog(debugKey, "检查第${i}行 ${lineData}", "获取光标前的节名")
                if (lineData.startsWith("[") && lineData.endsWith("]")) {
                    val name = lineData.subSequence(1, lineData.length - 1).toString()
                    DebugHelper.printLog(debugKey, "返回 ${name}", "获取光标前的节名")
                    return name
                }
            }
            DebugHelper.printLog(debugKey, "没有找到节。", "获取光标前的节名", isError = true)
            null
        } else {
            DebugHelper.printLog(debugKey, "行号小于0无法获取。", "获取光标前的节名", isError = true)
            null
        }
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

}