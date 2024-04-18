package com.coldmint.rust.pro.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.pro.fragments.FollowFragment
import com.coldmint.rust.pro.fragments.NullFragment
import com.coldmint.rust.pro.fragments.RankingFragment
import com.coldmint.rust.pro.fragments.RecommendedFragment
import com.coldmint.rust.pro.fragments.UserInfoFragment

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