package com.coldmint.rust.core.interfaces

import java.io.File

/**
 * 移动和复制监听器
 */
interface RemoveAndCopyListener {

    /**
     * 当操作文件时
     * @param file File 文件
     */
    fun whenOperatorFile(file: File)

}