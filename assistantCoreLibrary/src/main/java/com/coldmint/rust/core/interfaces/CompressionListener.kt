package com.coldmint.rust.core.interfaces

import java.io.File

interface CompressionListener {
    /**
     * 当压缩文件时
     *
     * @param file 文件
     * @return 返回假则结束循环
     */
    fun whenCompressionFile(file: File): Boolean

    /**
     * 当压缩文件夹时
     *
     * @param folder 文件夹
     * @return 返回假则结束循环
     */
    fun whenCompressionFolder(folder: File): Boolean

    /**
     * 当压缩完毕时
     *
     * @param result 结果
     */
    fun whenCompressionComplete(result: Boolean)
}