package com.coldmint.rust.core.dataBean


import com.google.gson.annotations.SerializedName

/**
 * 搜索建议
 * @property code Int
 * @property `data` List<String>
 * @property message String
 * @constructor
 */
data class SearchSuggestionsData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: MutableList<String>,
    @SerializedName("message")
    val message: String
)