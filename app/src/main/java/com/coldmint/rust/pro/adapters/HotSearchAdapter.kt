package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.dataBean.HotSearchData
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemHotSearchBinding

class HotSearchAdapter(context: Context, dataList: MutableList<HotSearchData.Data>) :
    BaseAdapter<ItemHotSearchBinding, HotSearchData.Data>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemHotSearchBinding {
        return ItemHotSearchBinding.inflate(layoutInflater,parent,false)
    }

    override fun onBingView(
        data: HotSearchData.Data,
        viewBinding: ItemHotSearchBinding,
        viewHolder: ViewHolder<ItemHotSearchBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.keyword
    }
}