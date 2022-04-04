package com.coldmint.rust.core.dataBean.mod

/**
 * 网络模组信息
 * @property code Int
 * @property `data` Data
 * @property message String
 * @constructor
 */
data class WebModInfoData(
    val code: Int,
    val `data`: Data,
    val message: String
) {
    data class Data(
        val creationTime: String,
        val describe: String,
        val developer: String,
        val downloadNumber: Int = 0,
        val hidden: Int,
        val icon: String? = null,
        val id: String,
        val link: String,
        val name: String,
        val versionNumber: Int = 0,
        val versionName: String,
        val screenshots: String? = null,
        val tags: String,
        val unitNumber: Int,
        val updateTime: String
    )
}