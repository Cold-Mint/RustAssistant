package com.coldmint.rust.pro.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.pro.fragments.InstalledTemplateFragment
import com.coldmint.rust.pro.fragments.NullFragment
import com.coldmint.rust.pro.fragments.TemplateCommunityFragment

/**
 * 创建单位适配器
 * @constructor
 */
class CreateUnitPageAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    /**
     * 已安装的模板碎片
     */
    private val installedTemplateFragment by lazy {
        InstalledTemplateFragment()
    }


    /**
     * 设置Root目录
     * @param rootPath String
     */
    fun setRootPath(rootPath:String?){
        installedTemplateFragment.viewModel.mRootPath = rootPath
    }


    /**
     * 设置创建的目录
     * @param createPath String
     */
    fun setCreatePath(createPath :String){
       return installedTemplateFragment.setCreatePath(createPath)
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            installedTemplateFragment
        } else {
            TemplateCommunityFragment()
        }
    }
}