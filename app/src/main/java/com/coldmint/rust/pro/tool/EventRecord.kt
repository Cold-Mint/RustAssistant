package com.coldmint.rust.pro.tool

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * 事件记录类
 */
object EventRecord {
    const val Event_LOGOUT = "logout"

    //打包Mod
    const val Event_PACK_MOD = "pack_mod"
    const val Event_LOGIN = "login"

    private val firebaseAnalytics = Firebase.analytics
    private var account: String? = null

    /**
     * 设置用户id
     */
    fun setUserId(userId: String?) {
        account = userId
    }

    /**
     * 登录
     */
    fun login() {
        val bundle = Bundle()
        bundle.putString("account", account)
        firebaseAnalytics.logEvent(Event_LOGIN, bundle)
        firebaseAnalytics.setUserId(account)
    }

    /**
     * 登出
     */
    fun logout() {
        val bundle = Bundle()
        bundle.putString("account", account)
        firebaseAnalytics.logEvent(Event_LOGOUT, bundle)
    }

    /**
     * 打包模组
     */
    fun packMod(targetFileName: String) {
        val bundle = Bundle()
        bundle.putString("account", account)
        bundle.putString("fileName", targetFileName)
        firebaseAnalytics.logEvent(Event_PACK_MOD, bundle)
        firebaseAnalytics.setUserId(account)
    }
}