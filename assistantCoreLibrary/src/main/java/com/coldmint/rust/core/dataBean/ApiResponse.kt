package com.coldmint.rust.core.dataBean
/**
 * code : 0
 * message : 注册成功。
 * data : null
 */
data class ApiResponse(
    var code: Int = 0,
    var message: String,
    var data: String?
)