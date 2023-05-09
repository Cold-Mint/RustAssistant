package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.WebModItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import me.zhanghai.android.fastscroll.PopupTextProvider

class WebModAdapter( context: Context,  dataList: MutableList<WebModListData.Data>) :
    BaseAdapter<WebModItemBinding, WebModListData.Data>(context, dataList) , PopupTextProvider {

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): WebModItemBinding {
        return WebModItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebModListData.Data,
        viewBinding: WebModItemBinding,
        viewHolder: BaseAdapter.ViewHolder<WebModItemBinding>,
        position: Int
    ) {
        viewBinding.modNameView.text = data.name
        viewBinding.modIntroductionView.text = data.describe
        viewBinding.modInfo.text = String.format(
            context.getString(R.string.web_mod_info),
            data.updateTime,
            data.downloadNumber
        )

        val icon = data.icon
        if (icon != null && icon.isNotBlank()) {
            val path: String = ServerConfiguration.getRealLink(icon)
            Glide.with(context).load(path).apply(GlobalMethod.getRequestOptions())
                .into(viewBinding.modIcon)
        }else{
            viewBinding.modIcon.setImageResource(R.drawable.image)
        }
    }

    override fun getPopupText(position: Int): String {
        return dataList[position].downloadNumber.toString()
    }
}