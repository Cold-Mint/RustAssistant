package com.coldmint.rust.pro.ui

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 稳定的线性布局管理器
 */
class ScrollLinearLayoutManager(val context: Context) : LinearLayoutManager(context) {

    /**
     * 这里加了try catch防止奔溃
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun canScrollVertically(): Boolean {
        return false
    }
}