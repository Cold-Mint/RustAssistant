package com.coldmint.rust.pro.adapters

import android.content.Context
import android.content.ServiceConnection
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.WebModAllInfoData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemWebmodAllInfoBinding
import com.coldmint.rust.pro.databinding.WebModItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2022/1/4 10:59
 */
class WebModAllInfoAdapter(
     context: Context,
    list: MutableList<WebModAllInfoData.Data>
) :
    BaseAdapter<ItemWebmodAllInfoBinding, WebModAllInfoData.Data>(context, list) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemWebmodAllInfoBinding {
        return ItemWebmodAllInfoBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebModAllInfoData.Data,
        viewBinding: ItemWebmodAllInfoBinding,
        viewHolder: ViewHolder<ItemWebmodAllInfoBinding>,
        position: Int
    ) {
        viewBinding.modNameView.text = data.name
        viewBinding.modIntroductionView.text = data.describe
        val icon = data.icon
        if (icon != null && icon.isNotBlank()) {
            Glide.with(context).load(ServerConfiguration.getRealLink(icon)).apply(GlobalMethod.getRequestOptions())
                .into(viewBinding.modIcon)
        }
    }


}