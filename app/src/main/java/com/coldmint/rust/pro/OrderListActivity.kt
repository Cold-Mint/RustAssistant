package com.coldmint.rust.pro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.OrderListDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ActivationApp
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.adapters.OrderAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ActivityOrderListBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar

/**
 * @author Cold Mint
 * @date 2022/1/12 17:52
 */
class OrderListActivity : BaseActivity<ActivityOrderListBinding>() {
    var loadAll = false
    val account by lazy {
        AppSettings.getValue(AppSettings.Setting.Account, "")
    }
    val appId by lazy {
        AppSettings.getValue(AppSettings.Setting.AppID, "")
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getText(R.string.order_list)
            setReturnButton()
            if (account.isBlank()) {
                showError(getString(R.string.please_login_first))
                return
            }
            val thisIntent = intent
            loadAll = thisIntent.getBooleanExtra("loadAll", false)
            viewBinding.recyclerview.layoutManager = LinearLayoutManager(this)
            loadList(loadAll)
        }
    }


    fun loadList(canLoadAll: Boolean) {
        ActivationApp.instance.getOrderList(
            object : ApiCallBack<OrderListDataBean> {
                override fun onResponse(t: OrderListDataBean) {
                    val dataList = t.data
                    if (t.code == ServerConfiguration.Success_Code && dataList != null && dataList.isNotEmpty()) {
                        viewBinding.progressBar.isVisible = false
                        viewBinding.recyclerview.isVisible = true
                        viewBinding.tipView.isVisible = false
                        viewBinding.toolbar.title =
                            getString(R.string.order_list) + "(" + dataList.size + ")"
                        val adapter = OrderAdapter(this@OrderListActivity, dataList)
                        adapter.loadAll = canLoadAll
                        adapter.setItemEvent { i, itemOrderBinding, viewHolder, data ->
                            itemOrderBinding.root.setOnClickListener {
                                if (canLoadAll) {
                                    //如果可以加载全部用户资料（是管理员模式）
                                    val popupMenu =
                                        PopupMenu(this@OrderListActivity, itemOrderBinding.root)
                                    popupMenu.menu.add("设置订单")
                                    popupMenu.menu.add("查看用户资料")
                                    popupMenu.show()
                                    popupMenu.setOnMenuItemClickListener {
                                        val title = it.title
                                        when (title) {
                                            "设置订单" -> {
                                                when (data.state) {
                                                    "false" -> {
                                                        MaterialDialog(this@OrderListActivity).show {
                                                            title(text = data.name).message(text = "确认收到" + data.account + "的付款了嘛?\n订单创建时间:" + data.createTime)
                                                                .positiveButton(R.string.pay_yes)
                                                                .positiveButton {
                                                                    confirmOrder(data.flag, true)
                                                                }.negativeButton(R.string.pay_no)
                                                                .negativeButton {
                                                                    confirmOrder(data.flag, false)
                                                                }
                                                        }
                                                    }
                                                    else -> {
                                                        showToast("无需处理")
                                                    }
                                                }
                                            }
                                            "查看用户资料" -> {
                                                val thisIntent = Intent(
                                                    this@OrderListActivity,
                                                    UserHomePageActivity::class.java
                                                )
                                                thisIntent.putExtra("userId", data.account)
                                                startActivity(thisIntent)
                                            }
                                        }
                                        true
                                    }

                                } else {
                                    MaterialDialog(this@OrderListActivity).show {
                                        title(text = data.name).message(
                                            text = String.format(
                                                getString(R.string.copy_orderid),
                                                data.flag
                                            )
                                        ).positiveButton(R.string.copy).positiveButton {
                                            val flag = data.flag
                                            GlobalMethod.copyText(this@OrderListActivity, flag)
                                            Snackbar.make(
                                                viewBinding.progressBar,
                                                String.format(
                                                    getString(R.string.copy_complete),
                                                    flag
                                                ),
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }.cancelable(false)
                                            .negativeButton(R.string.dialog_cancel)
                                    }
                                }
                            }
                        }
                        viewBinding.recyclerview.adapter = adapter
                    } else {
                        showInfoToView(text = t.message)
                    }
                }

                override fun onFailure(e: Exception) {
                    showInfoToView(R.string.network_error)
                }

            }, if (canLoadAll) {
                null
            } else {
                account
            }
        )
    }

    /**
     * 激活订单
     * @param flag String
     * @param payState Boolean
     */
    fun confirmOrder(flag: String, payState: Boolean) {
        ActivationApp.instance.confirmOrder(
            account,
            appId,
            flag,
            payState,
            object : ApiCallBack<ApiResponse> {
                override fun onResponse(t: ApiResponse) {
                    showToast(t.message)
                }

                override fun onFailure(e: Exception) {
                    showToast(getString(R.string.network_error))
                }

            })
    }

    /**
     * 在视图里显示内容
     * @param resId Int?
     * @param text String?
     */
    fun showInfoToView(resId: Int? = null, text: String? = null) {
        viewBinding.progressBar.isVisible = false
        viewBinding.recyclerview.isVisible = false
        viewBinding.tipView.isVisible = true
        if (resId != null) {
            viewBinding.tipView.setText(resId)
        }
        if (text != null) {
            viewBinding.tipView.text = text
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityOrderListBinding {
        return ActivityOrderListBinding.inflate(layoutInflater)
    }

}