package com.coldmint.rust.core.dataBean.mod


import com.google.gson.annotations.SerializedName

data class WebModUpdateLogData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: List<Data>?,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("id")
        val id: String,
        @SerializedName("time")
        val time: String,
        @SerializedName("updateLog")
        val updateLog: String,
        @SerializedName("versionName")
        val versionName: String,
        @SerializedName("versionNumber")
        val versionNumber: String
    )
}