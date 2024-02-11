package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.TemplatePageAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentTemplateBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TemplateFragment : BaseFragment<FragmentTemplateBinding>() {
    private val adapter: TemplatePageAdapter by lazy {
        TemplatePageAdapter(requireActivity())
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.viewPager2.adapter = adapter
        viewBinding.viewPager2.isSaveEnabled = false
        loadTab()
    }

    fun loadTab() {
        val mainActivity = requireActivity() as MainActivity
            TabLayoutMediator(viewBinding.tabLayout, viewBinding.viewPager2)
            { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = getText(R.string.local)
                    }
                    else -> {
                        tab.text = getText(R.string.network)
                    }
                }
            }.attach()
/*        } else {
            viewBinding.viewPager2.postDelayed({ loadTab() }, MainActivity.linkInterval)
        }*/
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentTemplateBinding {
        return FragmentTemplateBinding.inflate(layoutInflater)
    }
}