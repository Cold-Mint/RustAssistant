package com.coldmint.rust.pro.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.database.file.FileDataBase
//package com.coldmint.rust.core.database.file.
import com.coldmint.rust.pro.fragments.AllUnitsFragment
import com.coldmint.rust.pro.fragments.HistoryUnitFragment

/**
 * @author Cold Mint
 * @date 2022/1/13 21:22
 */
class ModPageAdapter(
    private val fragmentActivity: FragmentActivity,
    val modClass: ModClass
) :
    FragmentStateAdapter(fragmentActivity) {
    val fileDataBase: FileDataBase by lazy {
        FileDataBase.getInstance(fragmentActivity, modClass.modName, openNewDataBase = true)
    }
    val allUnitsFragment: AllUnitsFragment by lazy {
        val fragment = AllUnitsFragment()
        fragment.modClass = modClass
        fragment.fileDatabase = fileDataBase
        fragment
    }

    val historyUnitFragment: HistoryUnitFragment by lazy {
        val fragment = HistoryUnitFragment()
        fragment.fragmentActivity = fragmentActivity
        fragment.modClass = modClass
        fragment.fileDataBase = fileDataBase
        fragment
    }

    fun setAllUnitsChanged(changed: ((Int) -> Unit)?) {
        allUnitsFragment.whenNumberChanged = changed
    }

    fun setHistoryChanged(changed: ((Int) -> Unit)?) {
        historyUnitFragment.whenNumberChanged = changed
    }

    /**
     * 是否初始化了"全部单位"
     */
    var initAllUnitsFragment = false

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                historyUnitFragment
            }
            else -> {
                initAllUnitsFragment = true
                allUnitsFragment
            }
        }
    }
}