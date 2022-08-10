package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.dataBean.CouponListDataBean
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemCouponBinding
import kotlin.math.roundToInt

/**
 * @author Cold Mint
 * @date 2022/1/10 20:47
 */
class CouponAdapter( context: Context, dataList: MutableList<CouponListDataBean.Data>) :
    BaseAdapter<ItemCouponBinding, CouponListDataBean.Data>(context, dataList) {
    val timeLimit: String by lazy {
        context.getString(R.string.time_limit)
    }
    val infinite: String by lazy {
        context.getString(R.string.infinite)
    }

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemCouponBinding {
        return ItemCouponBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: CouponListDataBean.Data,
        viewBinding: ItemCouponBinding,
        viewHolder: ViewHolder<ItemCouponBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.name
        viewBinding.descriptionView.text = data.describe
        val numTip = if (data.num == -1) {
            infinite
        } else {
            data.num.toString()
        }
        viewBinding.expirationTimeView.text =
            String.format(timeLimit, data.expirationTime, numTip)
        val value = data.value
        val tip = if (value >= 1) {
            "-${value}元"
        } else {
            "-${100 - (value * 100)}%"
        }
        viewBinding.numView.text = tip
    }


}