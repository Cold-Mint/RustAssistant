package com.coldmint.rust.core.dataBean

data class DynamicItemDataBean(
    val code: Int,
    val `data`: List<Data>?,
    val message: String
) {
    data class Data(
        val account: String,
        val content: String,
        val email: String,
        val enable: String,
        val gender: Int,
        val headIcon: String?,
        val id: Int,
        val loginTime: String,
        val permission: String,
        val time: String,
        val userName: String
    )
}