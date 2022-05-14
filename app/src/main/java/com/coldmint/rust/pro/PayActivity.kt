package com.coldmint.rust.pro

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
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
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import androidx.core.view.isVisible
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

    //是否超时了
    var past: Boolean = false

    //是否第一次启动
    var first = true

    //优惠的价格(用于询问退出时使用)
    var difference = 0.toDouble()

    //倒计时器
    var countDownTimer: CountDownTimer? = null

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            viewBinding.toolbar.title = getText(R.string.pay)
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
                            difference = data.originalPrice - data.price
                        }
                        viewBinding.info.text = stringBuilder.toString()
                        val timeBuilder = StringBuilder()
                        countDownTimer = object : CountDownTimer(600000, 1000) {
                            override fun onTick(p0: Long) {
                                //秒
                                timeBuilder.clear()
                                val second = p0 / 1000
                                if (second >= 60) {
                                    val minute = second / 60
                                    timeBuilder.append(minute)
                                    timeBuilder.append("分钟")
                                    timeBuilder.append(second % 60)
                                    timeBuilder.append("秒")
                                } else {
                                    timeBuilder.append(second)
                                    timeBuilder.append("秒")
                                }
                                setMoney(timeBuilder.toString(), t.data.price)
                                Log.d("秒", timeBuilder.toString())
                            }

                            override fun onFinish() {
                                past = true
                                viewBinding.typeSpinner.isEnabled = false
                                viewBinding.baseImageView.isVisible = false
                                viewBinding.payMoneyView.text = getString(R.string.order_timeout)
                                viewBinding.saveCode.isEnabled = false
                            }
                        }
                        countDownTimer!!.start()
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


    override fun onResume() {
        super.onResume()
        if (first) {
            first = false
        } else {
            if (!past) {
                MaterialDialog(this).show {
                    title(R.string.pay).message(R.string.is_paid).positiveButton(R.string.paid_yes)
                        .positiveButton {
                            finish()
                        }
                        .negativeButton(R.string.paid_no).negativeButton {
                            askingQuit()
                        }.cancelable(false).neutralButton(R.string.paid_continue)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            askingQuit()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            askingQuit()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 询问是否退出
     */
    fun askingQuit() {
        if (difference == 0.toDouble()) {
            countDownTimer?.cancel()
            finish()
        } else {
            MaterialDialog(this).show {
                title(R.string.paid_no).message(
                    text = String.format(
                        getString(R.string.preferential_price),
                        difference
                    )
                ).positiveButton(R.string.dialog_ok).positiveButton {
                    countDownTimer?.cancel()
                    finish()
                }.negativeButton(R.string.dialog_cancel).cancelable(false)
            }
        }
    }


    /**
     * 设置显示的钱数以及剩余时间
     * @param time String
     * @param money Double
     */
    fun setMoney(time: String, money: Double) {
        val payMoney = getString(R.string.pay_tip)
        val tipMoney = money.toString()
        val tip = String.format(payMoney, time, tipMoney)
        val start = tip.indexOf("在") + 1
        val end = tip.indexOf("内")
        val start2 = tip.indexOf("付") + 1
        val end2 = tip.indexOf("元")
        val spannableString = SpannableString(tip)
        val colorSpan = ForegroundColorSpan(Color.parseColor("#0099EE"))
        val colorSpan2 = ForegroundColorSpan(Color.parseColor("#0099EE"))
        spannableString.setSpan(
            colorSpan,
            start,
            end,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            colorSpan2,
            start2,
            end2,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            start2,
            end2,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        viewBinding.payMoneyView.text = spannableString
    }

    override fun getViewBindingObject(): ActivityPayBinding {
        return ActivityPayBinding.inflate(layoutInflater)
    }
}