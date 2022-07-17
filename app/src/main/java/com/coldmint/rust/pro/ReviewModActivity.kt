package com.coldmint.rust.pro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.adapters.AuditModAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityReviewModBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.google.android.material.snackbar.Snackbar

/**
 * @author Cold Mint
 * @date 2022/1/9 16:50
 */
class ReviewModActivity : BaseActivity<ActivityReviewModBinding>() {
    val token by lazy {
        AppSettings.getInstance(this).getValue(AppSettings.Setting.Token, "")
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getText(R.string.review_mod)
            setReturnButton()
            if (token.isBlank()) {
                showInfoToView(resId = R.string.please_login_first)
                return
            }
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
            loadList()
        }
    }

    /**
     * 加载列表
     */
    fun loadList() {
        WebMod.instance.getAuditList(object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                val temData = t.data
                if (t.code == ServerConfiguration.Success_Code && temData != null) {
                    val mutableList = temData.toMutableList()
                    viewBinding.progressBar.isVisible = false
                    viewBinding.tipView.isVisible = false
                    viewBinding.recyclerView.isVisible = true
                    val adapter = AuditModAdapter(this@ReviewModActivity, mutableList)
                    adapter.setItemEvent { i, itemAuditModBinding, viewHolder, data ->
                        itemAuditModBinding.root.setOnClickListener {
                            val intent =
                                Intent(this@ReviewModActivity, WebModInfoActivity::class.java)
                            val target = data.id
                            val bundle = Bundle()
                            bundle.putString("modName", target)
                            bundle.putString("modId", target)
                            intent.putExtra("data", bundle)
                            startActivity(intent)
                        }
                        itemAuditModBinding.consentView.setOnClickListener {
                            WebMod.instance.auditMod(
                                token,
                                data.id,
                                true,
                                object : ApiCallBack<ApiResponse> {
                                    override fun onResponse(t: ApiResponse) {
                                        if (t.code == ServerConfiguration.Success_Code) {
                                            val index = mutableList.indexOf(data)
                                            mutableList.removeAt(index)
                                            adapter.notifyItemRemoved(index)
                                            if (mutableList.isEmpty()) {
                                                loadList()
                                            }
                                        } else {
                                            Snackbar.make(
                                                viewBinding.tipView,
                                                t.message,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onFailure(e: Exception) {
                                        showInternetError(viewBinding.tipView, e)
                                    }

                                })
                        }
                        itemAuditModBinding.refusedView.setOnClickListener {

                            WebMod.instance.auditMod(
                                token,
                                data.id,
                                false,
                                object : ApiCallBack<ApiResponse> {
                                    override fun onResponse(t: ApiResponse) {
                                        if (t.code == ServerConfiguration.Success_Code) {
                                            val index = mutableList.indexOf(data)
                                            mutableList.removeAt(index)
                                            adapter.notifyItemRemoved(index)
                                            if (mutableList.isEmpty()) {
                                                loadList()
                                            }
                                        } else {
                                            Snackbar.make(
                                                viewBinding.tipView,
                                                t.message,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onFailure(e: Exception) {
                                        showInternetError(viewBinding.tipView, e)
                                    }

                                })
                        }
                    }
                    viewBinding.recyclerView.adapter = adapter
                } else {
                    showInfoToView(str = t.message)
                }
            }

            override fun onFailure(e: Exception) {
                showInfoToView(resId = R.string.network_error)
            }

        }, sortMode = WebMod.SortMode.Latest_Time)
    }

    /**
     * 显示信息在视图上
     * @param str String?
     * @param resId Int?
     */
    fun showInfoToView(str: String? = null, resId: Int? = null) {
        viewBinding.progressBar.isVisible = false
        viewBinding.recyclerView.isVisible = false
        viewBinding.tipView.isVisible = true
        if (str != null) {
            viewBinding.tipView.text = str
        }
        if (resId != null) {
            viewBinding.tipView.setText(resId)
        }
    }

    override fun getViewBindingObject(): ActivityReviewModBinding {
        return ActivityReviewModBinding.inflate(layoutInflater)
    }
}