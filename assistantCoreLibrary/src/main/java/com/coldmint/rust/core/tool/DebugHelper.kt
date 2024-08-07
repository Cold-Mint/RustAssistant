package com.coldmint.rust.core.tool

import android.util.Log
import com.coldmint.rust.core.debug.LogCat

/**
 * 调试帮助器
 */
object DebugHelper {


    /**
     * 日志打印
     * @param message String 消息
     * @param module String? 模块名
     * @param isError Boolean 是否为错误
     */
    fun printLog(keyValue: String, message: String, module: String? = null, isError: Boolean = false) {
        val key = if (module == null) {
            keyValue
        } else {
            "$keyValue-$module"
        }
        if (isError) {
            LogCat.e(key, message)
        } else {
            LogCat.d(key, message)
        }
    }

}