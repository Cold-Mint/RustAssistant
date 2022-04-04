package com.coldmint.rust.core.dataBean.mod

data class WebModListData(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String
) {
    data class Data(
        val describe: String,
        val developer: String,
        val downloadNumber: Int,
        val icon: String?,
        val id: String,
        val name: String,
        val updateTime: String
    )
}