package com.coldmint.rust.core.dataBean.mod


import com.google.gson.annotations.SerializedName

data class InsertCoinHistoryData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: MutableList<Data>,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("account")
        val account: String,
        @SerializedName("headIcon")
        val headIcon: String?,
        @SerializedName("number")
        val number: Int,
        @SerializedName("time")
        val time: String,
        @SerializedName("userName")
        val userName: String
    )
}