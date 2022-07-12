package com.coldmint.rust.core.dataBean


import com.google.gson.annotations.SerializedName

data class HotSearchData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: MutableList<Data>,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("keyword")
        val keyword: String,
        @SerializedName("number")
        val number: String
    )
}