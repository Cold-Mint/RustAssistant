package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewpager2.widget.ViewPager2
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
        viewBinding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        viewBinding.bottomnavigationView.selectedItemId = R.id.action_recommended
                    }
                    1 -> {
                        viewBinding.bottomnavigationView.selectedItemId = R.id.action_follow
                    }
                    2 -> {
                        viewBinding.bottomnavigationView.selectedItemId = R.id.action_ranking
                    }
                    3 -> {
                        viewBinding.bottomnavigationView.selectedItemId = R.id.action_my
                    }
                }
            }
        })
        viewBinding.bottomnavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_recommended -> {
                    viewBinding.pager.currentItem = 0
                }
                R.id.action_follow -> {
                    viewBinding.pager.currentItem = 1
                }
                R.id.action_ranking -> {
                    viewBinding.pager.currentItem = 2
                }
                R.id.action_my -> {
                    viewBinding.pager.currentItem = 3
                }
            }
            true
        }


        loadTab()
    }
}