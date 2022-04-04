package com.coldmint.rust.core.dataBean.mod

data class WebModAllInfoData(
    val code: Int,
    val `data`: List<Data>?,
    val message: String
) {
    data class Data(
        val creationTime: String,
        val describe: String,
        val developer: String,
        val downloadNumber: Int,
        var hidden: Int,
        val icon: String? = null,
        val id: String,
        val link: String,
        val name: String,
        val screenshots: String? = null,
        val tags: String,
        val unitNumber: String,
        val updateTime: String,
        val versionName: String,
        val versionNumber: Int = 0
    )
}