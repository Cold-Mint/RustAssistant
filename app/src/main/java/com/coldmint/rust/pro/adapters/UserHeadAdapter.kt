package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.rust.core.dataBean.follow.FollowUserListData
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemUserHeadBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2021/12/28 10:59
 */
class UserHeadAdapter( context: Context, list: MutableList<FollowUserListData.Data>) :
    BaseAdapter<ItemUserHeadBinding, FollowUserListData.Data>(context, list) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemUserHeadBinding {
        return ItemUserHeadBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: FollowUserListData.Data,
        viewBinding: ItemUserHeadBinding,
        viewHolder: ViewHolder<ItemUserHeadBinding>,
        position: Int
    ) {
        val account = data.account
        if (account.isBlank()) {
            viewBinding.headIconView.setImageResource(R.drawable.all_dynamic)
        } else {
            val headIcon = data.headIcon
            if (headIcon != null) {
                Glide.with(context).load(ServerConfiguration.getRealLink(headIcon))
                    .into(viewBinding.headIconView)
            }
        }
        viewBinding.nameView.text = data.userName
    }


}