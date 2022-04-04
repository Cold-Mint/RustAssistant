package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.dataBean.OrderListDataBean
import com.coldmint.rust.core.web.ActivationApp
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemOrderBinding

/**
 * @author Cold Mint
 * @date 2022/1/12 16:09
 */
class OrderAdapter(context: Context, dataList: MutableList<OrderListDataBean.Data>) :
    BaseAdapter<ItemOrderBinding, OrderListDataBean.Data>(context, dataList) {
    val stringBuilder = StringBuilder()
    var loadAll = false
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemOrderBinding {
        return ItemOrderBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: OrderListDataBean.Data,
        viewBinding: ItemOrderBinding,
        viewHolder: ViewHolder<ItemOrderBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.name
        stringBuilder.clear()
        stringBuilder.append("订单名：")
        stringBuilder.append(data.name)
        stringBuilder.append("\n订单号：")
        stringBuilder.append(data.flag)
        stringBuilder.append("\n创建日期：")
        stringBuilder.append(data.createTime)
        stringBuilder.append("\n应付款：")
        stringBuilder.append(data.price)
        stringBuilder.append("元")
        if (data.originalPrice != data.price) {
            stringBuilder.append("\n原价：")
            stringBuilder.append(data.originalPrice)
            stringBuilder.append("元")
        }
        if (loadAll) {
            stringBuilder.append("\n账号：")
            stringBuilder.append(data.account)
        }
        val state = when (data.state) {
            "true" -> {
                "已完成"
            }
            "ignore" -> {
                "已过期"
            }
            else -> {
                "未完成"
            }
        }
        stringBuilder.append("\n订单状态:")
        stringBuilder.append(state)
        viewBinding.info.text = stringBuilder.toString()
    }


}