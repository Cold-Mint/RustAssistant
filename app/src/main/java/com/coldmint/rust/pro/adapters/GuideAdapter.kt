package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databean.GuideData
import com.coldmint.rust.pro.databinding.ItemGuideBinding

/**
 * 向导适配器
 */
class GuideAdapter(context: Context, dataList: MutableList<GuideData>) :
    BaseAdapter<ItemGuideBinding, GuideData>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemGuideBinding {
        return ItemGuideBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: GuideData,
        viewBinding: ItemGuideBinding,
        viewHolder: ViewHolder<ItemGuideBinding>,
        position: Int
    ) {
        viewBinding.iconView.setImageResource(data.imageRes)
        viewBinding.titleView.setText(data.titleRes)
        viewBinding.describeView.setText(data.describeRes)
    }
}