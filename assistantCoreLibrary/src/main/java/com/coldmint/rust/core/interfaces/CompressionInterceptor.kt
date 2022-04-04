package com.coldmint.rust.core.interfaces

import java.io.File

/**
 * 压缩拦截器
 * 此接口的实现类用于拦截压缩时的文档流
 */
interface CompressionInterceptor {
    /**
     * 获取文件类型（正则表达式）
     *
     * @return
     */
    val sourceFileRule: String

    /**
     * 获取无用文件规则
     *
     * @return
     */
    val uselessFileRule: String

    /**
     * 获取转换后的文档流
     *
     * @param file 目标文件
     * @return 处理后的文件内容
     */
    fun getSourceCode(file: File?): String?
}