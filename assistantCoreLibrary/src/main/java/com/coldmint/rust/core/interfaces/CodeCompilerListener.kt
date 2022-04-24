package com.coldmint.rust.core.interfaces

import android.text.SpannableString
import android.view.View
import com.coldmint.rust.core.AnalysisResult
import com.coldmint.rust.core.dataBean.CompileConfiguration
import com.coldmint.rust.core.database.code.ValueTypeInfo
import java.io.File

interface CodeCompilerListener {
    /**
     * 当编译完成时
     * @param compileConfiguration CompileConfiguration 编译配置（包含错误信息）
     * @param code String 代码
     */
    fun onCompilationComplete(compileConfiguration: CompileConfiguration, code: String)

    /**
     * 当翻译之前
     */
    fun beforeCompilation()

    /**
     * 当点击Key不存在项目
     * @param lineNum Int 行号
     * @param view View 视图
     * @param code String 代码
     * @param section String 节
     */

    fun onClickKeyNotFoundItem(
        lineNum: Int,
        columnNum: Int,
        view: View,
        code: String,
        section: String
    )

    /**
     * 当点击值类型错误项目
     * @param lineNum Int 行号
     * @param columnNum 列号
     * @param view View 视图
     * @param valueType ValueTypeInfo 值类型
     */

    fun onClickValueTypeErrorItem(
        lineNum: Int,
        columnNum: Int,
        view: View,
        valueType: ValueTypeInfo
    )


    /**
     * 当点击节位置错误
     * @param lineNum Int
     * @param columnNum Int
     * @param view View
     * @param sectionName String
     */

    fun onClickSectionIndexError(lineNum: Int, columnNum: Int, view: View, sectionName: String)

    /**
     * 当点击资源文件不存在项目
     * @param lineNum Int 行号
     * @param view View 视图
     * @param resourceFile File 资源文件
     */
    fun onClickResourceErrorItem(lineNum: Int, columnNum: Int, view: View, resourceFile: File)

    /**
     * 当点击节错误项目（未知的节）
     * @param lineNum Int 行号
     * @param view View 视图
     * @param displaySectionName String 显示的节名
     */
    fun onClickSectionErrorItem(lineNum: Int, view: View, displaySectionName: String)


    /**
     * 单击了游戏同步项目
     * @param lineNum Int
     * @param columnNum Int
     * @param view View
     */
    fun onClickSynchronizationGame(lineNum: Int, columnNum: Int, view: View)

    /**
     * 当点击节名错误项目
     * @param lineNum Int 行号
     * @param columnNum Int 列名
     * @param view View 视图
     * @param sectionName String 节名
     * @param needName Boolean 是否需要附加名
     */

    fun onClickSectionNameErrorItem(
        lineNum: Int,
        columnNum: Int,
        view: View,
        sectionName: String,
        symbolIndex: Int?,
        needName: Boolean
    )

    /**
     * 当点击代码位置错误
     * @param lineNum Int 行号
     * @param view View 视图
     * @param sectionName String 节名
     */

    fun onClickCodeIndexErrorItem(lineNum: Int, view: View, sectionName: String)

    /**
     * 当展示编译结果
     * @param code String
     * @return Boolean
     */
    fun onShowCompilationResult(code: String): Boolean
}