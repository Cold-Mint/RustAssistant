package com.coldmint.rust.pro

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityPayBinding
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.dataBean.OrderDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.ActivationApp
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import java.io.File
import java.io.IOException


/**
 * @author Cold Mint
 * @date 2022/1/11 13:38
 */
class PayActivity : BaseActivity<ActivityPayBinding>() {
    val hashMap: HashMap<String, String> = HashMap()
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            viewBinding.toolbar.title = getText(R.string.pay)
            setSupportActionBar(viewBinding.toolbar)
            setReturnButton()
            val thisIntent = intent
            val uuid = thisIntent.getStringExtra("uuid")
            val account = thisIntent.getStringExtra("account")
            if (uuid == null || account == null) {
                showError("启动错误")
                return
            }
            hashMap[getString(R.string.qq_pay)] =
                ServerConfiguration.getRealLink("/resources/image/Payment/QQ.png")
            hashMap[getString(R.string.alipay)] =
                ServerConfiguration.getRealLink("/resources/image/Payment/Alipay.png")
            hashMap[getString(R.string.wechat_pay)] =
                ServerConfiguration.getRealLink("/resources/image/Payment/WeChat.png")
            val array = resources.getStringArray(R.array.pay_type_entries)
            viewBinding.typeSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        when (val type = array[position]) {
                            getString(R.string.qq_pay), getString(R.string.alipay), getString(R.string.wechat_pay) -> {
                                Glide.with(this@PayActivity)
                                    .load(hashMap[type]).apply(GlobalMethod.getRequestOptions())
                                    .into(viewBinding.baseImageView)
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }

            viewBinding.saveCode.setOnClickListener {
                GlobalMethod.copyText(this, uuid)
                val type = array[viewBinding.typeSpinner.selectedItemPosition]
                val link = hashMap[type]
                val appName = when (type) {
                    getString(R.string.qq_pay) -> {
                        getString(R.string.qq)
                    }
                    getString(R.string.wechat_pay) -> {
                        getString(R.string.wechat)
                    }
                    else -> {
                        getString(R.string.alipay)
                    }
                }
                val targetFile = File(AppSettings.dataRootDirectory + "/pay/" + type + ".png")
                //获取下载链接，保存二维码（如果二维码存在则不会保存）
                if (link != null && !targetFile.exists()) {
                    val okHttpClient = ServerConfiguration.initOkHttpClient()
                    val request = Request.Builder()
                        .url(link).build()
                    val call = okHttpClient.newCall(request)
                    call.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Snackbar.make(
                                viewBinding.saveCode,
                                R.string.file_download_fail,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val body = response.body
                            if (body != null) {
                                val inputStream = body.byteStream()
                                FileOperator.copyFile(
                                    inputStream,
                                    targetFile
                                )
                            } else {
                                Snackbar.make(
                                    viewBinding.saveCode,
                                    R.string.file_download_fail,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }

                    })
                }
                AppOperator.updateTheAlbum(this, targetFile)
                MaterialDialog(this).show {
                    title(R.string.pay).message(
                        text = String.format(
                            getString(R.string.pay_tip2),
                            appName
                        )
                    )
                        .positiveButton(R.string.dialog_ok).positiveButton {
                            val packName = when (type) {
                                getString(R.string.qq_pay) -> {
                                    "com.tencent.mobileqq"
                                }
                                getString(R.string.wechat_pay) -> {
                                    "com.tencent.mm"
                                }
                                else -> {
                                    "com.eg.android.AlipayGphone"
                                }
                            }
                            if (AppOperator.isAppInstalled(this@PayActivity, packName)) {
                                AppOperator.openApp(this@PayActivity, packName)
                            } else {
                                Snackbar.make(
                                    viewBinding.saveCode,
                                    String.format(getString(R.string.no_app_installed), appName),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }.negativeButton(R.string.dialog_cancel).cancelable(false)
                }

            }

            ActivationApp.instance.getOrderInfo(account, uuid, object : ApiCallBack<OrderDataBean> {
                override fun onResponse(t: OrderDataBean) {
                    if (t.code == ServerConfiguration.Success_Code) {
                        val data = t.data
                        createMoney(data.price)
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("订单名：")
                        stringBuilder.append(data.name)
                        stringBuilder.append("\n订单号：")
                        stringBuilder.append(data.flag)
                        stringBuilder.append("\n创建日期：")
                        stringBuilder.append(data.createTime)
                        stringBuilder.append("\n应付款：")
                        stringBuilder.append(data.price)
                        stringBuilder.append("元")
                        if (data.originalPrice != data.price) {
                            stringBuilder.append("\n原价：")
                            stringBuilder.append(data.originalPrice)
                            stringBuilder.append("元")
                        }
                        viewBinding.info.text = stringBuilder.toString()
                    } else {
                        showError(t.message)
                    }
                }

                override fun onFailure(e: Exception) {
                    showInternetError(viewBinding.saveCode, e)
                }

            })

        }
    }

    /**
     * 设置显示钱数
     * @param money Double
     */
    fun createMoney(money: Double) {
        val payMoney = getString(R.string.pay_tip)
        val tipMoney = money.toString()
        val tip = String.format(payMoney, tipMoney)
        val start = payMoney.indexOf("%1\$s")
        val spannableString = SpannableString(tip)
        val colorSpan = ForegroundColorSpan(Color.parseColor("#0099EE"))
        spannableString.setSpan(
            colorSpan,
            start,
            start + tipMoney.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            start + tipMoney.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
        viewBinding.payMoneyView.text = spannableString
    }

    override fun getViewBindingObject(): ActivityPayBinding {
        return ActivityPayBinding.inflate(layoutInflater)
    }
}