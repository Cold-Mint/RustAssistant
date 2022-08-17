package com.coldmint.rust.core.dataBean.user

data class SpaceInfoData(
    val code: Int,
    val `data`: Data,
    val message: String
) {
    data class Data(
        val account: String,
        val cover: String?,
        val headIcon: String?,
        val email: String,
        val enable: String,
        val fans: Int,
        val follower: Int,
        val introduce: String?,
        val loginTime: String,
        val permission: Int,
        val praise: Int,
        val expirationTime: String,
        val userName: String,
        val gender: Int,
        val location: String
    )
}