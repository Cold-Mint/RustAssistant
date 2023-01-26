package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.user.SearchResultDataBean
import com.coldmint.rust.pro.adapters.SearchResultAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentSearchResultBinding
import com.google.android.material.divider.MaterialDividerItemDecoration

/**
 * 搜索结果碎片
 * @property keyword String
 * @property dataList MutableList<Data>
 * @constructor
 */
class SearchResultFragment(
    val keyword: String,
    val data: SearchResultDataBean.Data,
    val typeName: String
) :
    BaseFragment<FragmentSearchResultBinding>() {
    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {

        val adapter = SearchResultAdapter(requireContext(), keyword, filterList())
        viewBinding.recyclerView.adapter = adapter
        val divider = MaterialDividerItemDecoration(
            requireContext(),
            MaterialDividerItemDecoration.VERTICAL
        )
        viewBinding.recyclerView.addItemDecoration(divider)
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * 过滤列表(为all返回所有)
     * @return MutableList<SearchResultDataBean.Data.Total>
     */
    fun filterList(): MutableList<SearchResultDataBean.Data.Total> {
        if (typeName == "all") {
            return data.total
        }
        val list = ArrayList<SearchResultDataBean.Data.Total>()
        data.total.forEach {
            if (it.type == typeName) {
                list.add(it)
            }
        }
        return list
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentSearchResultBinding {
        return FragmentSearchResultBinding.inflate(layoutInflater)
    }
}