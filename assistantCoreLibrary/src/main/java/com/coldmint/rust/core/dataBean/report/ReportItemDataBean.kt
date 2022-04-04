package com.coldmint.rust.core.dataBean.report

data class ReportItemDataBean(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String
) {
    data class Data(
        val account: String,
        val describe: String,
        val headIcon: String?,
        val id: String,
        val state: String,
        val target: String,
        val time: String,
        val type: String,
        val userName: String,
        val why: String
    )
}