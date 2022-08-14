package com.coldmint.rust.pro.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.pro.fragments.DynamicFragment
import com.coldmint.rust.pro.fragments.PersonalHomeFragment

class UserHomeStateAdapter(activity: FragmentActivity, val userId: String) :
    FragmentStateAdapter(activity) {
    private val dynamicFragment: DynamicFragment by lazy {
        DynamicFragment(this.userId)
    }

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                PersonalHomeFragment(this.userId)
            }
            else -> {
                dynamicFragment
            }
        }
    }

    /**
     * 更新动态列表
     */
    fun updataDynamicList() {
        dynamicFragment.loadList()
    }
}