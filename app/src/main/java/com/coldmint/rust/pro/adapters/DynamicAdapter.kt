package com.coldmint.rust.pro.adapters

import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.DynamicItemDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.web.Dynamic
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemDynamicBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.tool.TextStyleMaker
import com.google.android.material.snackbar.Snackbar
import org.w3c.dom.Text

/**
 * @author Cold Mint
 * @date 2021/12/28 18:29
 */
class DynamicAdapter(context: Context, dataList: MutableList<DynamicItemDataBean.Data>) :
    BaseAdapter<ItemDynamicBinding, DynamicItemDataBean.Data>(context, dataList) {


    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemDynamicBinding {
        return ItemDynamicBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: DynamicItemDataBean.Data,
        viewBinding: ItemDynamicBinding,
        viewHolder: ViewHolder<ItemDynamicBinding>,
        position: Int
    ) {
        val headIcon = data.headIcon
        if (headIcon != null) {
            Glide.with(context).load(ServerConfiguration.getRealLink(headIcon))
                .apply(GlobalMethod.getRequestOptions(true))
                .into(viewBinding.headIconView)
        }
        viewBinding.timeView.text = if (data.location == null) {
            data.time
        } else {
            data.time + " " + data.location
        }
        viewBinding.nameView.text = data.userName
        TextStyleMaker.instance.load(viewBinding.contentView, data.content) { type, data ->
            TextStyleMaker.instance.clickEvent(context, type, data)
        }
        viewBinding.shareImageView.setOnClickListener {
            AppOperator.shareText(context, context.getString(R.string.share_message), data.content);
        }
        viewBinding.thumbUpImageView.setOnClickListener {
            Snackbar.make(viewBinding.thumbUpImageView,R.string.temporarily_unavailable,Snackbar.LENGTH_SHORT).show()
        }
        viewBinding.moreImageView.setOnClickListener { view ->
            val menu = GlobalMethod.createPopMenu(view)
            menu.menu.add(R.string.copy)
            menu.menu.add(R.string.delete_title)
            menu.setOnMenuItemClickListener {
                val title = it.title
                when (title) {
                    context.getString(R.string.copy) -> {
                        GlobalMethod.copyText(context, data.content, view)
                    }
                    context.getString(R.string.delete_title) -> {
                        CoreDialog(context).setTitle(R.string.delete_dynamic)
                            .setMessage(R.string.delete_dynamic_tip)
                            .setPositiveButton(R.string.dialog_ok) {
                                val account = AppSettings.getValue(AppSettings.Setting.Account, "")
                                val appId =
                                    AppSettings
                                        .getValue(AppSettings.Setting.AppID, "")
                                Dynamic.instance.deleteDynamic(
                                    account,
                                    appId,
                                    data.id,
                                    object : ApiCallBack<ApiResponse> {
                                        override fun onResponse(t: ApiResponse) {
                                            //成功与否执行都一样
                                            if (t.code == ServerConfiguration.Success_Code) {
                                                removeItem(viewHolder.adapterPosition)
                                            }
                                            Snackbar.make(
                                                viewBinding.root,
                                                t.message,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onFailure(e: Exception) {
                                            Snackbar.make(
                                                viewBinding.root,
                                                R.string.network_error,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }

                                    })
                            }.setNegativeButton(R.string.dialog_cancel) {

                        }.show()
                    }
                }
                true
            }
            menu.show()
        }
    }


}