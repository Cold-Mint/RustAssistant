package com.coldmint.rust.core.dataBean.user

/**
 * 用户数据
 * @property code Int
 * @property `data` Data
 * @property message String
 * @constructor
 */
data class UserData(
    val code: Int,
    val `data`: Data,
    val message: String
) {
    data class Data(
        val account: String,
        val appID: String,
        val creationTime: String,
        val email: String,
        val enable: Boolean,
        val expirationTime: String,
        val gender: Int,
        val headIcon: String?,
        val loginTime: String,
        val password: String,
        val permission: Int,
        val userName: String,
        val activation: Boolean
    )
}