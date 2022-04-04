package com.coldmint.rust.pro.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.coldmint.rust.pro.R

/**
 * @author Cold Mint
 * @date 2021/10/18 21:14
 */
class RootViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
    var titleView: TextView = itemView.findViewById(R.id.titleView)

    companion object {
        /**
         * 获取默认视图
         *
         * @param context 上下文环境
         * @param parent  组件容器
         * @return 默认视图
         */
        fun getDefaultView(context: Context?, parent: ViewGroup?): View {
            return LayoutInflater.from(context).inflate(R.layout.load_item, parent, false)
        }
    }

}