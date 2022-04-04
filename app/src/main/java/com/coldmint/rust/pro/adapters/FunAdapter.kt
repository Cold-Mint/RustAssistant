package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databean.FunctionInfo
import com.coldmint.rust.pro.databinding.ItemFunBinding
import com.coldmint.rust.pro.databinding.ItemServiceBinding

class FunAdapter(val context: Context, val dataList: MutableList<FunctionInfo>) :
    BaseAdapter<ItemFunBinding, FunctionInfo>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemFunBinding {
        return ItemFunBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: FunctionInfo,
        viewBinding: ItemFunBinding,
        viewHolder: ViewHolder<ItemFunBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.name
        val resId = data.iconRes
        if (resId != null) {
            viewBinding.iconView.setImageResource(resId)
        }
    }

}