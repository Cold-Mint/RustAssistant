package com.coldmint.rust.core.dataBean

/**
 * 优惠券列表数据
 * @property code Int
 * @property `data` List<Data>
 * @property message String
 * @constructor
 */
data class CouponListDataBean(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String
) {
    //优惠券
    data class Data(
        val createTime: String,
        val describe: String,
        val expirationTime: String,
        val id: Int,
        val name: String,
        val num: Int,
        val target: String,
        val type: String,
        val value: Double
    )
}