package com.coldmint.rust.core.dataBean

data class OrderListDataBean(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String
) {
    data class Data(
        val account: String,
        val addTime: String,
        val createTime: String,
        val flag: String,
        val id: String,
        val name: String,
        val originalPrice: String,
        val price: String,
        val state: String
    )
}