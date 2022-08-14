package com.coldmint.rust.pro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.report.ReportItemDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Report
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.adapters.ReportAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityReportListBinding
import com.coldmint.rust.pro.tool.AppSettings

/**
 * @author Cold Mint
 * @date 2022/1/9 10:39
 */
class ReportListActivity : BaseActivity<ActivityReportListBinding>() {
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getText(R.string.report_to_deal)
            setReturnButton()
            val account = AppSettings.getValue(AppSettings.Setting.Account, "")
            if (account.isBlank()) {
                showInfoToView(R.string.please_login_first)
                return
            }
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
            loadList(account)
        }
    }

    /**
     * 加载列表
     * @param account String
     */
    fun loadList(account: String) {
        Report.instance.list(object : ApiCallBack<ReportItemDataBean> {
            override fun onResponse(t: ReportItemDataBean) {
                if (t.code == ServerConfiguration.Success_Code) {
                    val dataList = t.data
                    if (dataList != null && dataList.isNotEmpty()) {
                        val adapter = ReportAdapter(this@ReportListActivity, dataList)
                        adapter.setItemEvent { i, itemReportBinding, viewHolder, data ->

                            itemReportBinding.actionView.setOnClickListener {
                                Report.instance.dispose(
                                    account,
                                    data.id,
                                    true,
                                    object : ApiCallBack<ApiResponse> {
                                        override fun onResponse(t: ApiResponse) {
                                            if (t.code == ServerConfiguration.Success_Code) {
                                                val index = dataList.indexOf(data)
                                                dataList.removeAt(index)
                                                adapter.notifyItemRemoved(index)
                                                if (dataList.isEmpty()) {
                                                    loadList(account)
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@ReportListActivity,
                                                    t.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onFailure(e: Exception) {
                                            Toast.makeText(
                                                this@ReportListActivity,
                                                R.string.network_error,
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }

                                    })
                            }

                            itemReportBinding.ignoreView.setOnClickListener {
                                Report.instance.dispose(
                                    account,
                                    data.id,
                                    false,
                                    object : ApiCallBack<ApiResponse> {
                                        override fun onResponse(t: ApiResponse) {
                                            if (t.code == ServerConfiguration.Success_Code) {
                                                val index = dataList.indexOf(data)
                                                dataList.removeAt(index)
                                                adapter.notifyItemRemoved(index)
                                                if (dataList.isEmpty()) {
                                                    loadList(account)
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@ReportListActivity,
                                                    t.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onFailure(e: Exception) {
                                            Toast.makeText(
                                                this@ReportListActivity,
                                                R.string.network_error,
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }

                                    })
                            }
                        }
                        viewBinding.tipView.isVisible = false
                        viewBinding.progressBar.isVisible = false
                        viewBinding.recyclerView.isVisible = true
                        viewBinding.recyclerView.adapter = adapter
                    } else {
                        showInfoToView(str = t.message)
                    }
                } else {
                    showInfoToView(str = t.message)
                }
            }

            override fun onFailure(e: Exception) {
                showInfoToView(R.string.network_error)
            }

        })
    }

    /**
     * 显示错误在视图上
     * @param resID Int?
     * @param str String?
     */
    fun showInfoToView(resID: Int? = null, str: String? = null) {
        viewBinding.recyclerView.isVisible = false
        viewBinding.progressBar.isVisible = false
        viewBinding.tipView.isVisible = true
        if (resID != null) {
            viewBinding.tipView.setText(resID)
        }
        if (str != null) {
            viewBinding.tipView.text = str
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityReportListBinding {
        return ActivityReportListBinding.inflate(layoutInflater)
    }
}