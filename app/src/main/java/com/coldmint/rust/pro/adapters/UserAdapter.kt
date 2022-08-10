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
import com.coldmint.rust.pro.databinding.ItemPlanBinding
import com.coldmint.rust.pro.databinding.ItemUserBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2021/12/23 22:44
 */
class UserAdapter( context: Context, dataList: MutableList<FollowUserListData.Data>) :
    BaseAdapter<ItemUserBinding, FollowUserListData.Data>(context, dataList) {
    val defaultIntroduced = context.getString(R.string.defaultIntroduced)
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemUserBinding {
        return ItemUserBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: FollowUserListData.Data,
        viewBinding: ItemUserBinding,
        viewHolder: ViewHolder<ItemUserBinding>,
        position: Int
    ) {
        viewBinding.nameView.text = data.userName
        val introduce = data.introduce
        if (introduce == null || introduce.isBlank()) {
            viewBinding.describeView.text = defaultIntroduced
        } else {
            viewBinding.describeView.text = introduce
        }
        val icon = data.headIcon
        if (icon != null) {
            Glide.with(context).load(ServerConfiguration.getRealLink(icon)).apply(GlobalMethod.getRequestOptions(true))
                .into(viewBinding.iconView)
        }
    }


}