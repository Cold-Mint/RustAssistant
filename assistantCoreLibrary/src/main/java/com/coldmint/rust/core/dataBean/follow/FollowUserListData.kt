package com.coldmint.rust.core.dataBean.follow

/**
 * 关注列表
 * @property code Int
 * @property `data` List<Data>?
 * @property message String
 * @constructor
 */
data class FollowUserListData(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String
) {
    data class Data(
        val account: String,
        val cover: String? = null,
        val email: String,
        val enable: String,
        val fans: Int = 0,
        val follower: Int = 0,
        val gender: Int,
        val headIcon: String? = null,
        val introduce: String? = null,
        val loginTime: String,
        val permission: String,
        val praise: Int = 0,
        val userName: String
    )
}