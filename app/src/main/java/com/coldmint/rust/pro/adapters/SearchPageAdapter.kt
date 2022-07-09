package com.coldmint.rust.pro.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.core.dataBean.user.SearchResultDataBean
import com.coldmint.rust.pro.fragments.SearchResultFragment

class SearchPageAdapter(
    fragmentActivity: FragmentActivity, val keyword: String,
    val data: SearchResultDataBean.Data
) : FragmentStateAdapter(fragmentActivity) {


    override fun getItemCount(): Int {
        return data.type.size + 1
    }

    override fun createFragment(position: Int): Fragment {
        val typeName = if (position == 0) {
            "all"
        } else {
            data.type[position - 1].typeName
        }
        return SearchResultFragment(keyword, data, typeName)
    }


}