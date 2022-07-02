package com.coldmint.rust.core.dataBean.user


import com.google.gson.annotations.SerializedName

data class ActivationInfo(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("account")
        val account: String,
        @SerializedName("activation")
        val activation: Boolean,
        @SerializedName("headIcon")
        val headIcon: String?,
        @SerializedName("banTime")
        val banTime: String?,
        @SerializedName("email")
        val email: String,
        @SerializedName("enable")
        val enable: String,
        @SerializedName("expirationTime")
        val expirationTime: String,
        @SerializedName("userName")
        val userName: String,
        @SerializedName("permission")
        val permission: Int
    )
}