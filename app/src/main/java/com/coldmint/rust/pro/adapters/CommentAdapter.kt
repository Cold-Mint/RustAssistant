package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.rust.core.dataBean.mod.WebModCommentData
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemCommentBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.tool.TextStyleMaker

/**
 * 评论
 * @author Cold Mint
 * @date 2021/12/12 20:50
 */
class CommentAdapter(val context: Context, val dataList: MutableList<WebModCommentData.Data>) :
    BaseAdapter<ItemCommentBinding, WebModCommentData.Data>(context, dataList) {

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemCommentBinding {
        return ItemCommentBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebModCommentData.Data,
        viewBinding: ItemCommentBinding,
        viewHolder: ViewHolder<ItemCommentBinding>,
        position: Int
    ) {
        val icon = data.headIcon
        if (icon != null) {
            Glide.with(context).load(ServerConfiguration.getRealLink(icon)).apply(GlobalMethod.getRequestOptions(true))
                .into(viewBinding.iconView)
        }
        viewBinding.nameView.text = data.userName
        viewBinding.timeView.text = data.time
        TextStyleMaker.instance.load(viewBinding.contentView, data.content) { type, data ->
            TextStyleMaker.instance.clickEvent(context, type, data)
        }
    }


}