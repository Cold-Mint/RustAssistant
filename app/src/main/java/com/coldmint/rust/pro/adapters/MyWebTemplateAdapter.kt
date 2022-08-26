package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.dataBean.WebTemplatePackageListData
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemMyWebTemplateBinding

class MyWebTemplateAdapter(
    context: Context,
    dataList: MutableList<WebTemplatePackageListData.Data>
) : BaseAdapter<ItemMyWebTemplateBinding, WebTemplatePackageListData.Data>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemMyWebTemplateBinding {
        return ItemMyWebTemplateBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebTemplatePackageListData.Data,
        viewBinding: ItemMyWebTemplateBinding,
        viewHolder: ViewHolder<ItemMyWebTemplateBinding>,
        position: Int
    ) {
        viewBinding.timeView.text = data.modificationTime
        viewBinding.titleView.text = data.getName()
        viewBinding.subTitleView.text = data.describe
    }
}