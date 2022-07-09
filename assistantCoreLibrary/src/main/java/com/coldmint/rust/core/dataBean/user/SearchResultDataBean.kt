package com.coldmint.rust.core.dataBean.user


import com.google.gson.annotations.SerializedName

data class SearchResultDataBean(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("total")
        val total: MutableList<Total>,
        @SerializedName("type")
        val type: List<Type>
    ) {
        data class Total(
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

        data class Type(
            @SerializedName("num")
            val num: Int,
            @SerializedName("typeName")
            val typeName: String
        )
    }
}