package com.coldmint.rust.pro.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.coldmint.rust.pro.CreationWizardActivity
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.WarehouseAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentWarehouseBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.permissionx.guolindev.PermissionX


/**
 * @author Cold Mint
 * @date 2022/1/5 10:18
 */
class WarehouseFragment : BaseFragment<FragmentWarehouseBinding>() {
    private fun loadTab() {
// 在需要申请权限的地方调用如下方法
        PermissionX.init(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onExplainRequestReason { scope, deniedList ->
                    scope.showRequestReasonDialog(deniedList, "核心基础是基于这些权限", "授权", "取消")
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(deniedList, "您需要手动允许设置中的必要权限", "授权", "取消")
                }.request { allGranted, _, _ ->
                    if (allGranted) {
                    }
                }
        if (isAdded) {
            TabLayoutMediator(viewBinding.tabLayout, viewBinding.pager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = getText(R.string.mod_title)
                    }

                    1 -> {
                        tab.text = getText(R.string.map)
                    }
                }
            }.attach()
            viewBinding.mainButton.setOnClickListener {
                val intent = Intent(context, CreationWizardActivity::class.java)
                intent.putExtra("type", "mod")
                startActivity(intent)
            }
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentWarehouseBinding {
        return FragmentWarehouseBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.pager.adapter = WarehouseAdapter(this)
        //解决启动为仓库页面，点击社区，再返回仓库重复崩溃的问题
        viewBinding.pager.isSaveEnabled = false
        loadTab()
    }


}