package com.coldmint.rust.core.dataBean

data class OrderDataBean(
    val code: Int,
    val `data`: Data,
    val message: String
) {
    data class Data(
        val account: String,
        val addTime: String,
        val createTime: String,
        val flag: String,
        val id: String,
        val name: String,
        val originalPrice: Double,
        val price: Double,
        val state: String
    )
}