package com.coldmint.rust.core.dataBean


import com.google.gson.annotations.SerializedName

/**
 * 搜索记录
 * @property code Int
 * @property `data` List<Data>
 * @property message String
 * @constructor
 */
data class SearchResultDataBean(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: MutableList<Data>?,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("content")
        val content: String,
        @SerializedName("icon")
        val icon: String?,
        @SerializedName("id")
        val id: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("type")
        val type: String
    )
}