package com.coldmint.rust.core.debug

import android.util.Log

object LogCat {

    private var logCatObservers: ArrayList<LogCatObserver>? = null
    var label = "LogCat"

    /**
     * 附加观察者
     */
    fun attachObserver(logCatObserver: LogCatObserver) {
        if (logCatObservers == null) {
            logCatObservers = ArrayList()
        }
        logCatObservers!!.add(logCatObserver)
    }

    /**
     * 卸载观察者
     */
    fun unattachObserver(logCatObserver: LogCatObserver) {
        if (logCatObservers == null) {
            return
        }
        logCatObservers!!.remove(logCatObserver)
    }

    private var enable = true

    /**
     * 是否处于启用状态
     */
    fun isEnable(): Boolean {
        return enable
    }

    /**
     * 设置是否启用
     */
    fun setEnable(enable: Boolean) {
        this.enable = enable
        Log.d(label, "设置启用状态$enable")
    }

    /**
     * 发送消息给所有观察者
     */
    private fun sendMessage(msg: String) {
        logCatObservers?.forEach {
            it.onReceiveLog(msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (!enable) {
            return
        }
        Log.e(label, "错误:${tag}-${msg}")
        sendMessage("错误:${tag}-${msg}")
    }

    fun d(tag: String, msg: String) {
        if (!enable) {
            return
        }
        Log.d(label, "调试:${tag}-${msg}")
        sendMessage("调试:${tag}-${msg}")
    }

    fun w(tag: String, msg: String) {
        if (!enable) {
            return
        }
        Log.w(label, "警告:${tag}-${msg}")
        sendMessage("警告:${tag}-${msg}")
    }

}