package com.coldmint.rust.core.dataBean.user


import com.google.gson.annotations.SerializedName

/**
 * 用户图标数据
 * @property code Int
 * @property `data` Data
 * @property message String
 * @constructor
 */
data class IconData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("headIcon")
        val headIcon: String?,
        @SerializedName("userName")
        val userName: String
    )
}