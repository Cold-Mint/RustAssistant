package com.coldmint.rust.pro.adapters

import com.coldmint.rust.core.SourceFile
import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.content.Context
import android.content.res.ColorStateList
import com.coldmint.rust.pro.tool.AppSettings
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.UnitItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception

class UnitAdapter(
    private val context: Context,
    private val dataList: MutableList<SourceFile>,
    val key: String
) : BaseAdapter<UnitItemBinding, SourceFile>(context, dataList) {

    private val language: String by lazy {
        AppSettings.getInstance(context).getValue(
            AppSettings.Setting.AppLanguage,
            Locale.getDefault().language
        )
    }
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): UnitItemBinding {
        return UnitItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        sourceFile: SourceFile,
        viewBinding: UnitItemBinding,
        viewHolder: ViewHolder<UnitItemBinding>,
        position: Int
    ) {
        try {
            val name = sourceFile.getName(
                language
            )
            viewBinding.unitNameView.text = createSpannableString(name, key)
            val finalDes = sourceFile.getDescribe(
                language, context.getString(R.string.not_find_describe)
            )
            if (finalDes != null) {
                viewBinding.unitDescribeView.text = if (finalDes.length > 20) {
                    finalDes.subSequence(0, 20).toString() + "..."
                } else {
                    finalDes
                }
            }
            viewBinding.unitTimeView.text = formatter.format(sourceFile.file.lastModified())
            val imageView = viewBinding.iconView
            val drawable = sourceFile.getIcon()
            if (drawable != null) {
                Glide.with(context).load(drawable).apply(
                    RequestOptions().override(200).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                ).into(imageView)
            } else {
                Glide.with(context).load(
                    GlobalMethod.tintDrawable(
                        context.getDrawable(R.drawable.image),
                        ColorStateList.valueOf(GlobalMethod.getColorPrimary(context))
                    )
                ).apply(
                    RequestOptions().override(200).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                ).into(imageView)
            }
        } catch (e: Exception) {
            viewBinding.unitDescribeView.text = e.toString()
        }
    }


}