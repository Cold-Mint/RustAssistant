package com.coldmint.rust.core.interfaces

import android.view.View

/**
 * 模板解析器接口
 */
interface TemplateParser {

    /**
     * 实现获取用户输入
     */
    fun getInput(): String

    /**
     * 实现实例化视图
     */
    val contextView: View

    /**
     * 实现设置错误方法
     * @param info String 错误信息
     */
    fun setError(info: String)

    /**
     * 指明操作节(可为空)
     */
    val section: String?

    /**
     * 指明操作代码
     */
    val code: String

    /**
     * 是否需要处理
     */
    val needParse: Boolean

}