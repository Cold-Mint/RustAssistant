package com.coldmint.rust.pro.adapters

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.pro.fragments.*

class CommunityAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }
//
//    // java.lang.IllegalStateException: Fragment no longer exists for key f0:
//    override fun saveState(): Parcelable? {
//        return null
//    }



    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                RecommendedFragment()
            }
            1 -> {
                FollowFragment()
            }
            2->{
                RankingFragment()
            }
            3 -> {
                UserInfoFragment()
            }
            else -> {
                NullFragment()
            }
        }

    }
}