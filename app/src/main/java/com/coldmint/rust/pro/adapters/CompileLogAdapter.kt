package com.coldmint.rust.pro.adapters

import android.content.Context
import android.text.SpannableString
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.coldmint.rust.core.AnalysisResult
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.LogItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod

//编译日志适配器
class CompileLogAdapter( context: Context, analysisResults: MutableList<AnalysisResult>) :
    BaseAdapter<LogItemBinding, AnalysisResult>(context, analysisResults) {

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): LogItemBinding {
        return LogItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: AnalysisResult,
        viewBinding: LogItemBinding,
        viewHolder: ViewHolder<LogItemBinding>,
        position: Int
    ) {
//        viewBinding.logInfoView.movementMethod = LinkMovementMethod.getInstance()
        viewBinding.logInfoView.text = data.errorInfo
        if (data.icon == null) {
            viewBinding.imageView.isVisible = false
        } else {
            viewBinding.imageView.isVisible = true
            Glide.with(context).load(data.icon).apply(GlobalMethod.getRequestOptions()).into(viewBinding.imageView)
        }
        val temFun = data.function
        if (temFun != null) {
            viewBinding.logInfoView.setOnClickListener {
                temFun.invoke(viewBinding.root)
            }
        }
    }


}