package com.coldmint.rust.core.interfaces

import android.os.Handler
import java.io.File

interface GameSynchronizerListener {


    /**
     * 当找到文件时
     * @param file File
     */
    fun whenChanged(handler: Handler, name: String)

    /**
     * 当同步完成时
     * @param boolean Boolean
     */
    fun whenCompleted(boolean: Boolean)
}