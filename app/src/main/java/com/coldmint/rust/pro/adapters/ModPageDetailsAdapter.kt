package com.coldmint.rust.pro.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.core.dataBean.mod.WebModInfoData
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.pro.fragments.InsertCoinsFragment
import com.coldmint.rust.pro.fragments.ModCommentsFragment
import com.coldmint.rust.pro.fragments.NullFragment
import com.coldmint.rust.pro.fragments.WebModDetailsFragment

/**
 * 模组详情页面适配器
 * @constructor
 */
class ModPageDetailsAdapter(fragmentActivity: FragmentActivity, val modId: String) :
    FragmentStateAdapter(fragmentActivity) {
    private lateinit var webModDetailsFragment: WebModDetailsFragment
    private lateinit var modCommentsFragment: ModCommentsFragment
    override fun getItemCount(): Int {
        return 3
    }

    val modName: MutableLiveData<String> by lazy {
        MutableLiveData()
    }


    /**
     * 获取下载链接
     * @return String?
     */
    fun getLink(): String? {
        return if (this::webModDetailsFragment.isInitialized) {
            webModDetailsFragment.getLink()
        } else {
            DebugHelper.printLog("获取下载路径", "详情碎片未初始化，返回null", isError = true)
            null
        }
    }

    /**
     * 获取此模组是否对外开放
     * @return Boolean
     */
    fun isOpen(): Boolean {
        return if (this::webModDetailsFragment.isInitialized) {
            webModDetailsFragment.isOpen()
        } else {
            DebugHelper.printLog("获取模组公开状态", "详情碎片未初始化，返回false", isError = true)
            false
        }
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                if (!this::webModDetailsFragment.isInitialized) {
                    webModDetailsFragment = WebModDetailsFragment(modId, modName)
                }
                webModDetailsFragment
            }
            1 -> {
                InsertCoinsFragment(modId)
            }
            2 -> {
                if (!this::modCommentsFragment.isInitialized) {
                    modCommentsFragment = ModCommentsFragment(modId)
                }
                modCommentsFragment
            }
            else -> {
                NullFragment()
            }
        }
    }
}