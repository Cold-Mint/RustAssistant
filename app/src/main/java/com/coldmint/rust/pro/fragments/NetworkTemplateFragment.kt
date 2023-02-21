package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.WebTemplatePackageListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.NetworkTemplatePackageDetailsActivity
import com.coldmint.rust.pro.adapters.MyWebTemplateAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentNetworkTemplateBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.ui.StableLinearLayoutManager

/**
 * 网络模板管理器
 */
class NetworkTemplateFragment : BaseFragment<FragmentNetworkTemplateBinding>() {
    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = StableLinearLayoutManager(requireContext())
        loadList()
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            loadList()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    fun loadList() {
        val token = AppSettings.getValue(AppSettings.Setting.Token, "")
        TemplatePhp.instance.getTemplatePackageList(token,
            object : ApiCallBack<WebTemplatePackageListData> {
                override fun onResponse(t: WebTemplatePackageListData) {
                    if (t.data != null) {
                        val adapter =
                            MyWebTemplateAdapter(requireContext(), t.data)
                        adapter.setItemEvent { i, itemMyWebTemplateBinding, viewHolder, data ->
                            itemMyWebTemplateBinding.root.setOnClickListener {
                                val intent = Intent(requireContext(),NetworkTemplatePackageDetailsActivity::class.java)
                                intent.putExtra("id",data.id)
                                startActivity(intent)
                            }
                        }
                        viewBinding.recyclerView.adapter = adapter
                    }
                }

                override fun onFailure(e: Exception) {
                }

            })
    }

    override fun onResume() {
        super.onResume()
        loadList()
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentNetworkTemplateBinding {
        return FragmentNetworkTemplateBinding.inflate(layoutInflater)
    }
}