package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databean.CommunityServiceInfo
import com.coldmint.rust.pro.databinding.ItemServiceBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 *社区服务适配器
 */
class CommunityServiceAdapter(
     context: Context,
     dataList: MutableList<CommunityServiceInfo>
) :
    BaseAdapter<ItemServiceBinding, CommunityServiceInfo>(context, dataList) {

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemServiceBinding {
        return ItemServiceBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: CommunityServiceInfo,
        viewBinding: ItemServiceBinding,
        viewHolder: BaseAdapter.ViewHolder<ItemServiceBinding>,
        position: Int
    ) {
        Glide.with(context).load(data.iconRes).apply(GlobalMethod.getRequestOptions())
            .into(viewBinding.iconView)
        viewBinding.titleView.setText(data.titleRes)
//        holder.itemView.setOnClickListener {
//            val listener = itemListener
//            listener?.onClickItem(communityServiceInfo)
//        }
    }
}