package com.coldmint.rust.core.dataBean

import com.coldmint.rust.core.web.User

/**
 * 登录请求信息
 */
data class LoginRequestData(
    val account: String,
    val passWord: String,
    val appId: String,
    val isEmail: Boolean = User.isEmail(account)
)