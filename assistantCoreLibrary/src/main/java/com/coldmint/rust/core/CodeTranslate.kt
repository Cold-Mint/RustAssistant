package com.coldmint.rust.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.ValueTypeInfo
import com.coldmint.rust.core.interfaces.LineParserEvent
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.tool.LineParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

/**
 * 代码编翻译者
 */
class CodeTranslate(val context: Context) {


    companion object {
        const val split = "\n ,:()=%{}+*/\r"
        private var debugKey = "代码翻译器"
        private var num = 0


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

    private val codeDataBase by lazy {
        CodeDataBase.getInstance(context)
    }

    /**
     * 值类型映射
     */
    private val valueTypeMap by lazy {
        HashMap<String, ValueTypeInfo>()
    }

    //英文模式
    private var englishMode: Boolean = false

    //翻译模式
    private var translateMode: Boolean = true

    /**
     * 设置是否启用英文模式 当启用时禁用翻译和编译功能
     * @param enable Boolean
     */
    fun setEnglishMode(enable: Boolean) {
        this.englishMode = enable
    }

    /**
     * 是否启用翻译模式，当启用时将输入看作英文，转换为中文
     * @param enable Boolean
     */
    fun setTranslate(enable: Boolean) {
        this.translateMode = enable
    }


    /**
     * 代码块类
     * 键，值，节，注释，变量名,引用
     * Reference 引用是一种特殊的数据类型，编译器会尝试编译此值，若无法编译则使用原始值
     */
    enum class CodeBlockType {
        Key, Value, Section, Note, VariableName, Reference, Symbol
    }

    /**
     * 开始工作
     * @param func Function1<String, Unit>
     */
    fun start(input: String, func: (String) -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        val scope = CoroutineScope(Job())
        num++
        debugKey = "代码翻译器-任务${num}"
        DebugHelper.printLog(
            CodeTranslate.debugKey,
            "开始执行(英文模式${englishMode} 翻译模式${translateMode})...",
            "代码翻译", isError = true
        )
        scope.launch {
            if (englishMode) {
                //如果是英文模式，无论是翻译还是编译都返回其本身。
                DebugHelper.printLog(
                    CodeTranslate.debugKey,
                    "是英文模式返回其本身。",
                    "代码翻译", isError = true
                )
                handler.post {
                    func.invoke(input)
                }
                return@launch
            }

            val tokenizer = StringTokenizer(input, CodeTranslate.split, true)
            //缓存翻译数据，以便加速重复数据的翻译
            val translationMap = HashMap<String, String>()
            //保存每次代码的翻译结果
            val codeResult = StringBuilder()
            //保存完整的翻译结果
            val translationResult = StringBuilder()
            var codeBlockType = CodeBlockType.Key
            //保存资源引用值（应该看做整体处理）
            val referenceResult = StringBuilder()
            while (tokenizer.hasMoreTokens()) {
                val code = tokenizer.nextToken()
                if (translationMap.containsKey(code)) {
                    if (codeBlockType == CodeBlockType.Reference) {
                        referenceResult.append(translationMap[code])
                    } else {
                        translationResult.append(translationMap[code])
                    }
                } else {
                    codeResult.clear()
                    when (code) {
                        "\n" -> {
                            if (codeBlockType == CodeBlockType.Reference) {
                                val referenceValue = referenceResult.toString()
                                val codeInfo = if (translateMode) {
                                    codeDataBase.getCodeDao().findCodeByCode(referenceValue)
                                } else {
                                    codeDataBase.getCodeDao().findCodeByTranslate(referenceValue)
                                }
                                if (codeInfo == null) {
                                    translationResult.append(referenceResult)
                                } else {
                                    translationResult.append(
                                        if (translateMode) {
                                            codeInfo.translate
                                        } else {
                                            codeInfo.code
                                        }
                                    )
                                }
                                DebugHelper.printLog(
                                    CodeTranslate.debugKey,
                                    "追加引用值[" + referenceValue + "]",
                                    "翻译行引用处理"
                                )
                                referenceResult.clear()
                            }
                            codeBlockType = CodeBlockType.Key
                            codeResult.append(code)
                        }
                        "\r" -> {
                        }
                        " ", ",", "(", ")", "=", "%", "{", "}", "+", "*", "/" -> {
                            if (codeBlockType == CodeBlockType.Reference) {
                                referenceResult.append(code)
                            } else {
                                codeResult.append(
                                    code
                                )
                            }
                        }
                        ":" -> {
                            if (codeBlockType == CodeBlockType.Reference) {
                                referenceResult.append(code)
                            } else {
                                if (codeBlockType == CodeBlockType.Key) {
                                    codeBlockType =
                                        CodeBlockType.Value
                                }
                                codeResult.append(code)
                            }
                        }
                        else -> if (codeBlockType == CodeBlockType.Note) {
                            codeResult.append(code)
                        } else if (codeBlockType == CodeBlockType.Reference) {
                            //资源引用值应该被整体处理
                            DebugHelper.printLog(
                                CodeTranslate.debugKey,
                                "翻译添加引用值[" + code + "]",
                                "翻译代码处理"
                            )
                            referenceResult.append(code)
                        } else {
                            if (code.startsWith("#")) {
                                codeBlockType = CodeBlockType.Note
                                codeResult.append(code)
                            } else if (code.startsWith("[") && code.endsWith("]")) {
                                val symbolPosition = code.lastIndexOf("_")
                                if (symbolPosition > 0) {
                                    val sectionPrefixName = code.substring(1, symbolPosition)
                                    codeResult.append("[")
                                    val info = if (translateMode) {
                                        codeDataBase.getSectionDao()
                                            .findSectionInfoByCode(sectionPrefixName)
                                    } else {
                                        codeDataBase.getSectionDao()
                                            .findSectionInfoByTranslate(sectionPrefixName)
                                    }
                                    if (translateMode) {
                                        codeResult.append(
                                            info?.translate ?: sectionPrefixName
                                        )
                                    } else {
                                        codeResult.append(
                                            info?.code ?: sectionPrefixName
                                        )
                                    }
                                    codeResult.append("_")
                                    codeResult.append(code.substring(symbolPosition + 1))
                                } else {
                                    val sectionCode = code.substring(1, code.length - 1)
                                    codeResult.append("[")
                                    val info = if (translateMode) {
                                        codeDataBase.getSectionDao()
                                            .findSectionInfoByCode(sectionCode)
                                    } else {
                                        codeDataBase.getSectionDao()
                                            .findSectionInfoByTranslate(sectionCode)
                                    }
                                    if (translateMode) {
                                        codeResult.append(
                                            info?.translate ?: sectionCode
                                        )
                                    } else {
                                        codeResult.append(
                                            info?.code ?: sectionCode
                                        )
                                    }
                                    codeResult.append("]")
                                }
                            } else {
                                //翻译代码
                                val codeInfo = if (translateMode){
                                    codeDataBase.getCodeDao().findCodeByCode(code)
                                }else{
                                    codeDataBase.getCodeDao().findCodeByTranslate(code)
                                }
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
                                                val temCodeInfo = if (translateMode) {
                                                    codeDataBase.getCodeDao()
                                                        .findCodeByCode(lineData.trim())
                                                } else {
                                                    codeDataBase.getCodeDao()
                                                        .findCodeByTranslate(lineData.trim())
                                                }
                                                if (temCodeInfo == null) {
                                                    codeResult.append(lineData)
                                                } else {
                                                    if (translateMode) {
                                                        codeResult.append(temCodeInfo.translate)
                                                    } else {
                                                        codeResult.append(temCodeInfo.code)
                                                    }
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
                                    if (codeBlockType == CodeBlockType.Key) {
                                        val type = getValueData(codeInfo.type)
                                        val tag = type?.tag
                                        if (!tag.isNullOrBlank()) {
                                            //如果此类型为特殊标注，那么设置为注释
                                            codeBlockType =
                                                CodeBlockType.Reference
                                        }
                                    }
                                    if (translateMode)
                                    {
                                        codeResult.append(codeInfo.translate)
                                    }else{
                                        codeResult.append(codeInfo.code)
                                    }
                                }
                            }
                        }
                    }
                    //如果代码不是注释，也不是换行，不是冒号，那么缓存它。
                    if (codeBlockType != CodeBlockType.Note && codeBlockType != CodeBlockType.Reference && code != ":" && code != "\n") {
                        translationMap[code] = codeResult.toString()
                    }
                    translationResult.append(codeResult.toString())
                    DebugHelper.printLog(
                        CodeTranslate.debugKey,
                        "代码[" + code + "]译文[" + codeResult.toString() + "]是否翻译[" + (code != codeResult.toString()) + "]",
                        "翻译"
                    )
                }
            }
            handler.post {
                func.invoke(translationResult.toString())
            }
        }
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

}