package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databean.ErrorInfo
import com.coldmint.rust.pro.databinding.ItemErrorInfoBinding
import com.coldmint.rust.pro.tool.GlobalMethod

class ErrorInfoAdapter( context: Context, dataList: ArrayList<ErrorInfo>) :
    BaseAdapter<ItemErrorInfoBinding, ErrorInfo>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemErrorInfoBinding {
        return ItemErrorInfoBinding.inflate(layoutInflater)
    }

    override fun onBingView(
        data: ErrorInfo,
        viewBinding: ItemErrorInfoBinding,
        viewHolder: ViewHolder<ItemErrorInfoBinding>,
        position: Int
    ) {
        viewBinding.timeView.text = data.time
        val des = data.describe
        if (des == null || des.isBlank()) {
            viewBinding.describeView.isVisible = false
        } else {
            viewBinding.describeView.isVisible = true
            viewBinding.describeView.text = des
        }
        viewBinding.root.setOnClickListener { view ->
            MaterialDialog(context, BottomSheet()).show {
                title(text = data.time).message(text = data.allErrorDetails)
                    .positiveButton(R.string.copy).positiveButton {
                        GlobalMethod.copyText(context, data.allErrorDetails, view)
                    }.negativeButton(R.string.dialog_cancel)
            }
        }
    }
}
