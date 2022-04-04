package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databean.ThanksDataBean
import com.coldmint.rust.pro.databinding.ItemThanksBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2022/1/3 19:39
 */
class ThanksAdapter(val context: Context, dataList: MutableList<ThanksDataBean>) :
    BaseAdapter<ItemThanksBinding, ThanksDataBean>(context, dataList) {

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemThanksBinding {
        return ItemThanksBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: ThanksDataBean,
        viewBinding: ItemThanksBinding,
        viewHolder: ViewHolder<ItemThanksBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.title
        viewBinding.descriptionView.text = data.description
        Glide.with(context).load(data.getIconLink())
            .apply(GlobalMethod.getRequestOptions(true))
            .into(viewBinding.headIconView)
    }

}