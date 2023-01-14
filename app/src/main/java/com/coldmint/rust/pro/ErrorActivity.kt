package com.coldmint.rust.pro

import com.coldmint.rust.pro.base.BaseActivity
import android.content.Intent
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.coldmint.rust.pro.tool.AppSettings
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.ErrorReport
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.databean.ErrorInfo
import com.coldmint.rust.pro.databinding.ActivityErrorBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class ErrorActivity() : BaseActivity<ActivityErrorBinding>() {
    private val errorInfo by lazy {
        ErrorInfo()
    }


    private fun initView() {
        setTitle(R.string.collapse_info)
        val title = String.format(getString(R.string.error_title), getString(R.string.app_name))
        viewBinding.titleView.text = title
        GlobalMethod.requestStoragePermissions(this) {
            if (it) {
                val intent = intent
                if (intent != null) {
                    errorInfo.autoSave = false
                    errorInfo.allErrorDetails =
                        CustomActivityOnCrash.getAllErrorDetailsFromIntent(
                            this@ErrorActivity,
                            intent
                        )
                    errorInfo.activityLog = CustomActivityOnCrash.getActivityLogFromIntent(intent)
                }
                viewBinding.errorInfo.text = errorInfo.allErrorDetails
                LogCat.e("错误日志", errorInfo.allErrorDetails)
                if (AppSettings.getValue(AppSettings.Setting.ExperiencePlan, true)) {
                    val info = packageManager.getPackageInfo(packageName, 0)
                    ErrorReport.instance.send(
                        errorInfo.allErrorDetails,
                        info.versionName,
                        info.versionCode,
                        object : ApiCallBack<ApiResponse> {
                            override fun onResponse(t: ApiResponse) {
                                if (t.code == ServerConfiguration.Success_Code) {
                                    viewBinding.shareLogButton.isEnabled = false
                                    viewBinding.shareLogButton.text = getString(R.string.anonymous_send_completed)
                                }
                            }

                            override fun onFailure(e: Exception) {
                                e.printStackTrace()
                            }

                        },
                        AppSettings.getValue(
                            AppSettings.Setting.ServerAddress,
                            ServerConfiguration.website
                        )
                    )
                }
                saveLog()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (saveLog()) {
                    finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (saveLog()) {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 创建错误日志
     */
    fun saveLog(): Boolean {
        return errorInfo.save()
    }

    private fun initAction() {
        viewBinding.shareLogButton.setOnClickListener(View.OnClickListener {
            saveLog()
            if (!errorInfo.getLogFile().exists()) {
                Snackbar.make(
                    viewBinding.shareLogButton,
                    R.string.file_not_exist,
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            } else {
                FileOperator.shareFile(this@ErrorActivity, errorInfo.getLogFile())
            }
        })



        viewBinding.restartButton.setOnClickListener {
            saveLog()
            val config = CustomActivityOnCrash.getConfigFromIntent(
                intent
            )
            if (config == null) {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                CustomActivityOnCrash.restartApplication(
                    this@ErrorActivity, config
                )
            }

        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityErrorBinding {
        return ActivityErrorBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            initView()
            initAction()
        }
    }
}