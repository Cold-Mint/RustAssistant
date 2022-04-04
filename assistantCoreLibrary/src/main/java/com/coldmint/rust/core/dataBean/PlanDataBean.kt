package com.coldmint.rust.core.dataBean

import android.util.Log
import java.math.BigDecimal
import javax.xml.transform.dom.DOMLocator
import kotlin.math.roundToInt

/**
 * 激活计划类
 * @property code Int
 * @property `data` List<Data>
 * @property message String
 * @constructor
 */
data class PlanDataBean(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String
) {
    data class Data(
        val describe: String,
        val id: String,
        val limit: String,
        val name: String,
        var price: Double,
        var originalPrice: Double,
        val time: String,
        var hasCoupon: Boolean = false
    ) {
        /**
         * 设置折扣券(传入null还原价格)
         * @param coupon Coupon
         */
        fun setCoupon(coupon: CouponListDataBean.Data?) {
            if (coupon == null) {
                if (hasCoupon) {
                    hasCoupon = false
                    price = originalPrice
                    originalPrice = 0.0
                }
            } else {
                if (!hasCoupon) {
                    originalPrice = price
                    val value = coupon.value
                    if (value >= 1) {
                        if (value > price) {
                            price = 0.0
                        } else {
                            price -= value
                        }
                    } else {
                        price *= value
                    }
                    hasCoupon = true
                }
            }
        }
    }
}