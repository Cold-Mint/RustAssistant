package com.coldmint.rust.core.debug

interface LogCatObserver {

    /**
     * 当收到日志时
     */
    fun onReceiveLog(msg: String)

}