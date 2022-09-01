package com.coldmint.rust.core.dataBean.mod


import com.google.gson.annotations.SerializedName

/**
 * 投币状态
 * @property code Int
 * @property `data` Boolean
 * @property message String
 * @constructor
 */
data class CoinStatusData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Boolean = true,
    @SerializedName("message")
    val message: String
)