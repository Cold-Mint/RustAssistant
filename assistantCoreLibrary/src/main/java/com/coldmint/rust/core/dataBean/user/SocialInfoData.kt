package com.coldmint.rust.core.dataBean.user

/**
 * 社交信息
 * @property code Int
 * @property `data` Data
 * @property message String
 * @constructor
 */
data class SocialInfoData(
    val code: Int,
    val `data`: Data,
    val message: String
) {
    data class Data(
        val account: String,
        val email: String,
        val enable: String,
        val loginTime: String,
        val permission: String,
        val userName: String,
        val gender:Int
    )
}