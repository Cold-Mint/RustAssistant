package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.WarehouseAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentWarehouseBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author Cold Mint
 * @date 2022/1/5 10:18
 */
class WarehouseFragment : BaseFragment<FragmentWarehouseBinding>() {


    private fun loadTab() {
        if (isAdded) {
            val mainActivity = activity as MainActivity
/*            if (tableLayout == null) {
                viewBinding.pager.postDelayed({ loadTab() }, MainActivity.linkInterval)
            } else {*/
            TabLayoutMediator(viewBinding.tabLayout, viewBinding.pager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = getText(R.string.mod_title)
                    }

                    1 -> {
                        tab.text = getText(R.string.map)
                    }
                }
            }.attach()
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentWarehouseBinding {
        return FragmentWarehouseBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.pager.adapter = WarehouseAdapter(this)
        //解决启动为仓库页面，点击社区，再返回仓库重复崩溃的问题
        viewBinding.pager.isSaveEnabled = false
        loadTab()
    }


}