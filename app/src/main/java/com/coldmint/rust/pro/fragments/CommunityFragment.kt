package com.coldmint.rust.pro.fragments

import android.view.LayoutInflater
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.CommunityAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CommunityFragment : BaseFragment<FragmentCommunityBinding>() {
    // 当请求时，此适配器返回一个
    // representing an object in the collection.
    private val communityAdapter: CommunityAdapter by lazy {
        CommunityAdapter(this)
    }


    fun loadTab() {
        val mainActivity = requireActivity() as MainActivity
        val tabLayout: TabLayout? = mainActivity.tabLayout
        if (tabLayout != null) {
            tabLayout.isVisible = true
            TabLayoutMediator(tabLayout, viewBinding.pager)
            { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = getText(R.string.recommended)
                    }
                    1 -> {
                        tab.text = getText(R.string.follow)
                    }
                    2 -> {
                        tab.text = getText(R.string.ranking)
                    }
                    3 -> {
                        tab.text = getText(R.string.my)
                    }
                }
            }.attach()
        } else {
            viewBinding.pager.postDelayed({ loadTab() }, MainActivity.linkInterval)
        }
    }





    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.pager.adapter = communityAdapter
        viewBinding.pager.isSaveEnabled = false
        loadTab()
    }


}