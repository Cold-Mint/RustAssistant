package com.coldmint.rust.core.dataBean

/**
 * 注册时的请求信息
 */
data class RegisterRequestData(
    val account: String,
    val passWord: String,
    val userName: String,
    val email: String,
    val appID: String
)