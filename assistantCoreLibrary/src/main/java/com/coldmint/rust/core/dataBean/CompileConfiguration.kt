package com.coldmint.rust.core.dataBean

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.coldmint.rust.core.*
import java.io.File

/**
 * @author Cold Mint
 * @date 2022/1/27 15:09
 */
data class CompileConfiguration(
    val context: Context,
    val openedSourceFile: OpenedSourceFile,
    val modClass: ModClass,
    val apkFolder: File,
    private var line: Int = 0,
    private var column: Int = 0,
    private var errorNumber: Int = 0,
    private var warningNumber: Int = 0,
    var codeBlockType: CodeBlockType = CodeBlockType.Key,
    var lastSection: String? = null,
    //只能在特定函数内添加错误，否则将抛出异常
    private var canAddError: Boolean = false,
    private val keyBuilder: StringBuilder = StringBuilder(),
    private val valueBuilder: StringBuilder = StringBuilder(),
    private val arrayList: ArrayList<AnalysisResult> = ArrayList(),
    private val errorIcon: Drawable? = context.getDrawable(R.drawable.error),
    private val warningIcon: Drawable? = context.getDrawable(R.drawable.warning),
    //错误记录映射记录表（）
    private var errorRecordMap: HashMap<CodeIndex, ErrorRecord>? = null
) {


    /**
     * 设置是否可以添加错误（只能在特定的函数内添加错误）
     * @param canAddError Boolean
     */
    fun setCanAddError(canAddError: Boolean) {
        this.canAddError = canAddError
    }

    /**
     * 追加结果到Key或Value，仅在行内有效
     * @param string String
     */
    fun appendResult(string: String) {
        when (codeBlockType) {
            CodeBlockType.Key -> {
                keyBuilder.append(string)
            }
            CodeBlockType.Value -> {
                valueBuilder.append(string)
            }
        }
    }

    /**
     * 获取Key
     * @param needTrim Boolean 是否需要去除空格
     * @return String
     */
    fun getKey(needTrim: Boolean = true): String {
        return if (needTrim) {
            keyBuilder.toString().trim()
        } else {
            keyBuilder.toString()
        }
    }


    /**
     * 获取Value
     * @param needTrim Boolean 是否需要去除空格
     * @return String
     */
    fun getValue(needTrim: Boolean = true): String {
        return if (needTrim) {
            valueBuilder.toString().trim()
        } else {
            valueBuilder.toString()
        }
    }

    /**
     * 设置错误映射对象
     * @param errorRecordMap HashMap<CodeIndex, ErrorRecord>?
     */
    fun setErrorRecordMap(errorRecordMap: HashMap<CodeIndex, ErrorRecord>?) {
        this.errorRecordMap = errorRecordMap
    }

    //通常 跟随主题色
    //警告 标识为黄色
    //错误 标识为红色
    enum class ErrorType {
        General, Warning, Error
    }

    /**
     * 代码块类
     */
    enum class CodeBlockType {
        Key, Value, Section, Note, VariableName
    }

    /**
     * 错误记录
     * @property info String 错误信息
     * @property function function? 点击事件
     * @property errorType ErrorType 错误类型
     * @property verifyFunction Function0<Boolean>? 错误验证函数（会被二次执行，返回真则不再显示内容）
     * @constructor
     */
    data class ErrorRecord(
        val info: String,
        var function: ((View) -> Unit)? = null,
        val errorType: ErrorType = ErrorType.Warning,
        var verifyFunction: ((CompileConfiguration) -> Boolean)? = null
    )


    /**
     * 代码位置记录
     * @property string String
     * @property line Int
     * @property column Int
     * @constructor
     */
    data class CodeIndex(val string: String, val line: Int, val column: Int)

    /**
     * 创建代码位置记录
     * @param string String
     * @return CodeIndex
     */
    fun createCodeIndex(string: String): CodeIndex {
        return CodeIndex(string, line, column)
    }

    /**
     * 获取错误数量
     * @return Int
     */
    fun getErrorNumber(): Int {
        return errorNumber
    }

    /**
     * 获取解析结果
     * @return List<AnalysisResult>
     */
    fun getAnalysisResult(): List<AnalysisResult> {
        return arrayList.toList()
    }

    /**
     * 获取警告数量
     * @return Int
     */
    fun getWarningNumber(): Int {
        return warningNumber
    }

    /**
     * 添加错误
     * 若未同步错误表[CompileConfiguration.setErrorRecordMap]则抛出异常
     * 使用[CompileConfiguration.setCanAddError]授权方法添加错误
     * @param codeIndex 代码位置记录
     * @param errorRecord ErrorRecord 错误记录
     */
    fun addError(codeIndex: CodeIndex, errorRecord: ErrorRecord) {
        if (!canAddError) {
            arrayList.add(
                AnalysisResult(
                    "程序错误:未经许可的方法被调用。$errorRecord",
                    errorIcon, errorType = ErrorType.General
                )
            )
            return
        }
        val location = String.format(
            context.getString(R.string.location_info),
            openedSourceFile.file.name,
            line + 1
        )
        val analysisResult =
            AnalysisResult(location + errorRecord.info, errorType = errorRecord.errorType)
        analysisResult.function = errorRecord.function
        when (errorRecord.errorType) {
            ErrorType.Error -> {
                analysisResult.icon = errorIcon
                errorNumber++
            }
            ErrorType.Warning -> {
                analysisResult.icon = warningIcon
                warningNumber++
            }
        }
        if (errorRecordMap == null) {
            throw NullPointerException("错误记录表未同步。")
        } else {
            errorRecordMap?.set(codeIndex, errorRecord)
        }
        arrayList.add(analysisResult)
    }


    /**
     * 添加编译信息
     * @param string String
     */
    fun addInfo(string: String) {
        val analysisResult = AnalysisResult(string, errorType = ErrorType.General)
        arrayList.add(analysisResult)
    }

    /**
     * 获取行号
     * @return Int
     */
    fun getLineNum(): Int {
        return line
    }

    /**
     * 获取列号
     * @return Int
     */
    fun getColumnNum(): Int {
        return column
    }

    /**
     * 将光标指向下一行（初始化行参数）
     */
    fun nextLine() {
        line++
        column = 0
        codeBlockType = CodeBlockType.Key
        keyBuilder.clear()
        valueBuilder.clear()
    }

    /**
     * 添加列内容（添加列值）
     * @param string String
     */
    fun addColumn(string: String) {
        column += string.length
    }

}
