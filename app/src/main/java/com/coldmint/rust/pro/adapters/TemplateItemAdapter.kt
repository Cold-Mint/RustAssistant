package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.dataBean.WebTemplatePackageDetailsData
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemTemplateBinding

/**
 * 模板项目
 */
class TemplateItemAdapter(context: Context, dataList: MutableList<WebTemplatePackageDetailsData.Data.Template>) :
    BaseAdapter<ItemTemplateBinding, WebTemplatePackageDetailsData.Data.Template>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemTemplateBinding {
        return ItemTemplateBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebTemplatePackageDetailsData.Data.Template,
        viewBinding: ItemTemplateBinding,
        viewHolder: ViewHolder<ItemTemplateBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.title
    }
}