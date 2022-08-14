package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.DynamicItemDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Dynamic
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.DynamicAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentDynamicBinding

/**
 * @author Cold Mint
 * @date 2021/12/30 9:43
 */
class DynamicFragment(val userId: String) : BaseFragment<FragmentDynamicBinding>() {

    /**
     * 加载列表方法（公开）
     */
    fun loadList() {
        Dynamic.instance.getList(object : ApiCallBack<DynamicItemDataBean> {
            override fun onResponse(t: DynamicItemDataBean) {
                val data = t.data?.toMutableList()
                if (t.code == ServerConfiguration.Success_Code && data != null) {
                    val adapter = DynamicAdapter(requireContext(), data)
                    viewBinding.recyclerView.adapter = adapter
                    viewBinding.recyclerView.isVisible = true
                    viewBinding.progressBar.isVisible = false
                    viewBinding.textview.isVisible = false
                } else {
                    viewBinding.recyclerView.isVisible = false
                    viewBinding.progressBar.isVisible = false
                    viewBinding.textview.isVisible = true
                    viewBinding.textview.text = t.message
                }
            }

            override fun onFailure(e: Exception) {
                viewBinding.recyclerView.isVisible = false
                viewBinding.progressBar.isVisible = false
                viewBinding.textview.isVisible = true
                viewBinding.textview.text = requireContext().getText(R.string.network_error)
            }

        }, account = userId)
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentDynamicBinding {
        return FragmentDynamicBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadList()
    }
}