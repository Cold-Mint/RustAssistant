package com.coldmint.rust.pro.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.report.ReportItemDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Report
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.UserHomePageActivity
import com.coldmint.rust.pro.WebModInfoActivity
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemReportBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import java.lang.StringBuilder

/**
 * @author Cold Mint
 * @date 2022/1/9 11:03
 */
class ReportAdapter(
    val context: Context,
     dataList: MutableList<ReportItemDataBean.Data>
) :
    BaseAdapter<ItemReportBinding, ReportItemDataBean.Data>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemReportBinding {
        return ItemReportBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: ReportItemDataBean.Data,
        viewBinding: ItemReportBinding,
        viewHolder: ViewHolder<ItemReportBinding>,
        position: Int
    ) {
        val headIcon = data.headIcon
        if (headIcon != null) {
            Glide.with(context).load(ServerConfiguration.getRealLink(headIcon))
                .apply(GlobalMethod.getRequestOptions(true))
                .into(viewBinding.headIconView)
        }
        viewBinding.timeView.text = data.time
        viewBinding.nameView.text = data.userName
        viewBinding.openView.text =
            String.format(context.getString(R.string.view_the_report_object), data.target)
        when (data.type) {
            "mod" -> {
                viewBinding.typeView.setText(R.string.report_mod)
                viewBinding.actionView.setText(R.string.sold_out_mod)
                viewBinding.openView.setOnClickListener {
                    val intent = Intent(context, WebModInfoActivity::class.java)
                    val target = data.target
                    val bundle = Bundle()
                    bundle.putString("modName", target)
                    bundle.putString("modId", target)
                    intent.putExtra("data", bundle)
                    context.startActivity(intent)
                }
            }
            "user" -> {
                viewBinding.typeView.setText(R.string.report_user)
                viewBinding.openView.setOnClickListener {
                    val intent = Intent(context, UserHomePageActivity::class.java)
                    intent.putExtra("userId", data.target)
                    context.startActivity(intent)

                }
            }
        }
        viewBinding.textview.text = data.why
        viewBinding.describeView.text = data.describe
    }

}