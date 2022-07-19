package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ScreenshotItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2021/11/20 18:38
 */
class ScreenshotAdapter(val context: Context,  dataList: ArrayList<String>) :
    BaseAdapter<ScreenshotItemBinding, String>(context, dataList) {


    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ScreenshotItemBinding {
        return ScreenshotItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: String,
        viewBinding: ScreenshotItemBinding,
        viewHolder: ViewHolder<ScreenshotItemBinding>,
        position: Int
    ) {
        Glide.with(context).load(data).apply(GlobalMethod.getRequestOptions())
            .into(viewBinding.imageView)
        viewBinding.imageView.setOnLongClickListener {
            MaterialDialog(context).show {
                title(R.string.remove).message(
                    text = String.format(
                        it.context.getString(R.string.remove_image_item),
                        data
                    )
                ).positiveButton(R.string.dialog_ok) {
                    removeItem(viewHolder.adapterPosition)
                }.negativeButton(R.string.dialog_cancel).cancelable(false)
            }

            return@setOnLongClickListener true
        }
    }


}