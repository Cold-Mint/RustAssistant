package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.coldmint.rust.core.MapClass
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemMapBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2022/1/5 11:05
 */
class MapAdapter(val context: Context, dataList: MutableList<MapClass>) :
    BaseAdapter<ItemMapBinding, MapClass>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemMapBinding {
        return ItemMapBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: MapClass,
        viewBinding: ItemMapBinding,
        viewHolder: ViewHolder<ItemMapBinding>,
        position: Int
    ) {
        val icon = data.getIconFile()
        if (icon != null) {
            Glide.with(context).load(icon).apply(GlobalMethod.getRequestOptions()).into(viewBinding.mapIcon)
        }
        viewBinding.mapUpTime.text = data.lastModificationTime
        viewBinding.mapNameView.text = data.getName()
    }


}