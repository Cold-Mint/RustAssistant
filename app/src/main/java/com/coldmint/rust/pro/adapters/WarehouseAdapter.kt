package com.coldmint.rust.pro.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.pro.fragments.*

/**
 * @author Cold Mint
 * @date 2022/1/5 10:24
 */
class WarehouseAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ModFragment()
            }
            1 -> {
                MapFragment()
            }
            else -> {
                NullObjectFragment()
            }
        }
    }
}