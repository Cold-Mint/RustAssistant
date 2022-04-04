package com.coldmint.rust.core.interfaces

import java.io.File

interface FileFinderListener {
    /**
     * 当找到文件时
     * @param file 文件
     * @return 若返回假，则停止查找
     */
    fun whenFindFile(file: File): Boolean

    /**
     * 当找到文件夹时
     * @param folder 文件夹
     * @return 若返回假，则停止查找
     */
    fun whenFindFolder(folder: File): Boolean
}