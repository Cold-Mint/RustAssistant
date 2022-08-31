package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databean.UserGroupData
import com.coldmint.rust.pro.databinding.ItemTemplateBinding
import com.coldmint.rust.pro.databinding.ItemUserBinding
import com.coldmint.rust.pro.databinding.ItemUserGroupBinding

/**
 * 用户群适配器
 */
class UserGroupAdapter(context: Context, dataList: MutableList<UserGroupData>) :
    BaseAdapter<ItemUserGroupBinding, UserGroupData>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemUserGroupBinding {
        return ItemUserGroupBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: UserGroupData,
        viewBinding: ItemUserGroupBinding,
        viewHolder: ViewHolder<ItemUserGroupBinding>,
        position: Int
    ) {
        viewBinding.imageView.setImageResource(data.iconRes)
        viewBinding.titleView.setText(data.titleRes)
    }
}