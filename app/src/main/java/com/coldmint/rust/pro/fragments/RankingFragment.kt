package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.WebModInfoActivity
import com.coldmint.rust.pro.adapters.WebModAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentRankingBinding
import me.zhanghai.android.fastscroll.FastScrollerBuilder

/**
 * 排行榜
 */
class RankingFragment : BaseFragment<FragmentRankingBinding>() {
    var webModAdapter: WebModAdapter? = null
    var lastOffset = 0
    var lastPosition = 0
    val linearLayoutManager by lazy {
        LinearLayoutManager(requireContext())
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = linearLayoutManager
        viewBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                var layoutManager = viewBinding.recyclerView.layoutManager
                if (layoutManager != null) {
                    //获取第一个可视视图
                    val topView = layoutManager.getChildAt(0)
                    if (topView != null) {
                        lastOffset = topView.top
                        lastPosition = layoutManager.getPosition(topView)
                    }
                }
            }
        })
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            loadMods()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

    }

    override fun onResume() {
        loadMods()
        super.onResume()
    }

    fun loadMods() {
        WebMod.instance.list(object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                if (t.code == ServerConfiguration.Success_Code) {
                    val list = t.data
                    if (list != null && list.isNotEmpty()) {
                        viewBinding.progressBar.isVisible = false
                        viewBinding.textview.isVisible = false
                        viewBinding.swipeRefreshLayout.isVisible = true
                        val adapter = createAdapter(list)
                        viewBinding.recyclerView.adapter = adapter
                        linearLayoutManager.scrollToPositionWithOffset(
                            lastPosition,
                            lastOffset
                        )
                        FastScrollerBuilder(viewBinding.recyclerView).useMd2Style()
                            .setPopupTextProvider(adapter).build()
                    } else {
                        showInfoToView(R.string.network_error)
                    }
                } else {
                    showInfoToView(text = t.message)
                }
            }

            override fun onFailure(e: Exception) {
                showInfoToView(R.string.network_error)
            }

        }, sortMode = WebMod.SortMode.Download_Number, limit = "100")
    }


    /**
     * 创建适配器
     * @param dataList MutableList<Data>
     * @return WebModAdapter
     */
    fun createAdapter(dataList: MutableList<WebModListData.Data>): WebModAdapter {
        val adapter: WebModAdapter = if (webModAdapter == null) {
            webModAdapter = WebModAdapter(context = requireContext(), dataList = dataList)
            webModAdapter!!
        } else {
            webModAdapter!!
        }
        adapter.setItemEvent { i, webModItemBinding, viewHolder, data ->
            webModItemBinding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("modId", data.id)
                bundle.putString("modName", data.name)
                val intent = Intent(requireContext(), WebModInfoActivity::class.java)
                intent.putExtra("data", bundle)
                requireContext().startActivity(intent)
            }
        }
        return adapter
    }

    /**
     * 显示信息到视图
     * @param textRes Int?
     * @param text String?
     */
    fun showInfoToView(textRes: Int? = null, text: String? = null) {
        viewBinding.progressBar.isVisible = false
        viewBinding.swipeRefreshLayout.isVisible = false
        viewBinding.textview.isVisible = true
        if (textRes == null) {
            viewBinding.textview.setText(textRes)
        } else {
            viewBinding.textview.text = text ?: ""
        }
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentRankingBinding {
        return FragmentRankingBinding.inflate(layoutInflater)
    }
}