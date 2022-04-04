package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.coldmint.rust.core.dataBean.CouponListDataBean
import com.coldmint.rust.core.dataBean.PlanDataBean
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemPlanBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2021/12/19 20:32
 */
class PlanAdapter(context: Context, val dataList: MutableList<PlanDataBean.Data>) :
    BaseAdapter<ItemPlanBinding, PlanDataBean.Data>(context, dataList) {
    private val money: String = context.getString(R.string.money)

    //选中位置
    private var selectedIndex = 0
        get() = field

    /**
     * 选择项目
     * @param index Int
     */
    fun selectItem(index: Int) {
        if (index != selectedIndex) {
            //如果选中位置是新位置
            val oldIndex = selectedIndex
            selectedIndex = index
            notifyItemChanged(oldIndex)
            notifyItemChanged(index)
        }
    }

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemPlanBinding {
        return ItemPlanBinding.inflate(layoutInflater, parent, false)
    }

    /**
     * 添加人民币符号
     * @param num Double
     * @return String
     */
    private fun addSymbol(num: Double): String {
        return String.format(money, num)
    }


    /**
     * 设置优惠券
     * @param coupon Data?
     */
    fun setCoupon(coupon: CouponListDataBean.Data?) {
        dataList.forEach {
            it.setCoupon(coupon)
        }
        notifyDataSetChanged()
    }

    override fun onBingView(
        data: PlanDataBean.Data,
        viewBinding: ItemPlanBinding,
        viewHolder: ViewHolder<ItemPlanBinding>,
        position: Int
    ) {
        if (selectedIndex == position) {
            viewBinding.linearLayout.setBackgroundResource(R.drawable.round_background_true)
        } else {
            viewBinding.linearLayout.setBackgroundResource(R.drawable.round_background_false)
        }
        viewBinding.titleView.text = data.name
        if (data.originalPrice < data.price) {
            viewBinding.originalPriceView.isVisible = false
        } else {
            viewBinding.originalPriceView.isVisible = true
            viewBinding.originalPriceView.text = addSymbol(data.originalPrice)
            GlobalMethod.addDeleteLine(viewBinding.originalPriceView)
        }
        viewBinding.priceView.text = addSymbol(data.price)
    }
}