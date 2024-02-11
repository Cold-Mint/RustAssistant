package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.CommunityAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentCommunityBinding
import com.google.android.material.tabs.TabLayoutMediator

class CommunityFragment : BaseFragment<FragmentCommunityBinding>() {
    private fun loadTab() {
        if (!isAdded) {
            return
        }
        TabLayoutMediator(viewBinding.tabLayout, viewBinding.pager)
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
        /* else {
            viewBinding.pager.postDelayed({ loadTab() }, MainActivity.linkInterval)
        }*/
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentCommunityBinding {
        return FragmentCommunityBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.pager.adapter = CommunityAdapter(this)
        viewBinding.pager.isSaveEnabled = false
        loadTab()
    }
}