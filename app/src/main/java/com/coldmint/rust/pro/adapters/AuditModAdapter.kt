package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemAuditModBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2022/1/10 8:49
 */
class AuditModAdapter(val context: Context, dataList: MutableList<WebModListData.Data>) :
    BaseAdapter<ItemAuditModBinding, WebModListData.Data>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemAuditModBinding {
        return ItemAuditModBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebModListData.Data,
        viewBinding: ItemAuditModBinding,
        viewHolder: ViewHolder<ItemAuditModBinding>,
        position: Int
    ) {
        val info = String.format(
            context.getString(R.string.publisher_information),
            data.developer,
            data.updateTime
        )
        viewBinding.modInfo.text = info
        val icon = data.icon
        if (icon != null && icon.isNotBlank()) {
            val path: String = ServerConfiguration.getRealLink(icon)
            Glide.with(context).load(path).apply(GlobalMethod.getRequestOptions())
                .into(viewBinding.modIcon)
        }
        viewBinding.modNameView.text = data.name
        viewBinding.modIntroductionView.text = data.describe
    }


}