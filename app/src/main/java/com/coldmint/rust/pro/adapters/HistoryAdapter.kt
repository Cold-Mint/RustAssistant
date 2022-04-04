package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.database.file.HistoryRecord
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemHistoryBinding

/**
 * @author Cold Mint
 * @date 2022/1/14 19:20
 */
class HistoryAdapter(context: Context, dataList: MutableList<HistoryRecord>) :
    BaseAdapter<ItemHistoryBinding, HistoryRecord>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemHistoryBinding {
        return ItemHistoryBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: HistoryRecord,
        viewBinding: ItemHistoryBinding,
        viewHolder: ViewHolder<ItemHistoryBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.fileName
        viewBinding.descriptionView.text = data.time
    }


}