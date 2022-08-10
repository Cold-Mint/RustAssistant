package com.coldmint.rust.pro.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.coldmint.rust.core.dataBean.user.SearchResultDataBean
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.UserHomePageActivity
import com.coldmint.rust.pro.WebModInfoActivity
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemSearchResultBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.tool.TextStyleMaker
import java.util.*

/**
 * 搜索结果适配器
 * @property context Context
 * @property iconOptions RequestOptions
 * @constructor
 */
class SearchResultAdapter(
     context: Context,
    val keyWord: String,
    dataList: MutableList<SearchResultDataBean.Data.Total>
) :
    BaseAdapter<ItemSearchResultBinding, SearchResultDataBean.Data.Total>(context, dataList) {


    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSearchResultBinding {
        return ItemSearchResultBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: SearchResultDataBean.Data.Total,
        viewBinding: ItemSearchResultBinding,
        viewHolder: ViewHolder<ItemSearchResultBinding>,
        position: Int
    ) {
        val finalIcon = data.icon
        if (finalIcon != null && finalIcon.isNotBlank()) {
            viewBinding.imageView.isVisible = true
            if (data.type == "user") {
                Glide.with(context).load(ServerConfiguration.getRealLink(finalIcon))
                    .apply(GlobalMethod.getRequestOptions(true)).into(viewBinding.imageView)
            } else {
                Glide.with(context).load(ServerConfiguration.getRealLink(finalIcon)).apply(GlobalMethod.getRequestOptions())
                    .into(viewBinding.imageView)
            }
        } else {
            viewBinding.imageView.isVisible = false
        }
        viewBinding.titleView.text = data.title
        val index = data.content.indexOf('\n')
        val thisContext = if (index > -1 && data.type == "mod") {
            data.content.subSequence(0, index)
        } else {
            data.content
        }.toString()
        TextStyleMaker.instance.load(viewBinding.contentView, thisContext) { type, da ->
            TextStyleMaker.instance.clickEvent(context, type, da)
        }
        viewBinding.root.setOnClickListener {
            val type = data.type
            when (type) {
                "mod", "mod_comments", "mod_versions" -> {
                    val bundle = Bundle()
                    bundle.putString("modId", data.id)
                    val intent = Intent(
                        context,
                        WebModInfoActivity::class.java
                    )
                    intent.putExtra("data", bundle)
                    context.startActivity(intent)
                }
                "user", "dynamic" -> {
                    val intent = Intent(
                        context,
                        UserHomePageActivity::class.java
                    )
                    intent.putExtra("userId", data.id)
                    context.startActivity(
                        intent
                    )
                }
            }
        }
    }
}