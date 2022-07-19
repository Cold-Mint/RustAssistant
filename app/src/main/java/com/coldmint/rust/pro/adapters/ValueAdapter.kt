package com.coldmint.rust.pro.adapters

import com.coldmint.rust.core.dataBean.ValueTypeDataBean
import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.content.Context
import android.graphics.Color
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ValueItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.StringBuilder
import java.util.ArrayList

class ValueAdapter(context: Context, dataList: ArrayList<ValueTypeDataBean>) :
    BaseAdapter<ValueItemBinding, ValueTypeDataBean>(context, dataList) {

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ValueItemBinding {
        return ValueItemBinding.inflate(layoutInflater,parent,false)
    }

    override fun onBingView(
        typeDataBean: ValueTypeDataBean,
        viewBinding: ValueItemBinding,
        viewHolder: ViewHolder<ValueItemBinding>,
        position: Int
    ) {
        val title = StringBuilder()
        title.append(typeDataBean.name)
        title.append('(')
        title.append(typeDataBean.type)
        title.append(')')
        val scope = typeDataBean.scope
        val context = viewBinding.scopeView.context
        var showData: String? = null
        var scopeTitle: String? = null
        if (scope == FileDataBase.scopeGlobal) {
            scopeTitle = context.getString(R.string.scope_global)
            showData = context.getString(R.string.scope_global_describe)
            viewBinding.scopeView.text = scopeTitle
            viewBinding.cardView.setCardBackgroundColor(Color.parseColor("#d71345"))
        } else if (scope == FileDataBase.scopeFilePath) {
            scopeTitle = context.getString(R.string.scope_filePath)
            showData = context.getString(R.string.scope_filePath_describe)
            viewBinding.scopeView.text = scopeTitle
            viewBinding.cardView.setCardBackgroundColor(Color.parseColor("#f47920"))
        } else if (scope == FileDataBase.scopeThisFile) {
            scopeTitle = context.getString(R.string.scope_thisfile)
            showData = context.getString(R.string.scope_thisfile_describe)
            viewBinding.scopeView.text = scopeTitle
            viewBinding.cardView.setCardBackgroundColor(Color.parseColor("#ffd400"))
        } else {
            scopeTitle = context.getString(R.string.scope_global)
            showData = context.getString(R.string.scope_global_describe)
            viewBinding.scopeView.text = scopeTitle
            viewBinding.cardView.setCardBackgroundColor(Color.parseColor("#d71345"))
        }
        val finalScopeTitle: String = scopeTitle
        val finalShowData: String = showData
        viewBinding.scopeView.setOnClickListener {
            MaterialAlertDialogBuilder(context).setTitle(finalScopeTitle).setMessage(finalShowData)
                .setPositiveButton(R.string.dialog_ok, null).show()
        }
        viewBinding.titleView.text = title.toString()
        viewBinding.descriptionView.text = typeDataBean.describe

    }

}