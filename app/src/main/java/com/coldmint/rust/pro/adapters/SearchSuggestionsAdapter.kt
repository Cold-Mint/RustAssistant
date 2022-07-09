package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemSearchSuggestionsBinding

/**
 * 搜索建议适配器
 * @constructor
 */
class SearchSuggestionsAdapter(
    context: Context,
    val keyword: String,
    dataList: MutableList<String>
) :
    BaseAdapter<ItemSearchSuggestionsBinding, String>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSearchSuggestionsBinding {
        return ItemSearchSuggestionsBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: String,
        viewBinding: ItemSearchSuggestionsBinding,
        viewHolder: ViewHolder<ItemSearchSuggestionsBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = createSpannableString(data,keyword)
    }
}