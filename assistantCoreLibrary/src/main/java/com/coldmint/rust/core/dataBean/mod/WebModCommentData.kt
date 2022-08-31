package com.coldmint.rust.core.dataBean.mod

data class WebModCommentData(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String
) {
    data class Data(
        val account: String,
        val content: String,
        val headIcon: String?,
        val id: Int,
        val time: String,
        val userName: String,
        val location: String?
    )
}