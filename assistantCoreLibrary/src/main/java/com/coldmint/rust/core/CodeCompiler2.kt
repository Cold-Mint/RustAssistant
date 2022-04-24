package com.coldmint.rust.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.coldmint.rust.core.dataBean.CompileConfiguration
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.SectionInfo
import com.coldmint.rust.core.database.code.ValueTypeInfo
import com.coldmint.rust.core.interfaces.*
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LineParser
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap
import kotlin.text.StringBuilder

/**
 * 代码编译器 2
 * @author Cold Mint
 * @date 2022/1/24 10:36
 */
class CodeCompiler2 private constructor(val context: Context) : CodeCompilerInterface, EnglishMode {


    /**
     * 英文模式
     */
    private var englishMode = false

    /**
     * 编译结果映射
     */
    private val compileMap by lazy {
        HashMap<String, String>()
    }

    /**
     * 节结果映射
     */
    private val sectionMap by lazy {
        HashMap<String, SectionInfo>()
    }

    /**
     * 值类型映射
     */
    private val valueTypeMap by lazy {
        HashMap<String, ValueTypeInfo>()
    }

    /**
     * 编译错误记录映射
     */
    private val errorRecordMap by lazy {
        HashMap<CompileConfiguration.CodeIndex, CompileConfiguration.ErrorRecord>()
    }
    private val executorService by lazy {
        Executors.newCachedThreadPool()
    }
    private val codeDataBase by lazy {
        CodeDataBase.getInstance(context)
    }
    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }


    companion object {
        const val split = "\n ,:()=%{}+*/\r"
        val debugKey = "代码编译器"

        @SuppressLint("StaticFieldLeak")
        private var instance: CodeCompiler2? = null

        /**
         * 获取实例对象
         * @param context Context
         * @return CodeCompiler2
         */
        fun getInstance(context: Context): CodeCompiler2 {
            if (instance == null) {
                synchronized(this)
                {
                    if (instance == null) {
                        instance = CodeCompiler2(context)
                    }
                }
            }
            return instance!!
        }

        /**
         * 格式化内容
         *
         * @param text 源文本
         * @return 格式化后的文本
         */
        @JvmStatic
        fun format(text: String): String {
            val result = StringBuilder()
            val lineParser = LineParser(text)
            lineParser.needTrim = true
            lineParser.analyse(object : LineParserEvent {
                override fun processingData(
                    lineNum: Int,
                    lineData: String,
                    isEnd: Boolean
                ): Boolean {
                    if (lineData.isNotBlank()) {
                        if (lineNum > 0 && result.isNotBlank()) {
                            if (lineData.startsWith("[") && lineData.endsWith("]")) {
                                result.append("\n")
                            }
                            result.append("\n")
                        }
                        val needSymbol = lineData.indexOf(':')
                        if (needSymbol > -1) {
                            val key = lineData.substring(0, needSymbol).trim { it <= ' ' }
                            val value =
                                lineData.substring(needSymbol + 1, lineData.length)
                                    .trim { it <= ' ' }
                            result.append(key)
                            result.append(":")
                            result.append(value)
                        } else {
                            result.append(lineData)
                        }
                    }
                    return true
                }

            })
            return result.toString()
        }
    }

    /**
     * 清理缓存
     */
    fun clearCache() {
        compileMap.clear()
        errorRecordMap.clear()
        valueTypeMap.clear()
    }

    /**
     * 翻译活动
     * @param code String
     * @param translatorListener CodeTranslatorListener
     */
    private fun translationWork(code: String, translatorListener: CodeTranslatorListener) {
        if (englishMode) {
            handler.post {
                translatorListener.beforeTranslate()
                translatorListener.onTranslateComplete(code)
            }
        } else {
            val tokenizer = StringTokenizer(code, split, true)
            //缓存翻译数据，以便加速重复数据的翻译
            val translationMap = HashMap<String, String>()
            //保存每次代码的翻译结果
            val codeResult = StringBuilder()
            //保存完整的翻译结果
            val translationResult = StringBuilder()
            var codeBlockType = CompileConfiguration.CodeBlockType.Key
            //保存资源引用值（应该看做整体处理）
            val referenceResult = StringBuilder()
            handler.post {
                translatorListener.beforeTranslate()
            }
            while (tokenizer.hasMoreTokens()) {
                val code = tokenizer.nextToken()
                if (translationMap.containsKey(code)) {
                    translationResult.append(translationMap[code])
                } else {
                    codeResult.clear()
                    when (code) {
                        "\n" -> {
                            if (codeBlockType == CompileConfiguration.CodeBlockType.Reference) {
                                val referenceValue = referenceResult.toString()
                                val codeInfo =
                                    codeDataBase.getCodeDao().findCodeByCode(referenceValue)
                                if (codeInfo == null) {
                                    translationResult.append(referenceResult)
                                } else {
                                    translationResult.append(codeInfo.translate)
                                }
                                referenceResult.clear()
                            }
                            codeBlockType = CompileConfiguration.CodeBlockType.Key
                            codeResult.append(code)
                        }
                        "\r" -> {
                        }
                        " ", ",", "(", ")", "=", "%", "{", "}", "+", "*", "/" -> {
                            if (codeBlockType == CompileConfiguration.CodeBlockType.Reference) {
                                referenceResult.append(code)
                            } else {
                                codeResult.append(
                                    code
                                )
                            }
                        }
                        ":" -> {
                            if (codeBlockType == CompileConfiguration.CodeBlockType.Reference) {
                                referenceResult.append(code)
                            } else {
                                if (codeBlockType == CompileConfiguration.CodeBlockType.Key) {
                                    codeBlockType =
                                        CompileConfiguration.CodeBlockType.Value
                                }
                                codeResult.append(code)
                            }
                        }
                        else -> if (codeBlockType == CompileConfiguration.CodeBlockType.Note) {
                            codeResult.append(code)
                        } else if (codeBlockType == CompileConfiguration.CodeBlockType.Reference) {
                            //资源引用值应该被整体处理
                            referenceResult.append(code)
                        } else {
                            if (code.startsWith("#")) {
                                codeBlockType = CompileConfiguration.CodeBlockType.Note
                                codeResult.append(code)
                            } else if (code.startsWith("[") && code.endsWith("]")) {
                                val symbolPosition = code.lastIndexOf("_")
                                if (symbolPosition > 0) {
                                    val sectionPrefixName = code.substring(1, symbolPosition)
                                    codeResult.append("[")
                                    val info = codeDataBase.getSectionDao()
                                        .findSectionInfoByCode(sectionPrefixName)
                                    codeResult.append(
                                        info?.translate ?: sectionPrefixName
                                    )
                                    codeResult.append("_")
                                    codeResult.append(code.substring(symbolPosition + 1))
                                } else {
                                    val sectionCode = code.substring(1, code.length - 1)
                                    codeResult.append("[")
                                    val info =
                                        codeDataBase.getSectionDao()
                                            .findSectionInfoByCode(sectionCode)
                                    codeResult.append(
                                        info?.translate ?: sectionCode
                                    )
                                    codeResult.append("]")
                                }
                            } else {
                                val codeInfo = codeDataBase.getCodeDao().findCodeByCode(code)
                                if (codeInfo == null) {
                                    if (code.contains("_")) {
                                        val lineParser = LineParser(code)
                                        lineParser.symbol = "_"
                                        lineParser.analyse(object : LineParserEvent {
                                            override fun processingData(
                                                lineNum: Int,
                                                lineData: String,
                                                isEnd: Boolean
                                            ): Boolean {
                                                val temCodeInfo =
                                                    codeDataBase.getCodeDao()
                                                        .findCodeByCode(lineData.trim())
                                                if (temCodeInfo == null) {
                                                    codeResult.append(lineData)
                                                } else {
                                                    codeResult.append(temCodeInfo.translate)
                                                }
                                                if (!isEnd) {
                                                    codeResult.append(lineParser.symbol)
                                                }
                                                return true
                                            }
                                        })
                                    } else {
                                        codeResult.append(code)
                                    }
                                } else {
                                    //是否需要检查值
                                    if (codeBlockType == CompileConfiguration.CodeBlockType.Key) {
                                        val type = getValueData(codeInfo.type)
                                        val tag = type?.tag
                                        if (!tag.isNullOrBlank()) {
                                            //如果此类型为特殊标注，那么设置为注释
                                            codeBlockType =
                                                CompileConfiguration.CodeBlockType.Reference
                                        }
                                    }
                                    codeResult.append(codeInfo.translate)
                                }
                            }
                        }
                    }
                    //如果代码不是注释，也不是换行，不是冒号，那么缓存它。
                    if (codeBlockType != CompileConfiguration.CodeBlockType.Note && code != ":" && code != "\n") {
                        translationMap[code] = codeResult.toString()
                    }
                    translationResult.append(codeResult.toString())
                    DebugHelper.printLog(
                        debugKey,
                        "代码[" + code + "]译文[" + codeResult.toString() + "]是否翻译[" + (code != codeResult.toString()) + "]",
                        "翻译"
                    )
                }
            }
            handler.post {
                translatorListener.onTranslateComplete(translationResult.toString())
            }
        }
    }


    /**
     * 编译活动
     * @param sourceCode String 代码
     * @param compileConfiguration CompileConfiguration 编译配置
     * @param compilerListener CodeCompilerListener? 编译监听器
     */
    private fun compileWork(
        sourceCode: String,
        compileConfiguration: CompileConfiguration,
        compilerListener: CodeCompilerListener? = null
    ) {
        compileConfiguration.setErrorRecordMap(errorRecordMap)
        //添加换行符以便检测
        val finalSourceCode = sourceCode + "\n"
        val tokenizer = StringTokenizer(finalSourceCode, split, true)
        //保存每段代码的翻译结果
        val codeResult = StringBuilder()
        //保存完整的翻译结果
        val compileResult = StringBuilder()
        //是否为首次引用冒号值(首次值会直接附加到结果集合内)
        var isFirstReference = false

        //保存资源引用值（应该看做整体处理）
        val referenceResult = StringBuilder()
        val context = compileConfiguration.context
        handler.post {
            compilerListener?.beforeCompilation()
        }
        val startTime = System.currentTimeMillis()
        while (tokenizer.hasMoreTokens()) {
            val translation = tokenizer.nextToken()
            codeResult.clear()
            if (compileMap.containsKey(translation)) {
                //此处仅读取字段缓存，不涉及错误缓存的读取
                val code = compileMap[translation]
                if (code != null) {
                    if (code.startsWith("[") && code.endsWith("]")) {
                        analysisSection(
                            code,
                            codeResult,
                            compileConfiguration,
                            compilerListener, false
                        )
                    } else {
                        //是代码，添加到代码段结果集
                        compileConfiguration.appendResult(code)
                        codeResult.append(code)
                    }
                }
            } else {
                when (translation) {
                    "\n" -> {
                        if (compileConfiguration.codeBlockType == CompileConfiguration.CodeBlockType.Reference) {
                            //此段代码将临时引用数据referenceValue 附加到编译结果内，并附加到 值 数据内（附加值数据用于检查）
                            val referenceValue = referenceResult.toString()
                            compileConfiguration.appendValue(referenceResult.toString())
                            val codeInfo = codeDataBase.getCodeDao()
                                .findCodeByTranslate(referenceResult.toString())
                            if (codeInfo == null) {
                                compileResult.append(referenceValue)
                            } else {
                                compileResult.append(codeInfo.code)
                            }
                            DebugHelper.printLog(
                                debugKey,
                                "引用数据[" + referenceValue + "]代码信息存在状态[" + (codeInfo != null) + "]",
                                "行引用附加"
                            )
                            referenceResult.clear()
                        }
                        isFirstReference = true
                        checkLineCode(compileConfiguration, compilerListener)
                        compileConfiguration.nextLine()
                        codeResult.append(translation)
                    }
                    "\r" -> {
                    }
                    " ", ",", "(", ")", "=", "%", "{", "}", "+", "*", "/" -> {
                        if (compileConfiguration.codeBlockType == CompileConfiguration.CodeBlockType.Reference) {
                            referenceResult.append(translation)
                        } else {
                            codeResult.append(
                                translation
                            )
                            compileConfiguration.appendResult(translation)
                        }
                    }
                    ":" -> {
                        when (compileConfiguration.codeBlockType) {
                            CompileConfiguration.CodeBlockType.Value -> {
                                compileConfiguration.appendResult(translation)
                                codeResult.append(translation)
                            }
                            CompileConfiguration.CodeBlockType.Key -> {
                                compileConfiguration.codeBlockType =
                                    CompileConfiguration.CodeBlockType.Value
                                codeResult.append(translation)
                            }
                            CompileConfiguration.CodeBlockType.Reference -> {
                                if (isFirstReference) {
                                    codeResult.append(translation)
                                    isFirstReference = false
                                } else {
                                    referenceResult.append(translation)
                                }
                            }
                        }
                    }
                    else -> if (compileConfiguration.codeBlockType == CompileConfiguration.CodeBlockType.Note) {
                        codeResult.append(translation)
                    } else if (compileConfiguration.codeBlockType == CompileConfiguration.CodeBlockType.Reference) {
                        //资源引用值应该被整体处理
                        referenceResult.append(translation)
                    } else {
                        if (translation.startsWith("#") && compileConfiguration.codeBlockType ==
                            CompileConfiguration.CodeBlockType.Key
                        ) {
                            compileConfiguration.codeBlockType =
                                CompileConfiguration.CodeBlockType.Note
                            codeResult.append(translation)
                        } else if (translation.startsWith("[") && translation.endsWith("]")) {
                            analysisSection(
                                translation,
                                codeResult,
                                compileConfiguration,
                                compilerListener
                            )
                        } else {
                            //为代码时
                            val codeInfo =
                                codeDataBase.getCodeDao().findCodeByTranslate(translation)
                            if (codeInfo == null) {
                                if (translation.contains("_")) {
                                    val lineParser = LineParser(translation)
                                    lineParser.symbol = "_"
                                    lineParser.analyse(object : LineParserEvent {
                                        override fun processingData(
                                            lineNum: Int,
                                            lineData: String,
                                            isEnd: Boolean
                                        ): Boolean {
                                            val temCodeInfo =
                                                codeDataBase.getCodeDao()
                                                    .findCodeByTranslate(lineData.trim())
                                            if (temCodeInfo == null) {
                                                codeResult.append(lineData)
                                            } else {
                                                codeResult.append(temCodeInfo.code)
                                            }
                                            if (!isEnd) {
                                                codeResult.append(lineParser.symbol)
                                            }
                                            return true
                                        }
                                    })
                                } else {
                                    codeResult.append(translation)
                                }
                            } else {
                                //是否需要检查值
                                if (compileConfiguration.codeBlockType == CompileConfiguration.CodeBlockType.Key) {
                                    val type = getValueData(codeInfo.type)
                                    val tag = type?.tag
                                    if (!tag.isNullOrBlank()) {
                                        //如果此类型为特殊标注，那么设置为注释
                                        compileConfiguration.codeBlockType =
                                            CompileConfiguration.CodeBlockType.Reference
                                    }
                                }
                                codeResult.append(codeInfo.code)
                            }
                            //翻译代码段完毕后，加入行结果集合
                            compileConfiguration.appendResult(codeResult.toString())
                        }
                    }
                }
                //如果代码不是注释，也不是换行，也不是冒号，那么缓存它。
                if (compileConfiguration.codeBlockType != CompileConfiguration.CodeBlockType.Note && translation != "\n" && translation != ":") {
                    compileMap[translation] = codeResult.toString()
                }
            }
            //保存单次编译结果
            compileResult.append(codeResult.toString())
            compileConfiguration.addColumn(translation)
        }
        val time = System.currentTimeMillis() - startTime
        val tip = String.format(
            context.getString(R.string.compilation_result_tip),
            compileConfiguration.getErrorNumber(),
            compileConfiguration.getWarningNumber(),
            time
        )
        compileConfiguration.addInfo(tip)
        handler.post {
            compilerListener?.onCompilationComplete(
                compileConfiguration,
                compileResult.substring(0, compileResult.length - 1).toString()
            )
        }
    }


    /**
     * 检查行数据，此函数不修改结果集。
     * @param compileConfiguration CompileConfiguration 代码配置
     * @param compilerListener CodeCompilerListener? 代码监听器
     */
    fun checkLineCode(
        compileConfiguration: CompileConfiguration,
        compilerListener: CodeCompilerListener? = null
    ) {
        DebugHelper.printLog(
            debugKey,
            "键[" + compileConfiguration.getKey() + "]值[" + compileConfiguration.getValue() + "]",
            "行处理"
        )
        compileConfiguration.setCanAddError(true)
        val key = compileConfiguration.getKey()
        //设置了监听器并且key不为空
        if (key.isNotBlank() && compilerListener != null) {
            val value = compileConfiguration.getValue()
            if (value.isBlank()) {
                //缺少值(此处不会读取缓存)
                val codeInfo = codeDataBase.getCodeDao().findCodeByCode(key)
                val codeIndex = compileConfiguration.createCodeIndex(key)
                compileConfiguration.addError(
                    codeIndex, CompileConfiguration.ErrorRecord(
                        String.format(
                            context.getString(R.string.compiler_error10),
                            codeInfo?.translate
                                ?: key
                        ), errorType = CompileConfiguration.ErrorType.Error, function = {
                        }
                    )
                )
            } else {
                //生成代码位置
                val codeIndex = compileConfiguration.createCodeIndex("$key:$value")
                //读取缓存
                val errorRecord = errorRecordMap[codeIndex]
                if (errorRecord == null) {
                    //没有错误缓存，检测代码
                    //key必为英文
                    val codeInfo = codeDataBase.getCodeDao().findCodeByCode(key)
                    if (codeInfo == null) {
                        //代码没有录入
                        val finalLineNum = compileConfiguration.getLineNum()
                        val finalColumnNum = compileConfiguration.getColumnNum() - 1
                        compileConfiguration.addError(
                            codeIndex,
                            CompileConfiguration.ErrorRecord(
                                String.format(
                                    context.getString(R.string.compiler_error9),
                                    key
                                ), function = {
                                    compilerListener.onClickKeyNotFoundItem(
                                        finalLineNum, finalColumnNum,
                                        it,
                                        key,
                                        compileConfiguration.lastSection ?: ""
                                    )
                                }
                            )
                        )
                    } else {
                        //判断节位置
                        val section = codeInfo.section
                        //lastSection绝对为英文
                        val lastSection = compileConfiguration.lastSection
                        if (lastSection == null) {
                            //不存在于任何节内
                            compileConfiguration.addError(
                                codeIndex,
                                CompileConfiguration.ErrorRecord(
                                    String.format(
                                        context.getString(R.string.compiler_error11),
                                        codeInfo.translate
                                    ), errorType = CompileConfiguration.ErrorType.Error
                                )
                            )
                            compileConfiguration.setCanAddError(false)
                            return
                        }

                        //如果节不包含的话
                        if (!section.contains(lastSection)) {
                            val finalLine = compileConfiguration.getLineNum()
                            val finalColumnNum = compileConfiguration.getColumnNum() - 1
                            compileConfiguration.addError(
                                codeIndex,
                                CompileConfiguration.ErrorRecord(
                                    String.format(
                                        context.getString(R.string.compiler_error6),
                                        codeInfo.translate, sectionListToTranslate(section)
                                    ),
                                    errorType = CompileConfiguration.ErrorType.Warning,
                                    verifyFunction = {
                                        if (it.lastSection == null) {
                                            true
                                        } else {
                                            //如果数据内不包含last则显示
                                            !section.contains(it.lastSection!!)
                                        }
                                    },
                                    function = {
                                        compilerListener.onClickSectionIndexError(
                                            finalLine, finalColumnNum,
                                            it,
                                            section
                                        )
                                    }
                                )
                            )
                            compileConfiguration.setCanAddError(false)
                            return
                        }
                        //判断值类型是否正确,值绝对为英文
                        val valueTypeInfo = getValueData(codeInfo.type)
                        if (valueTypeInfo != null && !value.matches(Regex(valueTypeInfo.rule))) {
                            //不满足规则
                            val finalLineNum = compileConfiguration.getLineNum()
                            val finalColumnNum = compileConfiguration.getColumnNum() - 1
                            compileConfiguration.addError(
                                codeIndex,
                                CompileConfiguration.ErrorRecord(
                                    String.format(
                                        context.getString(R.string.compiler_error1),
                                        value,
                                        valueTypeInfo.name
                                    ),
                                    errorType = CompileConfiguration.ErrorType.Error,
                                    function = {
                                        compilerListener.onClickValueTypeErrorItem(
                                            finalLineNum,
                                            finalColumnNum,
                                            it,
                                            valueTypeInfo
                                        )
                                    }
                                )
                            )
                        }

                        //判断关联内容
                        //如果是Tag标记
                        if (valueTypeInfo != null && valueTypeInfo.tag.startsWith("@file") && value != "AUTO" && value != "NONE") {
                            //检查附加描述
                            val root = "ROOT:"
                            //检查目录
                            val sourceFileFolder =
                                FileOperator.getSuperDirectory(compileConfiguration.openedSourceFile.file)
                            var apkFolder: String? = null
                            //文件类型
                            var type = ""
                            //是否仅检查此格式（默认情况apk文件不存在，检查源文件目录）此变量为真则仅检查apk内部
                            var only = false
                            val startIndex = valueTypeInfo.tag.indexOf('(')
                            if (startIndex > -1) {
                                val endIndex = valueTypeInfo.tag.indexOf(')')
                                if (endIndex > -1) {
                                    val targetValue =
                                        valueTypeInfo.tag.subSequence(startIndex + 1, endIndex)
                                    val apkTag = "apk{"
                                    val typeTag = "type{"
                                    val onlyType = "only{"
                                    val apkIndex = targetValue.indexOf(apkTag)
                                    val onlyIndex = targetValue.indexOf(onlyType)
                                    val typeIndex = targetValue.indexOf(typeTag)
                                    if (apkIndex > -1) {
                                        //指名为apk目录
                                        val endIndex = targetValue.indexOf('}', apkIndex)
                                        if (endIndex > -1) {
                                            val tag = targetValue.subSequence(
                                                apkIndex + apkTag.length,
                                                endIndex
                                            ).toString()
                                            val finalLineNum = compileConfiguration.getLineNum()
                                            val finalColumnNum =
                                                compileConfiguration.getColumnNum() - 1
                                            //如果引用了apk内部文件但是，没有同步数据那么抛出异常
                                            if (!compileConfiguration.apkFolder.exists()) {
                                                compileConfiguration.addError(
                                                    codeIndex, CompileConfiguration.ErrorRecord(
                                                        compileConfiguration.context.getString(R.string.compiler_error12),
                                                        verifyFunction = {
                                                            !it.apkFolder.exists()
                                                        }, function = {
                                                            compilerListener.onClickSynchronizationGame(
                                                                finalLineNum,
                                                                finalColumnNum,
                                                                it
                                                            )
                                                        })
                                                )
                                                return
                                            }
                                            apkFolder =
                                                compileConfiguration.apkFolder.absolutePath + "/" + tag
                                        }
                                    }
                                    if (typeIndex > -1) {
                                        //指定文件格式
                                        val endIndex = targetValue.indexOf('}', typeIndex)
                                        if (endIndex > -1) {
                                            val tag = targetValue.subSequence(
                                                typeIndex + typeTag.length,
                                                endIndex
                                            ).toString()
                                            type = tag
                                        }
                                    }
                                    if (onlyIndex > -1) {
                                        //指定是否仅检查
                                        val endIndex = targetValue.indexOf('}', onlyIndex)
                                        if (endIndex > -1) {
                                            val tag = targetValue.subSequence(
                                                onlyIndex + onlyType.length,
                                                endIndex
                                            ).toString()
                                            if (tag.lowercase(Locale.getDefault()) == "true") {
                                                only = true
                                            }
                                        }
                                    }
                                }
                            }
                            if (value.startsWith(root)) {
                                //文件检查Root
                                val finalLineNum = compileConfiguration.getLineNum()
                                val finalColumnNum = compileConfiguration.getColumnNum() - 1
                                val path = value.subSequence(root.length, value.length)
                                val file =
                                    File(compileConfiguration.modClass.modFile.absolutePath + "/" + path)
                                if (!file.exists()) {
                                    //抛出文件不存在错误
                                    compileConfiguration.addError(
                                        codeIndex, CompileConfiguration.ErrorRecord(
                                            String.format(
                                                compileConfiguration.context.getString(R.string.compiler_error3),
                                                file.name,
                                                file.absolutePath
                                            ),
                                            errorType = CompileConfiguration.ErrorType.Error,
                                            function = {
                                                compilerListener.onClickResourceErrorItem(
                                                    finalLineNum,
                                                    finalColumnNum,
                                                    it,
                                                    file
                                                )
                                            }
                                        )
                                    )
                                }
                            } else {
                                //目录检查
                                if (apkFolder == null) {
                                    val file = getTargetFile(sourceFileFolder, value, type)
                                    if (!file.exists()) {
                                        //抛出文件不存在错误
                                        val finalLineNum = compileConfiguration.getLineNum()
                                        val finalColumnNum = compileConfiguration.getColumnNum() - 1
                                        compileConfiguration.addError(
                                            codeIndex, CompileConfiguration.ErrorRecord(
                                                String.format(
                                                    compileConfiguration.context.getString(R.string.compiler_error3),
                                                    file.name,
                                                    file.absolutePath
                                                ),
                                                errorType = CompileConfiguration.ErrorType.Error,
                                                function = {
                                                    compilerListener.onClickResourceErrorItem(
                                                        finalLineNum,
                                                        finalColumnNum,
                                                        it,
                                                        file
                                                    )
                                                }
                                            )
                                        )
                                    }
                                } else {
                                    val file = getTargetFile(apkFolder, value, type)
                                    if (!file.exists()) {
                                        if (only) {
                                            //抛出文件不存在错误
                                            val finalLineNum = compileConfiguration.getLineNum()
                                            val finalColumnNum =
                                                compileConfiguration.getColumnNum() - 1
                                            compileConfiguration.addError(
                                                codeIndex, CompileConfiguration.ErrorRecord(
                                                    String.format(
                                                        compileConfiguration.context.getString(R.string.compiler_error3),
                                                        file.name,
                                                        file.absolutePath
                                                    ),
                                                    errorType = CompileConfiguration.ErrorType.Error,
                                                    function = {
                                                        compilerListener.onClickResourceErrorItem(
                                                            finalLineNum,
                                                            finalColumnNum,
                                                            it,
                                                            file
                                                        )
                                                    }
                                                )
                                            )
                                        } else {
                                            val file2 = getTargetFile(sourceFileFolder, value, type)
                                            if (!file2.exists()) {
                                                //抛出文件不存在错误
                                                val finalLineNum = compileConfiguration.getLineNum()
                                                val finalColumnNum =
                                                    compileConfiguration.getColumnNum() - 1
                                                compileConfiguration.addError(
                                                    codeIndex, CompileConfiguration.ErrorRecord(
                                                        String.format(
                                                            compileConfiguration.context.getString(R.string.compiler_error3),
                                                            file2.name,
                                                            file2.absolutePath
                                                        ),
                                                        errorType = CompileConfiguration.ErrorType.Error,
                                                        function = {
                                                            compilerListener.onClickResourceErrorItem(
                                                                finalLineNum,
                                                                finalColumnNum,
                                                                it,
                                                                file
                                                            )
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    //如果有错误记录，则填入
                    if (errorRecord.verifyFunction == null) {
                        compileConfiguration.addError(codeIndex, errorRecord)
                    } else {
                        if (errorRecord.verifyFunction!!.invoke(compileConfiguration)) {
                            compileConfiguration.addError(codeIndex, errorRecord)
                        }
                    }
                }
            }
        }
        compileConfiguration.setCanAddError(false)
    }

    /**
     * 检查文件方法
     * @param folder String
     * @param value String
     * @param type String?
     * @return 文件是否存在
     */
    fun getTargetFile(folder: String, value: String, type: String? = null): File {
        if (type != null) {
            //检查是否需要添加类型符
            val lineParser = LineParser(type)
            lineParser.symbol = ","
            //默认文件
            var result = File(folder + "/" + value)
            lineParser.analyse { lineNum, lineData, isEnd ->
                if (value.endsWith(lineData)) {
                    //不需要(确定了类型)
                    val file = File(folder + "/" + value)
                    result = file
                    return@analyse false
                } else {
                    val file = File(folder + "/" + value + "." + lineData)
                    result = file
                    //如果文件不存在继续循环
                    return@analyse !result.exists()
                }
            }
            return result
        } else {
            val file = File(folder + "/" + value)
            return file
        }
    }

    /**
     * 将SectionList转化为翻译
     * @param sectionList String
     * @return String
     */
    fun sectionListToTranslate(sectionList: String): String {
        val lineParser = LineParser(sectionList)
        val resultBuilder = StringBuilder()
        lineParser.symbol = ","
        lineParser.parserSymbol = true
        lineParser.analyse { lineNum, lineData, isEnd ->
            if (lineData == lineParser.symbol) {
                resultBuilder.append(lineData)
                return@analyse true
            }
            //检查是否有缓存
            if (sectionMap.containsKey(lineData)) {
                val section = sectionMap[lineData]
                resultBuilder.append(section?.translate ?: lineData)
            } else {
                val section = codeDataBase.getSectionDao().findSectionInfoByCode(lineData)
                if (section != null) {
                    sectionMap[lineData] = section
                    resultBuilder.append(section.translate)
                }
            }
            true
        }
        return resultBuilder.toString()
    }

    /**
     * 获取值类型数据
     * @param type String
     * @return ValueTypeInfo
     */
    private fun getValueData(type: String): ValueTypeInfo? {
        return if (valueTypeMap.containsKey(type)) {
            valueTypeMap[type]!!
        } else {
            val typeInfo = codeDataBase.getValueTypeDao().findTypeByType(type)
            if (typeInfo != null) {
                valueTypeMap[type] = typeInfo

            }
            typeInfo
        }
    }

    /**
     * 分析节数据
     * @param section String 节 可以是英文也可以是翻译，默认为翻译
     * @param codeResult StringBuilder
     * @param compileConfiguration CompileConfiguration
     * @param compilerListener CodeCompilerListener?
     * @param needCompile Boolean 是否需要编译默认为真
     */
    private fun analysisSection(
        section: String,
        codeResult: StringBuilder,
        compileConfiguration: CompileConfiguration,
        compilerListener: CodeCompilerListener? = null,
        needCompile: Boolean = true
    ) {
        compileConfiguration.setCanAddError(true)
        //节
        compileConfiguration.codeBlockType = CompileConfiguration.CodeBlockType.Section
        val symbolPosition = section.lastIndexOf("_")
        if (symbolPosition > 0) {
            val sectionPrefixName = section.substring(1, symbolPosition)
            codeResult.append("[")
            if (needCompile) {
                val info = if (englishMode) {
                    codeDataBase.getSectionDao()
                        .findSectionInfoByCode(sectionPrefixName)
                } else {
                    codeDataBase.getSectionDao()
                        .findSectionInfoByTranslate(sectionPrefixName)
                }
                //判断是否应该有附加名(当监听器不为空时)
                if (compilerListener != null) {
                    //不需要附加名抛出异常
                    if (info == null) {
                        compileConfiguration.addError(
                            compileConfiguration.createCodeIndex(section),
                            CompileConfiguration.ErrorRecord(
                                String.format(
                                    context.getString(R.string.section_not_find_error),
                                    section
                                )
                            )
                        )
                    } else {
                        if (!info.needName) {
                            val finalLineNum = compileConfiguration.getLineNum()
                            val finalColumnNum = section.length - 1
                            compileConfiguration.addError(compileConfiguration.createCodeIndex(
                                section
                            ),
                                CompileConfiguration.ErrorRecord(
                                    String.format(
                                        context.getString(R.string.need_name_error1),
                                        section
                                    ), {
                                        compilerListener.onClickSectionNameErrorItem(
                                            finalLineNum, finalColumnNum,
                                            it,
                                            section,
                                            symbolPosition, false
                                        )
                                    }
                                )
                            )
                        }
                    }
                }
                compileConfiguration.lastSection = info?.code ?: sectionPrefixName
                codeResult.append(
                    info?.code ?: sectionPrefixName
                )
            } else {
                //无需编译
                val oldError = errorRecordMap[compileConfiguration.createCodeIndex(section)]
                if (oldError != null) {
                    compileConfiguration.addError(
                        compileConfiguration.createCodeIndex(section),
                        oldError
                    )
                }
                compileConfiguration.lastSection = sectionPrefixName
                codeResult.append(sectionPrefixName)
            }
            codeResult.append("_")
            codeResult.append(section.substring(symbolPosition + 1))
        } else {
            val sectionCode = section.substring(1, section.length - 1)
            codeResult.append("[")
            if (needCompile) {
                val info = if (englishMode) {
                    codeDataBase.getSectionDao()
                        .findSectionInfoByCode(sectionCode)
                } else {
                    codeDataBase.getSectionDao()
                        .findSectionInfoByTranslate(sectionCode)
                }
                //判断是否应该有附加名(当监听器不为空时)
                if (compilerListener != null) {
                    if (info == null) {
                        compileConfiguration.addError(
                            compileConfiguration.createCodeIndex(section),
                            CompileConfiguration.ErrorRecord(
                                String.format(
                                    context.getString(R.string.section_not_find_error),
                                    section
                                )
                            )
                        )
                    } else {
                        if (info.needName) {
                            //需要附加名抛出异常
                            val finalLineNum = compileConfiguration.getLineNum()
                            val finalColumnNum = section.length - 1
                            compileConfiguration.addError(
                                compileConfiguration.createCodeIndex(section),
                                CompileConfiguration.ErrorRecord(String.format(
                                    context.getString(R.string.need_name_error2),
                                    section
                                ), {
                                    compilerListener.onClickSectionNameErrorItem(
                                        finalLineNum, finalColumnNum,
                                        it,
                                        section, null, true
                                    )
                                })
                            )
                        }
                    }
                }
                compileConfiguration.lastSection = info?.code ?: sectionCode
                codeResult.append(
                    info?.code ?: sectionCode
                )
            } else {
                val oldError = errorRecordMap[compileConfiguration.createCodeIndex(section)]
                if (oldError != null) {
                    compileConfiguration.addError(
                        compileConfiguration.createCodeIndex(section),
                        oldError
                    )
                }
                compileConfiguration.lastSection = sectionCode
                codeResult.append(sectionCode)
            }
            codeResult.append("]")
        }
        compileConfiguration.setCanAddError(false)
    }

    /**
     * 是否为主线程
     */
    private fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    /**
     * 翻译
     * @param code String 代码
     * @param translatorListener CodeTranslatorListener 代码翻译监听器
     */
    override fun translation(code: String, translatorListener: CodeTranslatorListener) {
        if (isMainThread()) {
            executorService.submit {
                translationWork(code, translatorListener)
            }
        } else {
            translationWork(code, translatorListener)
        }
    }

    /**
     * 编译方法
     * @param code String 代码
     * @param compileConfiguration CompileConfiguration 配置
     * @param compilerListener CodeCompilerListener? 监听器
     */
    override fun compile(
        code: String,
        compileConfiguration: CompileConfiguration,
        compilerListener: CodeCompilerListener?
    ) {
        if (isMainThread()) {
            executorService.submit {
                compileWork(code, compileConfiguration, compilerListener)
            }
        } else {
            compileWork(code, compileConfiguration, compilerListener)
        }
    }

    override fun isEnglishMode(): Boolean {
        return englishMode
    }

    override fun setEnglish(englishMode: Boolean) {
        this.englishMode = englishMode
    }

}