package com.coldmint.rust.pro.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.WarehouseAdapter
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentWarehouseBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author Cold Mint
 * @date 2022/1/5 10:18
 */
class WarehouseFragment : BaseFragment<FragmentWarehouseBinding>() {


    private fun loadTab() {
        val mainActivity = activity as MainActivity
        val tableLayout = mainActivity.tabLayout
        if (tableLayout == null) {
            viewBinding.pager.postDelayed({ loadTab() }, MainActivity.linkInterval)
        } else {
            tableLayout.isVisible = true
            TabLayoutMediator(tableLayout, viewBinding.pager)
            { tab, position ->
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
        loadTab()
    }


}