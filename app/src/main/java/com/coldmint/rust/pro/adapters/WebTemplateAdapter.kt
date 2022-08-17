package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.dataBean.WebTemplatePackageListData
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemWebTemplateBinding
import me.zhanghai.android.fastscroll.PopupTextProvider

/**
 * 网络模板适配器
 * @constructor
 */
class WebTemplateAdapter(context: Context, dataList: MutableList<WebTemplatePackageListData.Data>) :
    BaseAdapter<ItemWebTemplateBinding, WebTemplatePackageListData.Data>(context, dataList),
    PopupTextProvider {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemWebTemplateBinding {
        return ItemWebTemplateBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebTemplatePackageListData.Data,
        viewBinding: ItemWebTemplateBinding,
        viewHolder: ViewHolder<ItemWebTemplateBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.name
        viewBinding.describeView.text = data.describe
        viewBinding.infoView.text = data.modificationTime
        if (data.subscribe){
            viewBinding.button.setText(R.string.de_subscription)
        }else{
            viewBinding.button.setText(R.string.subscription)
        }
    }

    override fun getPopupText(position: Int): String {
        return getInitial(dataList[position].name).toString()
    }
}