package com.coldmint.rust.pro.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.pro.fragments.LocalTemplateFragment
import com.coldmint.rust.pro.fragments.NetworkTemplateFragment
import com.coldmint.rust.pro.fragments.NullFragment

/**
 * 模板页面适配器
 * @constructor
 */
class TemplatePageAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0){
            LocalTemplateFragment()
        }else{
            NetworkTemplateFragment()
        }
    }
}