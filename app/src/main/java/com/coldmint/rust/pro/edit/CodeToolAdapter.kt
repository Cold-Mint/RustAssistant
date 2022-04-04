package com.coldmint.rust.pro.edit

import android.view.ViewGroup
import android.view.LayoutInflater
import android.content.Context
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.CodeToolItemBinding

class CodeToolAdapter(context: Context, dataList: MutableList<String>) :
    BaseAdapter<CodeToolItemBinding, String>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): CodeToolItemBinding {
        return CodeToolItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: String,
        viewBinding: CodeToolItemBinding,
        viewHolder: ViewHolder<CodeToolItemBinding>,
        position: Int
    ) {
        viewBinding.codeTextItemView.text = data
    }
}