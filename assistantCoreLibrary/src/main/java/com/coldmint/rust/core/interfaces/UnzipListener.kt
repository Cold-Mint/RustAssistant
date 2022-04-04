package com.coldmint.rust.core.interfaces

import java.io.File
import java.util.zip.ZipEntry

interface UnzipListener {
    /**
     * 当解压文件时
     *
     * @param file 文件
     * @return 返回假则结束循环
     */
    fun whenUnzipFile(zipEntry: ZipEntry, file: File): Boolean

    /**
     * 当解压文件夹时
     *
     * @param folder 文件夹
     * @return 返回假则结束循环
     */
    fun whenUnzipFolder(zipEntry: ZipEntry, folder: File): Boolean

    /**
     * 当解压完毕时
     *
     * @param result 结果
     */
    fun whenUnzipComplete(result: Boolean)
}