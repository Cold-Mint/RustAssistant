package com.coldmint.rust.core.dataBean.user


import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("activation")
        val activation: Boolean,
        @SerializedName("expirationTime")
        val expirationTime: String,
        @SerializedName("token")
        val token: String,
        @SerializedName("account")
        val account:String
    )
}