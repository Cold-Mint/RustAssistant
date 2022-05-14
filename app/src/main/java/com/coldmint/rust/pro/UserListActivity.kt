package com.coldmint.rust.pro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.BooleanCallback
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.follow.FollowUserListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Community
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.adapters.UserAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityUserListBinding
import com.google.android.material.snackbar.Snackbar

class UserListActivity : BaseActivity<ActivityUserListBinding>() {

    /**
     * 显示加载失败的错误提示
     * @param tip String 提示
     */
    fun showErrorTip(tip: String) {
        viewBinding.progressBar.isVisible = false
        viewBinding.tipView.isVisible = true
        viewBinding.tipView.text = tip
    }


    override fun getViewBindingObject(): ActivityUserListBinding {
        return ActivityUserListBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            val thisIntent = intent
            val bundle = thisIntent.getBundleExtra("data")
            if (bundle == null) {
                showError("请输入data")
                return
            } else {
                val account = bundle.getString("account")
                if (account == null) {
                    showError("请输入account")
                    return
                }
                val isFollowMode = bundle.getBoolean("isFollowMode", true)
                val canRemoveFans = if (isFollowMode) {
                    false
                } else {
                    bundle.getBoolean("canRemoveFans", false)
                }
                title = if (isFollowMode) {
                    getString(R.string.follow)
                } else {
                    if (canRemoveFans) {
                        getString(R.string.fans_management)
                    } else {
                        getString(R.string.fans)
                    }
                }
                loadList(account, isFollowMode, canRemoveFans)
            }
        }
    }

    /**
     * 加载列表
     * @param account String
     * @param isFollowMode Boolean
     * @param canRemoveFans Boolean
     */
    fun loadList(account: String, isFollowMode: Boolean, canRemoveFans: Boolean) {
        Community.getUserList(
            account,
            isFollowMode,
            apiCallBack = object : ApiCallBack<FollowUserListData> {
                override fun onResponse(t: FollowUserListData) {
                    if (t.code == ServerConfiguration.Success_Code) {
                        val dataList = t.data
                        if (dataList != null) {
                            viewBinding.loadLayout.isVisible = false
                            viewBinding.recyclerView.isVisible = true
                            val adapter = UserAdapter(this@UserListActivity, dataList)
                            viewBinding.recyclerView.layoutManager =
                                LinearLayoutManager(this@UserListActivity)
                            adapter.setItemEvent { i, itemUserBinding, viewHolder, data ->
                                itemUserBinding.root.setOnClickListener {
                                    val intent = Intent(
                                        this@UserListActivity,
                                        UserHomePageActivity::class.java
                                    )
                                    intent.putExtra("userId", data.account)
                                    startActivity(
                                        intent
                                    )
                                }
                                if (canRemoveFans) {
                                    itemUserBinding.actionView.isVisible = true
                                    itemUserBinding.actionView.setText(R.string.remove_fans)
                                    var check = false
                                    itemUserBinding.actionView.setOnClickListener {
                                        MaterialDialog(this@UserListActivity).show {
                                            title(R.string.remove_fans).checkBoxPrompt(
                                                res = R.string.ban_fans,
                                                onToggle = object : BooleanCallback {
                                                    override fun invoke(p1: Boolean) {
                                                        check = p1
                                                    }
                                                }
                                            ).message(
                                                text = String.format(
                                                    getString(R.string.remove_fans_tip),
                                                    data.userName
                                                )
                                            )
                                                .positiveButton(R.string.dialog_ok)
                                                .positiveButton {
                                                    Community.removeFans(
                                                        account,
                                                        data.account,
                                                        object : ApiCallBack<ApiResponse> {
                                                            override fun onResponse(t: ApiResponse) {
                                                                if (t.code == ServerConfiguration.Success_Code) {
                                                                    val index =
                                                                        dataList.indexOf(
                                                                            data
                                                                        )
                                                                    dataList.removeAt(index)
                                                                    adapter.notifyItemRemoved(
                                                                        index
                                                                    )
                                                                    if (dataList.isEmpty()) {
                                                                        loadList(
                                                                            account,
                                                                            isFollowMode,
                                                                            canRemoveFans
                                                                        )
                                                                    }
                                                                }
                                                            }

                                                            override fun onFailure(e: Exception) {
                                                                showInternetError(
                                                                    viewBinding.recyclerView,
                                                                    e
                                                                )
                                                            }
                                                        },
                                                        check
                                                    )
                                                }
                                                .negativeButton(R.string.dialog_cancel)
                                        }
                                    }
                                }
                            }
                            viewBinding.recyclerView.adapter = adapter
                        } else {
                            showErrorTip(t.message)
                        }
                    } else {
                        showErrorTip(t.message)
                    }

                }

                override fun onFailure(e: Exception) {
                    showErrorTip(getString(R.string.network_error))
                }

            })
    }


}