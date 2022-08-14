package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.webTemplate.WebTemplatePackageListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.adapters.WebTemplateAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentTemplateCommunityBinding
import com.coldmint.rust.pro.tool.AppSettings
import me.zhanghai.android.fastscroll.FastScrollerBuilder

/**
 * 模板社区
 */
class TemplateCommunityFragment : BaseFragment<FragmentTemplateCommunityBinding>() {
    val token = AppSettings.getValue(AppSettings.Setting.Token, "")

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadData()
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }


    fun loadData() {
        TemplatePhp.instance.getPublicTemplatePackageList(token, object :
            ApiCallBack<WebTemplatePackageListData> {
            override fun onResponse(t: WebTemplatePackageListData) {
                viewBinding.swipeRefreshLayout.isVisible = true
                viewBinding.loadView.isVisible = false
                viewBinding.errorLayout.isVisible = false
                val adapter = WebTemplateAdapter(requireContext(), t.data)
                viewBinding.recyclerView.adapter = adapter
                FastScrollerBuilder(viewBinding.recyclerView).useMd2Style()
                    .setPopupTextProvider(adapter).build()
            }

            override fun onFailure(e: Exception) {
                e.printStackTrace()
                viewBinding.loadView.isVisible = false
                viewBinding.errorLayout.isVisible = true
                viewBinding.swipeRefreshLayout.isVisible = false
            }

        })
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentTemplateCommunityBinding {
        return FragmentTemplateCommunityBinding.inflate(layoutInflater)
    }
}