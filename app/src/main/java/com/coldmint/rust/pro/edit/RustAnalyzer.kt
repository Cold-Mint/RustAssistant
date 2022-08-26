package com.coldmint.rust.pro.edit

import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.CodeInfo
import com.coldmint.rust.core.interfaces.EnglishMode
import com.coldmint.rust.core.tool.LocalVariableName
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@Deprecated("已过时")
class RustAnalyzer() : EnglishMode {
//    private val labels: ArrayList<NavigationItem> by lazy {
//        ArrayList()
//    }

    companion object {
        /**
         * 本地变量名的存放
         */
        val localVariableNameList: HashSet<LocalVariableName> by lazy {
            HashSet()
        }
    }

    private var englishMode = false
    private var codeDataBase: CodeDataBase? = null
    private var containsHashMap = HashMap<String, Boolean>()
    private var validHashMap = HashMap<String, Boolean>()
    private var trueHashMap = HashMap<String, String>()

    /**
     * 设置代码数据库
     * @param codeDataBase CodeDataBase
     */
    fun setCodeDataBase(codeDataBase: CodeDataBase) {
        this.codeDataBase = codeDataBase
    }



    /**
//     * 语法分析器
//     * @param content CharSequence
//     * @param colors TextAnalyzeResult
//     * @param delegate Delegate
//     */
//    override fun analyze(
//        content: CharSequence,
//        colors: TextAnalyzeResult,
//        delegate: TextAnalyzer.AnalyzeThread.Delegate
//    ) {
//        //添加换行符作为结束符
//        val str = content.toString() + "\n"
//        if (str.isBlank() || codeDataBase == null) {
//            return
//        }
//        val lineParser = LineParser()
//        lineParser.parserSymbol = true
//        lineParser.symbol = "_"
//        colors.addNormalIfNull()
//        if (delegate.shouldAnalyze()) {
//            labels.clear()
//            localVariableNameList.clear()
//            var codeBlockType: CompileConfiguration.CodeBlockType =
//                CompileConfiguration.CodeBlockType.Key
//            var lineNum = 0
//            val keyBuilder = StringBuilder()
//            val valueBuilder = StringBuilder()
//            var column = 0
//            var startIndex = -1
//            //是否执行二次渲染，用于判断值是否合法
//            var lineRendering = true
//            var lastSection: String? = null
//            val stringTokenizer = StringTokenizer(str, CodeCompiler2.split, true)
//            while (stringTokenizer.hasMoreTokens()) {
//                val code = stringTokenizer.nextToken()
//                when (code) {
//                    "\n" -> {
//                        if (codeBlockType != CompileConfiguration.CodeBlockType.Note && codeBlockType != CompileConfiguration.CodeBlockType.Section && lineRendering) {
//                            renderLineContent(
//                                startIndex,
//                                lastSection,
//                                colors,
//                                lineNum,
//                                keyBuilder,
//                                valueBuilder
//                            )
//                        }
//                        lineNum++
//                        column = 0
//                        lineRendering = true
//                        startIndex = -1
//                        codeBlockType = CompileConfiguration.CodeBlockType.Key
//                        keyBuilder.clear()
//                        valueBuilder.clear()
//                    }
//                    " ", ",", "/" -> if (codeBlockType != CompileConfiguration.CodeBlockType.Note) {
//                        if (codeBlockType == CompileConfiguration.CodeBlockType.Value) {
//                            valueBuilder.append(code)
//                        }
//                        column += code.length
//                    }
//                    ":" -> {
//                        if (lastSection != null && codeBlockType != CompileConfiguration.CodeBlockType.Note) {
//                            codeBlockType = CompileConfiguration.CodeBlockType.Value
//                            colors.addIfNeeded(lineNum, column, EditorColorScheme.TEXT_NORMAL)
//                            column += code.length
//                        }
//                    }
//                    "\r" -> {
//                    }
//                    "(", ")", "=", "%", "." -> if (codeBlockType != CompileConfiguration.CodeBlockType.Note) {
//                        if (codeBlockType == CompileConfiguration.CodeBlockType.Value || codeBlockType == CompileConfiguration.CodeBlockType.VariableName) {
//                            valueBuilder.append(code)
//                        }
//                        column += code.length
//                    }
//                    "}" -> {
//                        if (codeBlockType != CompileConfiguration.CodeBlockType.Note) {
//                            if (codeBlockType == CompileConfiguration.CodeBlockType.VariableName) {
//                                colors.addIfNeeded(
//                                    lineNum,
//                                    column,
//                                    EditorColorScheme.TEXT_NORMAL
//                                )
//                                codeBlockType = CompileConfiguration.CodeBlockType.Value
//                                valueBuilder.append(code)
//                            }
//                            column += code.length
//                        }
//                    }
//                    "{" -> {
//                        if (codeBlockType != CompileConfiguration.CodeBlockType.Note) {
//                            if (codeBlockType == CompileConfiguration.CodeBlockType.Value) {
//                                lineRendering = false
//                                codeBlockType = CompileConfiguration.CodeBlockType.VariableName
//                                valueBuilder.append(code)
//                            }
//                            column += code.length
//                        }
//                    }
//                    else -> if (codeBlockType != CompileConfiguration.CodeBlockType.Note) {
//                        if (codeBlockType == CompileConfiguration.CodeBlockType.VariableName) {
//                            val symbol = "."
//                            val symbolIndex = code.indexOf(symbol)
//                            if (symbolIndex > -1) {
//                                //渲染 ${节.键} 的格式
//                                val sectionSpan =
//                                    Span.obtain(column, EditorColorScheme.FUNCTION_NAME)
//                                colors.add(
//                                    lineNum,
//                                    sectionSpan
//                                )
//                                val symbolSpan =
//                                    Span.obtain(
//                                        column + symbolIndex,
//                                        EditorColorScheme.TEXT_NORMAL
//                                    )
//                                colors.add(
//                                    lineNum,
//                                    symbolSpan
//                                )
//                                val keySpan = Span.obtain(
//                                    column + symbolIndex + symbol.length,
//                                    EditorColorScheme.KEYWORD
//                                )
//                                colors.add(lineNum, keySpan)
//
//                            } else {
//                                //渲染 ${变量名} 的个格式
//                                localVariableNameList.add(LocalVariableName(code, lineNum))
//                                val span = Span.obtain(column, EditorColorScheme.LITERAL)
//                                colors.add(
//                                    lineNum,
//                                    span
//                                )
//                            }
//                        } else {
//                            //不是变量名的话
//                            if (code.startsWith("#")) {
//                                codeBlockType = CompileConfiguration.CodeBlockType.Note
//                                colors.addIfNeeded(lineNum, column, EditorColorScheme.COMMENT)
//                            } else if (code.startsWith("[") && code.endsWith("]")) {
//                                codeBlockType = CompileConfiguration.CodeBlockType.Section
//                                lastSection = code
//                                labels.add(
//                                    NavigationItem(
//                                        lineNum,
//                                        code.substring(1, code.length - 1)
//                                    )
//                                )
//                                val span = Span.obtain(0, EditorColorScheme.FUNCTION_NAME)
//                                colors.add(
//                                    lineNum,
//                                    span
//                                )
//                            } else {
//                                if (lastSection != null) {
//                                    //当代码属于某个节时渲染
//                                    if (codeBlockType == CompileConfiguration.CodeBlockType.Key) {
//                                        keyBuilder.append(code)
//                                        if (hasCode(code)) {
//                                            colors.addIfNeeded(
//                                                lineNum,
//                                                column,
//                                                EditorColorScheme.KEYWORD
//                                            )
//                                            val trueCode = getCode(code)
//                                            if (trueCode == "@define" || trueCode == "@global") {
//                                                codeBlockType =
//                                                    CompileConfiguration.CodeBlockType.VariableName
//                                            }
//                                        } else {
//                                            if (code.contains("_")) {
//                                                val parser = LineParser(code)
//                                                parser.symbol = "_"
//                                                parser.parserSymbol = true
//                                                //局部渲染偏移
//                                                var offset = 0
//                                                parser.analyse { temLineNum, lineData, isEnd ->
//                                                    if (lineData == parser.symbol) {
//                                                        //是符号，添加符号跨度
//                                                        colors.addIfNeeded(
//                                                            lineNum, column + offset,
//                                                            EditorColorScheme.TEXT_NORMAL
//                                                        )
//                                                    } else {
//                                                        if (hasCode(lineData)) {
//                                                            colors.addIfNeeded(
//                                                                lineNum,
//                                                                column + offset,
//                                                                EditorColorScheme.KEYWORD
//                                                            )
//                                                        } else {
//                                                            colors.addIfNeeded(
//                                                                lineNum, column + offset,
//                                                                EditorColorScheme.TEXT_NORMAL
//                                                            )
//                                                        }
//                                                    }
//                                                    offset += lineData.length
//                                                    true
//                                                }
//                                            } else {
//                                                val span =
//                                                    Span.obtain(
//                                                        column,
//                                                        EditorColorScheme.TEXT_NORMAL
//                                                    )
//                                                colors.add(
//                                                    lineNum,
//                                                    span
//                                                )
//                                            }
//                                        }
//                                    } else {
//                                        if (startIndex == -1) {
//                                            startIndex = column
//                                        }
//                                        valueBuilder.append(getCode(code))
//                                    }
//                                }
//                            }
//                        }
//                        column += code.length
//                    }
//                }
//            }
//            colors.navigation = labels
//            colors.determine(lineNum)
//        }
//    }

//    /**
//     * 渲染行内容
//     * @param startIndex Int 冒号的起初位置
//     * @param lastSection String? 最后的节
//     * @param colors TextAnalyzeResult 颜色渲染
//     * @param lineNum Int 行号
//     * @param keyBuilder StringBuilder
//     * @param valueBuilder StringBuilder
//     */
//    private fun renderLineContent(
//        startIndex: Int,
//        lastSection: String?,
//        colors: TextAnalyzeResult,
//        lineNum: Int,
//        keyBuilder: StringBuilder,
//        valueBuilder: StringBuilder
//    ) {
//        //不在任何节内
//        if (lastSection == null) {
//            val valueErrorSpan =
//                Span.obtain(
//                    0,
//                    EditorColorScheme.FUNCTION_NAME
//                )
//            valueErrorSpan.problemFlags = Span.FLAG_ERROR
//            colors.add(lineNum, valueErrorSpan)
//            return
//        }
//        if (startIndex > -1) {
//            //值是否合法
//            if (isValid(
//                    keyBuilder.toString(),
//                    valueBuilder.toString().trim()
//                )
//            ) {
//                if (hasCode(valueBuilder.toString().trim(), true)) {
//                    colors.addIfNeeded(
//                        lineNum,
//                        startIndex,
//                        EditorColorScheme.KEYWORD
//                    )
//                } else {
//                    colors.addIfNeeded(
//                        lineNum,
//                        startIndex,
//                        EditorColorScheme.TEXT_NORMAL
//                    )
//                }
//            } else {
//                val valueErrorSpan =
//                    Span.obtain(
//                        startIndex,
//                        EditorColorScheme.FUNCTION_NAME
//                    )
//                valueErrorSpan.problemFlags = Span.FLAG_ERROR
//                colors.add(lineNum, valueErrorSpan)
//            }
//        }
//    }


    /**
     * 获取绝对代码
     * @param code String
     * @return String
     */
    private fun getCode(code: String): String {
        if (trueHashMap.containsKey(code)) {
            return trueHashMap[code] ?: code
        }
        val trueCode = if (englishMode) {
            code
        } else {
            val codeInfo = getCodeInfo(code)
            codeInfo?.code ?: code
        }
        trueHashMap[code] = trueCode
        return trueCode
    }

    /**
     * 获取代码信息
     * @param code String 代码
     * @return CodeInfo? 代码信息
     */
    private fun getCodeInfo(code: String, isEnglish: Boolean = englishMode): CodeInfo? {
        if (codeDataBase == null) {
            return null
        }
        return if (isEnglish) {
            codeDataBase!!.getCodeDao().findCodeByCode(code)
        } else {
            codeDataBase!!.getCodeDao().findCodeByTranslate(code)
        }
    }

    /**
     * 值是否合法
     * @param key String 键
     * @param value String 值
     * @return Boolean
     */
    private fun isValid(key: String, value: String): Boolean {
        val mapKey = key + "_" + value
        if (validHashMap.containsKey(mapKey)) {
            return validHashMap[mapKey] ?: false
        }
        val codeInfo = getCodeInfo(key)
        val result = if (codeInfo == null) {
            true
        } else {
            val type = codeDataBase?.getValueTypeDao()?.findTypeByType(codeInfo.type)
            return value.matches(Regex(type?.rule ?: "."))
        }
        validHashMap[mapKey] = result
        return result
    }

    /**
     * 数据库内是否包含代码
     *
     * @param value    值
     * @param language 检测语言，如果为 en 则检查Code。其他检查 Translation
     * @return 代码状态
     */
    private fun hasCode(value: String, isEnglish: Boolean = englishMode): Boolean {
        if (containsHashMap.containsKey(value)) {
            return containsHashMap[value] ?: false
        }
        val has = getCodeInfo(value, isEnglish) != null
        containsHashMap[value] = has
        return has
    }

    override fun isEnglishMode(): Boolean {
        return englishMode
    }

    override fun setEnglish(englishMode: Boolean) {
        this.englishMode = englishMode
    }

}