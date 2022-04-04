package com.coldmint.rust.pro.tool

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * 列表滚动状态记录
 */
class ListScrollStatusRecord(private val mRecyclerView: RecyclerView) {
    private var lastOffset = 0
    private var lastPosition = 0

    /*滑动事件监听器*/
    private val onScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (recyclerView.layoutManager != null) {
                    recordLocation()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        }

    /**
     * 记录RecyclerView位置
     */
    private fun recordLocation() {
        val layoutManager = mRecyclerView.layoutManager as LinearLayoutManager?
        //获取可视的第一个view
        val topView = layoutManager!!.getChildAt(0)
        if (topView != null) {
            //获取与该view的顶部的偏移量
            lastOffset = topView.top
            //得到该View的数组位置
            lastPosition = layoutManager.getPosition(topView)
        }
    }

    /**
     * 让RecyclerView滚动到指定位置
     */
    fun loadPosition() {
        if (mRecyclerView.layoutManager != null && lastPosition >= 0) {
            (mRecyclerView.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                lastPosition,
                lastOffset
            )
        }
    }

    /**
     * 构造方法
     *
     * @param recyclerView 回收布局
     */
    init {
        mRecyclerView.addOnScrollListener(onScrollListener)
    }
}