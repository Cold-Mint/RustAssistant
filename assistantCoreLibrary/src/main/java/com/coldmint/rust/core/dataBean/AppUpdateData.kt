package com.coldmint.rust.core.dataBean


import com.google.gson.annotations.SerializedName

/**
 * App更新数据
 * @property code Int
 * @property `data` Data
 * @property message String
 * @constructor
 */
data class AppUpdateData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("content")
        val content: String,
        @SerializedName("forced")
        var forced: Boolean,
        @SerializedName("id")
        val id: Int,
        @SerializedName("isBeta")
        val isBeta: Boolean,
        @SerializedName("link")
        val link: String,
        @SerializedName("time")
        val time: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("versionName")
        val versionName: String,
        @SerializedName("versionNumber")
        val versionNumber: Int
    )
}