package com.coldmint.rust.pro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.WebModAllInfoData
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.adapters.WebModAdapter
import com.coldmint.rust.pro.adapters.WebModAllInfoAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityWorkmangementBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar

/**
 * @author Cold Mint
 * @date 2022/1/4 9:34
 */
class WorkManagementActivity : BaseActivity<ActivityWorkmangementBinding>() {
    /**
     * 显示错误
     * @param stringRes Int 字符串资源id
     * @param str String 字符串
     */
    fun showErrorInView(stringRes: Int? = null, str: String? = null) {
        viewBinding.recyclerView.isVisible = false
        viewBinding.progressBar.isVisible = false
        viewBinding.textview.isVisible = true
        if (stringRes != null) {
            viewBinding.textview.setText(stringRes)
        }
        if (str != null) {
            viewBinding.textview.text = str
        }
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityWorkmangementBinding {
        return ActivityWorkmangementBinding.inflate(layoutInflater)
    }

    override fun onResume() {

        val account = AppSettings.getValue(AppSettings.Setting.Account, "")
        if (account.isBlank()) {
            showErrorInView(R.string.please_login_first)
        } else {
            WebMod.instance.getUserModListAllInfo(
                account,
                object : ApiCallBack<WebModAllInfoData> {
                    override fun onResponse(t: WebModAllInfoData) {
                        val data = t.data?.toMutableList()
                        if (t.code == ServerConfiguration.Success_Code && data != null && data.isNotEmpty()) {
                            try {
                                val adapter =
                                    WebModAllInfoAdapter(this@WorkManagementActivity, data)
                                adapter.setItemEvent { i, itemWebmodAllInfoBinding, viewHolder, data ->
                                    itemWebmodAllInfoBinding.more.setOnClickListener {
                                        val popupMenu = GlobalMethod.createPopMenu(it)
                                        when (data.hidden) {
                                            0 -> {
                                                popupMenu.menu.add(R.string.sold_out_mod)
                                            }
                                            1 -> {
                                                //等待审核
                                                itemWebmodAllInfoBinding.modIntroductionView.setText(
                                                    R.string.not_audit
                                                )
                                            }
                                            -1 -> {
                                                popupMenu.menu.add(R.string.review_audit)
                                            }
                                            -2 -> {
                                                //被管理员举报下架
                                                itemWebmodAllInfoBinding.modIntroductionView.setText(
                                                    R.string.banned_mod
                                                )
                                            }
                                        }
                                        popupMenu.menu.add(R.string.work_of_home_page)
                                        popupMenu.menu.add(R.string.submit_the_update)
                                        popupMenu.menu.add(R.string.update_record)
                                        popupMenu.show()
                                        popupMenu.setOnMenuItemClickListener {
                                            val title = it.title.toString()
                                            when (title) {
                                                getString(R.string.work_of_home_page) -> {
                                                    val bundle = Bundle()
                                                    bundle.putString("modId", data.id)
                                                    bundle.putString("modName", data.name)
                                                    val intent = Intent(
                                                        this@WorkManagementActivity,
                                                        WebModInfoActivity::class.java
                                                    )
                                                    intent.putExtra("data", bundle)
                                                    startActivity(intent)
                                                }
                                                getString(R.string.update_record) -> {
                                                    GlobalMethod.showUpdateLog(
                                                        this@WorkManagementActivity,
                                                        data.id
                                                    )
                                                }
                                                getString(R.string.submit_the_update) -> {
                                                    val intent = Intent(
                                                        this@WorkManagementActivity,
                                                        ReleaseModActivity::class.java
                                                    )
                                                    val bundle = Bundle()
                                                    bundle.putString("mode", "loadMode")
                                                    bundle.putString("modId", data.id)
                                                    intent.putExtra("data", bundle)
                                                    startActivity(intent)
                                                }
                                                getString(R.string.sold_out_mod) -> {
                                                    MaterialDialog(this@WorkManagementActivity).show {
                                                        title(R.string.sold_out_mod).message(R.string.sold_out_mod_tip)
                                                            .positiveButton(R.string.dialog_ok)
                                                            .positiveButton {
                                                                WebMod.instance.soldOutMod(
                                                                    data.developer,
                                                                    data.id,
                                                                    object :
                                                                        ApiCallBack<ApiResponse> {
                                                                        override fun onResponse(
                                                                            t: ApiResponse
                                                                        ) {
                                                                            if (t.code == ServerConfiguration.Success_Code) {
                                                                                data.hidden = -1
                                                                            } else {
                                                                                Snackbar.make(
                                                                                    viewBinding.recyclerView,
                                                                                    t.message,
                                                                                    Snackbar.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        }

                                                                        override fun onFailure(e: Exception) {
                                                                            showInternetError(
                                                                                view = viewBinding.recyclerView,
                                                                                exception = e
                                                                            )
                                                                        }

                                                                    })
                                                            }
                                                            .negativeButton(R.string.dialog_cancel)
                                                            .cancelable(false)
                                                    }
                                                }
                                                getString(R.string.review_audit) -> {
                                                    MaterialDialog(this@WorkManagementActivity).show {
                                                        title(R.string.review_audit).message(
                                                            text =
                                                            String.format(
                                                                getString(R.string.review_audit_mod_tip),
                                                                data.name
                                                            )
                                                        )
                                                            .positiveButton(R.string.dialog_ok)
                                                            .positiveButton {
                                                                WebMod.instance.afreshAuditMod(
                                                                    AppSettings
                                                                        .getValue(
                                                                            AppSettings.Setting.Token,
                                                                            ""
                                                                        ),
                                                                    data.id,
                                                                    object :
                                                                        ApiCallBack<ApiResponse> {
                                                                        override fun onResponse(
                                                                            t: ApiResponse
                                                                        ) {
                                                                            if (t.code == ServerConfiguration.Success_Code) {
                                                                                data.hidden = 1
                                                                            } else {
                                                                                Snackbar.make(
                                                                                    viewBinding.recyclerView,
                                                                                    t.message,
                                                                                    Snackbar.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        }

                                                                        override fun onFailure(e: Exception) {
                                                                            showInternetError(
                                                                                view = viewBinding.recyclerView,
                                                                                exception = e
                                                                            )
                                                                        }

                                                                    })
                                                            }
                                                            .negativeButton(R.string.dialog_cancel)
                                                            .cancelable(false)
                                                    }
                                                }
                                            }
                                            false
                                        }
                                    }
                                    /*itemWebmodAllInfoBinding.soldOutModView.setOnClickListener {
                                        when (itemWebmodAllInfoBinding.soldOutModView.text.toString()) {
                                            getString(R.string.not_audit) -> {

                                            }
                                            getString(R.string.review_audit) -> {

                                            }
                                            getString(R.string.sold_out_mod) -> {

                                            }

                                        }
                                    }*/
                                }
                                viewBinding.textview.isVisible = false
                                viewBinding.progressBar.isVisible = false
                                viewBinding.recyclerView.isVisible = true
                                viewBinding.recyclerView.adapter = adapter
                            } catch (e: Exception) {
                                showError(e.toString())
                            }
                        } else {
                            showErrorInView(str = t.message)
                        }
                    }

                    override fun onFailure(e: Exception) {
                        showErrorInView(R.string.network_error)
                    }

                })
        }


        super.onResume()
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getString(R.string.work_management)
            setReturnButton()
            viewBinding.recyclerView.layoutManager = StableLinearLayoutManager(this)
            val divider = MaterialDividerItemDecoration(
                this,
                MaterialDividerItemDecoration.VERTICAL
            )

            viewBinding.recyclerView.addItemDecoration(
                divider
            )
        }
    }


}