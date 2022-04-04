package com.coldmint.rust.core.dataBean


import com.google.gson.annotations.SerializedName

/**
 * bannerItem数据
 * @property code Int
 * @property `data` List<Data>
 * @property message String
 * @constructor
 */
data class BannerItemDataBean(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: MutableList<Data>?,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("createTime")
        val createTime: String,
        @SerializedName("expirationTime")
        val expirationTime: String,
        @SerializedName("id")
        val id: String,
        @SerializedName("link")
        val link: String,
        @SerializedName("owner")
        val owner: String,
        @SerializedName("picture")
        val picture: String,
        @SerializedName("title")
        val title: String
    )
}