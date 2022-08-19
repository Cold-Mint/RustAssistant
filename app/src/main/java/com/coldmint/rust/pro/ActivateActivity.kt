package com.coldmint.rust.pro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.CouponListDataBean
import com.coldmint.rust.core.dataBean.PlanDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.web.ActivationApp
import com.coldmint.rust.core.web.Coupon
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.adapters.CouponAdapter
import com.coldmint.rust.pro.adapters.FunAdapter
import com.coldmint.rust.pro.adapters.PlanAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databean.FunctionInfo
import com.coldmint.rust.pro.databinding.ActivityActivateBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ActivateActivity : BaseActivity<ActivityActivateBinding>() {
    var planAdapter: PlanAdapter? = null
    var planId: String? = null
    var couponId: Int? = null

    /**
     * 加载计划列表
     * @param account String
     */
    private fun loadPlanList(account: String) {
        ActivationApp.instance.getPlanList(account, object : ApiCallBack<PlanDataBean> {
            override fun onResponse(t: PlanDataBean) {
                if (t.code == ServerConfiguration.Success_Code) {
                    val data = t.data
                    if (data != null && data.isNotEmpty()) {
                        val layoutManager = LinearLayoutManager(this@ActivateActivity)
                        layoutManager.orientation = RecyclerView.HORIZONTAL
                        viewBinding.recyclerview.layoutManager = layoutManager
                        viewBinding.couponRecyclerview.layoutManager =
                            LinearLayoutManager(this@ActivateActivity)
                        val adapter = PlanAdapter(this@ActivateActivity, data)
                        planAdapter = adapter
                        adapter.setItemEvent { i, itemPlanBinding, viewHolder, data ->
                            itemPlanBinding.linearLayout.setOnClickListener {
                                selectItemAndLoadInfo(data, adapter, i)
                            }
                        }
                        //默认加载第一个元素
                        selectItemAndLoadInfo(data[0], adapter, 0)
                        viewBinding.recyclerview.adapter = adapter
                        initAllFunctionList()
                        viewBinding.linearLayout.isVisible = false
                        viewBinding.nestedScrollView.isVisible = true
                        viewBinding.button.isVisible = true
                    }
                } else {
                    showInfoToView(string = t.message)
                }
            }

            override fun onFailure(e: Exception) {
                showInfoToView(R.string.network_error)
            }

        })
    }

    /**
     * 加载上下文菜单
     * @param menu Menu
     * @return Boolean
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_pay, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.order_list -> {
                val thisIntent = Intent(this, OrderListActivity::class.java)
                startActivity(thisIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * 加载优惠券列表
     * @param account String
     */
    fun loadCouponList(account: String) {
        Coupon.instance.list(account, object : ApiCallBack<CouponListDataBean> {
            override fun onResponse(t: CouponListDataBean) {
                val list = t.data
                if (t.code == ServerConfiguration.Success_Code && list != null) {
                    val couponTip = String.format(getString(R.string.coupon_num), list.size)
                    viewBinding.couponView.text = couponTip
                    val adapter = CouponAdapter(this@ActivateActivity, list)
                    adapter.setItemEvent { i, itemCouponBinding, viewHolder, data ->
                        itemCouponBinding.useButton.setOnClickListener {
                            val finalPlanAdapter = planAdapter
                            if (finalPlanAdapter != null) {
                                couponId = data.id
                                finalPlanAdapter.setCoupon(null)
                                finalPlanAdapter.setCoupon(data)
                                selectItemAndLoadInfo(
                                    finalPlanAdapter.getItemData(0),
                                    finalPlanAdapter,
                                    0
                                )
                                val spannableString =
                                    SpannableString(data.describe + "[" + getString(R.string.clean) + "]")
                                viewBinding.couponDescribeView.movementMethod =
                                    LinkMovementMethod.getInstance()
                                spannableString.setSpan(
                                    object : ClickableSpan() {
                                        override fun onClick(widget: View) {
                                            couponId = null
                                            finalPlanAdapter.setCoupon(null)
                                            viewBinding.couponDescribeView.text =
                                                getString(R.string.coupon_not_use)
                                            selectItemAndLoadInfo(
                                                finalPlanAdapter.getItemData(0),
                                                finalPlanAdapter,
                                                0
                                            )
                                        }
                                    },
                                    data.describe.length,
                                    spannableString.length,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                                );
                                viewBinding.couponDescribeView.text = spannableString
                            }
                        }
                    }
                    viewBinding.couponRecyclerview.adapter = adapter
                } else {
                    viewBinding.couponDescribeView.text = t.message
                }
            }

            override fun onFailure(e: Exception) {
                showInternetError(viewBinding.button, e)
            }

        })
    }

    /**
     * 初始化功能列表
     */
    fun initAllFunctionList() {
        val list: ArrayList<FunctionInfo> = ArrayList()
        list.add(FunctionInfo("中文编辑", iconRes = R.drawable.translate))
        list.add(FunctionInfo("模组优化", iconRes = R.drawable.flash))
        list.add(FunctionInfo("代码高亮", iconRes = R.drawable.highlighted))
        list.add(FunctionInfo("智能联想", iconRes = R.drawable.lenovo))
        list.add(FunctionInfo("单位构建", iconRes = R.drawable.build))
        list.add(FunctionInfo("代码检查", iconRes = R.drawable.error_check))
        list.add(FunctionInfo("模组回收站", iconRes = R.drawable.auto_delete))
        list.add(FunctionInfo("模板系统", iconRes = R.drawable.template))
        val adapter = FunAdapter(this, list)
        viewBinding.functionRecyclerView.layoutManager = GridLayoutManager(this, 4)
        viewBinding.functionRecyclerView.adapter = adapter
    }

    /**
     * 选中项目并加载信息
     * @param data Data 数据
     */
    fun selectItemAndLoadInfo(data: PlanDataBean.Data, adapter: PlanAdapter, position: Int) {
        planId = data.id
        val tip = String.format(getString(R.string.open_tip), data.price)
        viewBinding.button.text = tip
        adapter.selectItem(position)
        //如果折扣，计算并显示折扣信息
        val originalPrize = data.originalPrice
        if (originalPrize > data.price) {
            viewBinding.planDescribeView.text = String.format(
                getString(R.string.discount_prompt),
                data.describe,
                (originalPrize - data.price)
            )
        } else {
            viewBinding.planDescribeView.text = data.describe
        }
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityActivateBinding {
        return ActivityActivateBinding.inflate(layoutInflater)
    }

    /**
     * 显示信息在视图上
     * @param resId Int?
     * @param string String?
     */
    fun showInfoToView(resId: Int? = null, string: String? = null) {
        viewBinding.button.isVisible = false
        viewBinding.nestedScrollView.isVisible = false
        viewBinding.linearLayout.isVisible = true
        viewBinding.tipView.isVisible = true
        viewBinding.progressBar.isVisible = false
        if (resId != null) {
            viewBinding.tipView.setText(resId)
        }
        if (string != null) {
            viewBinding.tipView.text = string
        }
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getText(R.string.activation_app)
            setReturnButton()
            val account = AppSettings.getValue(AppSettings.
            Setting.Account, "")
            if (account.isBlank()) {
                showError(getString(R.string.please_login_first))
                return
            }
            loadPlanList(account)
            loadCouponList(account)
            viewBinding.button.setOnClickListener {
                val finalPlanId = planId ?: return@setOnClickListener
                val finalCouponId = couponId
                val func: () -> Unit = {
                    ActivationApp.instance.createOrder(
                        account,
                        finalPlanId,
                        object : ApiCallBack<ApiResponse> {
                            override fun onResponse(t: ApiResponse) {
                                if (t.code == ServerConfiguration.Success_Code) {
                                    val uuid = t.data
                                    if (uuid != null) {
                                        val newIntent = Intent(
                                            this@ActivateActivity,
                                            PayActivity::class.java
                                        )
                                        newIntent.putExtra("uuid", uuid)
                                        newIntent.putExtra("account", account)
                                        startActivity(
                                            newIntent
                                        )
                                    }
                                } else {
                                    Snackbar.make(
                                        viewBinding.button,
                                        t.message,
                                        Snackbar.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }

                            override fun onFailure(e: Exception) {
                                showInternetError(view = viewBinding.button, e)
                            }

                        },
                        couponsId = couponId
                    )
                }
                if (finalCouponId != null) {
                    MaterialAlertDialogBuilder(this).setTitle(R.string.coupon)
                        .setMessage(R.string.use_coupon)
                        .setPositiveButton(R.string.dialog_ok) { i, i2 ->
                            func.invoke()
                        }.setNegativeButton(R.string.dialog_cancel) { i, i2 ->
                        }.show()
                } else {
                    func.invoke()
                }
            }
        }
    }


}