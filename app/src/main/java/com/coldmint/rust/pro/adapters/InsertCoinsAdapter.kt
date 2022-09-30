package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.coldmint.rust.core.dataBean.mod.InsertCoinHistoryData
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemInsertCoinsBinding
import com.coldmint.rust.pro.tool.GlobalMethod


/**
 * 投币记录适配器
 * @constructor
 */
class InsertCoinsAdapter(context: Context, dataList: MutableList<InsertCoinHistoryData.Data>) :
    BaseAdapter<ItemInsertCoinsBinding, InsertCoinHistoryData.Data>(context, dataList) {

    private val insertCoinsTip by lazy {
        context.getString(R.string.insert_coins_tip)
    }

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemInsertCoinsBinding {
        return ItemInsertCoinsBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: InsertCoinHistoryData.Data,
        viewBinding: ItemInsertCoinsBinding,
        viewHolder: ViewHolder<ItemInsertCoinsBinding>,
        position: Int
    ) {
        val icon = data.headIcon
        if (icon == null) {
            viewBinding.imageView.setImageResource(R.drawable.head_icon)

        } else {
            Glide.with(context).load(ServerConfiguration.getRealLink(icon))
                .apply(GlobalMethod.getRequestOptions(true)).into(viewBinding.imageView)
        }
        viewBinding.numberOfCoinView.text = String.format(insertCoinsTip, data.number)
        viewBinding.timeView.text = data.time
        viewBinding.nameView.text = data.userName

    }
}