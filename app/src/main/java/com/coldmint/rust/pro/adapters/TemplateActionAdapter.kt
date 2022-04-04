package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ModActionItemBinding

/**
 * 模板活动适配器
 * @property mutableList MutableList<String>
 * @constructor
 */
class TemplateActionAdapter(context: Context, val dataList: MutableList<String>) :
    BaseAdapter<ModActionItemBinding, String>(context, dataList) {

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ModActionItemBinding {
        return ModActionItemBinding.inflate(layoutInflater)
    }

    override fun onBingView(
        data: String,
        viewBinding: ModActionItemBinding,
        viewHolder: ViewHolder<ModActionItemBinding>,
        position: Int
    ) {
        viewBinding.operation.text = data
//        holder.modActionItemBinding.root.setOnClickListener {
//            actionListener?.onClickItem(string)
//        }
    }
}