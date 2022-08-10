package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databean.FileTab
import com.coldmint.rust.pro.databinding.ItemFileTabBinding

class FileTabAdapter(context: Context, dataList: MutableList<FileTab>) :
    BaseAdapter<ItemFileTabBinding, FileTab>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemFileTabBinding {
        return ItemFileTabBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: FileTab,
        viewBinding: ItemFileTabBinding,
        viewHolder: ViewHolder<ItemFileTabBinding>,
        position: Int
    ) {
        viewBinding.button.text = data.name
        val isEnd = position == dataList.size - 1
        viewBinding.button.icon = if (isEnd) {
            null
        } else {
            context.getDrawable(R.drawable.ic_baseline_chevron_right_24)
        }
    }
}