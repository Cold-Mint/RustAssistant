package com.coldmint.rust.pro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Report
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityReportBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar

/**
 * 举报活动
 * @author Cold Mint
 * @date 2022/1/6 15:38
 */
class ReportActivity : BaseActivity<ActivityReportBinding>() {
    lateinit var type: String
    lateinit var target: String

    override fun getViewBindingObject(): ActivityReportBinding {
        return ActivityReportBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            viewBinding.toolbar.title = getText(R.string.report)
            setReturnButton()
            val bundle = intent.getBundleExtra("data")
            if (bundle == null) {
                showError("无效的启动方式")
                return
            }
            val temType = bundle.getString("type")
            if (temType == null) {
                showError("请设置启动类型")
                return
            }
            if (temType != "mod" && temType != "user") {
                showError("type只能是mod或user")
                return
            }
            type = temType
            val temTarget = bundle.getString("target")
            if (temTarget == null) {
                showError("请输入目标")
                return
            }
            val name = bundle.getString("name")
            if (name == null) {
                showError("请输入名称")
                return
            }

            val account = appSettings.getValue(AppSettings.Setting.Account, "")
            if (account.isBlank()) {
                showError(getString(R.string.please_login_first))
                return
            }
            val newTitle = String.format(getString(R.string.report_t), name)
            viewBinding.toolbar.title = newTitle

            target = temTarget
            val arrayList = ArrayList<String>()
            if (type == "mod") {
                arrayList.add(getString(R.string.picture_of_infringement))
                arrayList.add(getString(R.string.erotic_elements))
                arrayList.add(getString(R.string.bloody_elements))
                arrayList.add(getString(R.string.unauthorized_handling))
                arrayList.add(getString(R.string.describe_the_indecent))
                arrayList.add(getString(R.string.others))
            }
            val adapter =
                ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    arrayList
                )
            viewBinding.spinner.adapter = adapter

            viewBinding.describeEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val describe = s.toString()
                    checkDescribe(describe)
                }

            })

            viewBinding.reportButton.setOnClickListener {
                val actionType = viewBinding.reportButton.text.toString()
                if (actionType == getString(R.string.submit)) {
                    val describe = viewBinding.describeEdit.text.toString()
                    if (checkDescribe(describe)) {
                        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                        viewBinding.reportButton.setBackgroundColor(
                            GlobalMethod.getThemeColor(
                                this,
                                R.attr.colorPrimaryVariant
                            )
                        )
                        viewBinding.reportButton.setText(R.string.request_data)
                        Report.instance.send(
                            account,
                            type,
                            target,
                            arrayList[viewBinding.spinner.selectedItemPosition],
                            describe,
                            object : ApiCallBack<ApiResponse> {
                                override fun onResponse(t: ApiResponse) {
                                    if (t.code == ServerConfiguration.Success_Code) {
                                        viewBinding.reportButton.setText(R.string.submit_complete)
                                    } else {
                                        Snackbar.make(
                                            viewBinding.reportButton,
                                            t.message,
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        viewBinding.reportButton.setText(R.string.submit_failure)
                                    }
                                }

                                override fun onFailure(e: Exception) {
                                    viewBinding.reportButton.setText(R.string.submit_failure)
                                    showInternetError(viewBinding.reportButton, e)
                                }

                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * 检查描述
     */
    fun checkDescribe(describe: String): Boolean {
        return if (describe.isBlank()) {
            setErrorAndInput(
                viewBinding.describeEdit,
                getString(R.string.describe_error), viewBinding.describeInputLayout
            )
            false
        } else {
            viewBinding.describeInputLayout.isErrorEnabled = false
            true
        }
    }

}