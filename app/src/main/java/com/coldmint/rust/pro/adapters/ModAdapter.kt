package com.coldmint.rust.pro.adapters

import com.coldmint.rust.core.ModClass
import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.content.Context
import com.coldmint.rust.pro.tool.GlobalMethod
import android.content.res.ColorStateList
import com.bumptech.glide.Glide
import android.os.Build
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ModListItemBinding
import me.zhanghai.android.fastscroll.PopupTextProvider

/*模组适配器
 * 此适配器只能使用于ModFragment成员变量按需传递。
 * */
class ModAdapter(context: Context, dataList: MutableList<ModClass>) :
    BaseAdapter<ModListItemBinding, ModClass>(context, dataList), PopupTextProvider {

    init {
        dataList.sortBy {
            getInitial(it.modName)
        }
    }

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ModListItemBinding {
        return ModListItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: ModClass,
        viewBinding: ModListItemBinding,
        viewHolder: ViewHolder<ModListItemBinding>,
        position: Int
    ) {
        if (data.modFile.isDirectory) {
            data.createNomeidaFile(context)
            val d = context.getString(R.string.not_find_describe)
            var description = data.readValueFromInfo("description", d)
            if (description.length > 15) {
                description = description.substring(0, 15) + "..."
            }
            viewBinding.modIntroductionView.text = description
            if (data.modIcon == null) {
                val drawable = context.getDrawable(R.drawable.image)
                viewBinding.modIcon.setImageDrawable(
                    GlobalMethod.tintDrawable(
                        drawable,
                        ColorStateList.valueOf(GlobalMethod.getColorPrimary(context))
                    )
                )
            } else {
                Glide.with(context).load(data.modIcon).apply(GlobalMethod.getRequestOptions())
                    .into(viewBinding.modIcon)
            }
        } else {
            val drawable = context.getDrawable(R.drawable.file)
            viewBinding.modIcon.setImageDrawable(
                GlobalMethod.tintDrawable(
                    drawable,
                    ColorStateList.valueOf(GlobalMethod.getColorPrimary(context))
                )
            )
            viewBinding.modIntroductionView.text = context.getString(R.string.mod_package)
        }
        viewBinding.modUpTime.text = data.lastModificationTime
        viewBinding.modNameView.text = data.modName
    }

    override fun getPopupText(position: Int): String {
        val s = dataList[position].modName
        return getInitial(s).toString()
    }
}