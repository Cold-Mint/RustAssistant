package com.coldmint.rust.pro.adapters

import android.content.Context
import android.content.ServiceConnection
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.WebModAllInfoData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemWebmodAllInfoBinding
import com.coldmint.rust.pro.databinding.WebModItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod

/**
 * @author Cold Mint
 * @date 2022/1/4 10:59
 */
class WebModAllInfoAdapter(
    context: Context,
    list: MutableList<WebModAllInfoData.Data>
) :
    BaseAdapter<ItemWebmodAllInfoBinding, WebModAllInfoData.Data>(context, list) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemWebmodAllInfoBinding {
        return ItemWebmodAllInfoBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: WebModAllInfoData.Data,
        viewBinding: ItemWebmodAllInfoBinding,
        viewHolder: ViewHolder<ItemWebmodAllInfoBinding>,
        position: Int
    ) {
        viewBinding.modNameView.text = data.name
        when (data.hidden) {
            1 -> {
                //等待审核
                viewBinding.modIntroductionView.setText(
                    R.string.not_audit
                )
                viewBinding.more.isVisible = false
            }
            -2 -> {
                //被管理员举报下架
                viewBinding.modIntroductionView.setText(
                    R.string.banned_mod
                )
                viewBinding.more.isVisible = false
            }
            0 -> {
                viewBinding.modIntroductionView.setText(R.string.public_mod)
                viewBinding.more.isVisible = true
            }
            else->{
                viewBinding.modIntroductionView.setText(R.string.private_mod)
                viewBinding.more.isVisible = true
            }
        }

        val icon = data.icon
        if (icon != null && icon.isNotBlank()) {
            Glide.with(context).load(ServerConfiguration.getRealLink(icon))
                .apply(GlobalMethod.getRequestOptions(grayscale = data.hidden == -2))
                .into(viewBinding.modIcon)
        }
    }


}