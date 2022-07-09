package com.coldmint.rust.pro

import android.animation.ObjectAnimator
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.coldmint.dialog.InputDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.LoginRequestData
import com.coldmint.rust.core.dataBean.user.UserData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.User
import com.coldmint.rust.core.web.User.verification
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityLoginBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.EmailAutoCompleteHelper
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar
import com.gyf.immersionbar.ktx.immersionBar

class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    private var isLogin = false

    private fun initAction() {
        immersionBar {
            transparentStatusBar().statusBarDarkFont(true)
                .navigationBarColor(R.color.white_200).navigationBarDarkIcon(true)
        }
        Log.d("应用识别码", appSettings.getValue(AppSettings.Setting.AppID, "无"))

        viewBinding.accountView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val account = s.toString()
                checkAccount(account)
                //设置背景
                if (account == "kano") {
                    viewBinding.root.setBackgroundResource(R.drawable.kano)
                    ObjectAnimator.ofFloat(viewBinding.root, "alpha", 0.4f, 1.0f)
                        .setDuration(375).start()
                } else {
                    viewBinding.root.setBackgroundResource(0)
                }
                setLoginButtonEnable()
            }

        })

        val emailAutoCompleteHelper = EmailAutoCompleteHelper(this)
        emailAutoCompleteHelper.onBindAutoCompleteTextView(viewBinding.accountView)

        viewBinding.passwordView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val passWord = s.toString()
                checkPassword(passWord)
                setLoginButtonEnable()
            }

        })

        viewBinding.button.setOnClickListener(View.OnClickListener { v ->
            if (!viewBinding.checkbox.isChecked) {
                Snackbar.make(
                    viewBinding.button,
                    R.string.please_agree_the_agreement_first,
                    Snackbar.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            if (!isLogin) {
                val account = viewBinding.accountView.text.toString()
                val passWord = viewBinding.passwordView.text.toString()
                if (!checkAccount(account)) {
                    return@OnClickListener
                }
                if (!checkPassword(passWord)) {
                    return@OnClickListener
                }
                val appId = appSettings.getValue(AppSettings.Setting.AppID, "");
                isLogin = true
                viewBinding.button.setText(R.string.request_data)
                User.login(LoginRequestData(account, passWord, appId),
                    object : ApiCallBack<UserData> {
                        override fun onResponse(userData: UserData) {
                            isLogin = false
                            viewBinding.button.setText(R.string.login)
                            if (userData.code == ServerConfiguration.Success_Code) {
                                appSettings.forceSetValue(
                                    AppSettings.Setting.Account,
                                    account
                                )
                                appSettings.forceSetValue(AppSettings.Setting.PassWord, passWord)
                                appSettings.forceSetValue(
                                    AppSettings.Setting.Token,
                                    userData.data.token
                                )
                                GlobalMethod.isActive = userData.data.activation
                                //更新本地激活时间
                                val expirationTime = userData.data.expirationTime
                                val time = ServerConfiguration.toLongTime(expirationTime)
                                appSettings.forceSetValue(AppSettings.Setting.ExpirationTime, time)
                                appSettings.forceSetValue(AppSettings.Setting.LoginStatus, true)
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                when (userData.message) {
                                    "请先激活您的账户" -> {
                                        MaterialDialog(this@LoginActivity).show {
                                            title(R.string.activate_the_account).message(R.string.activate_the_account_tip)
                                                .cancelable(false)
                                            input(
                                                maxLength = 6,
                                                waitForPositiveButton = false
                                            ) { dialog, text ->
                                                if (text.length == 6) {
                                                    dialog.setActionButtonEnabled(
                                                        WhichButton.POSITIVE,
                                                        true
                                                    )
                                                } else {
                                                    dialog.setActionButtonEnabled(
                                                        WhichButton.POSITIVE,
                                                        false
                                                    )
                                                }
                                            }.positiveButton(R.string.dialog_ok, null) { dialog ->
                                                User.activateAccount(account,
                                                    dialog.getInputField().text.toString(),
                                                    object : ApiCallBack<ApiResponse> {
                                                        override fun onResponse(t: ApiResponse) {
                                                            if (t.code == ServerConfiguration.Success_Code) {
                                                                Snackbar.make(
                                                                    viewBinding.button,
                                                                    R.string.activate_the_account_ok,
                                                                    Snackbar.LENGTH_SHORT
                                                                ).show()
                                                            } else {
                                                                Snackbar.make(
                                                                    viewBinding.button,
                                                                    t.message,
                                                                    Snackbar.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }

                                                        override fun onFailure(e: Exception) {
                                                            showInternetError(viewBinding.button, e)
                                                        }

                                                    })
                                            }.negativeButton(R.string.dialog_close)
                                        }
                                    }
                                    "找不到用户" -> {
                                        setErrorAndInput(
                                            viewBinding.accountView,
                                            getString(R.string.account_error3),
                                            viewBinding.accountInputLayout
                                        )

                                    }
                                    "找不到邮箱" -> {
                                        setErrorAndInput(
                                            viewBinding.accountView,
                                            getString(R.string.account_error4),
                                            viewBinding.accountInputLayout
                                        )

                                    }
                                    "密码错误" -> {
                                        setErrorAndInput(
                                            viewBinding.passwordView,
                                            getString(R.string.password_error2),
                                            viewBinding.passwordInputLayout
                                        )
                                    }
                                    "请更改登录设备" -> {
                                        viewBinding.button.isActivated = false
                                        verification(
                                            account,
                                            passWord,
                                            appId,
                                            object : ApiCallBack<ApiResponse> {
                                                override fun onResponse(t: ApiResponse) {
                                                    viewBinding.button.isActivated = true
                                                    if (t.code == ServerConfiguration.Success_Code) {
                                                        MaterialDialog(this@LoginActivity).show {
                                                            title(R.string.verification).message(
                                                                R.string.activate_the_account_tip
                                                            )
                                                                .cancelable(false)
                                                            input(
                                                                maxLength = 6,
                                                                waitForPositiveButton = false
                                                            ) { dialog, text ->
                                                                if (text.length == 6) {
                                                                    dialog.setActionButtonEnabled(
                                                                        WhichButton.POSITIVE,
                                                                        true
                                                                    )
                                                                } else {
                                                                    dialog.setActionButtonEnabled(
                                                                        WhichButton.POSITIVE,
                                                                        false
                                                                    )
                                                                }
                                                            }.positiveButton(
                                                                R.string.dialog_ok,
                                                                null
                                                            ) { dialog ->
                                                                User.changeAppId(account,
                                                                    dialog.getInputField().text.toString(),
                                                                    appId,
                                                                    object :
                                                                        ApiCallBack<ApiResponse> {
                                                                        override fun onResponse(t: ApiResponse) {
                                                                            if (t.code == ServerConfiguration.Success_Code) {
                                                                                Snackbar.make(
                                                                                    viewBinding.button,
                                                                                    R.string.activate_the_account_ok,
                                                                                    Snackbar.LENGTH_SHORT
                                                                                ).show()
                                                                            } else {
                                                                                Snackbar.make(
                                                                                    viewBinding.button,
                                                                                    t.message,
                                                                                    Snackbar.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        }

                                                                        override fun onFailure(e: Exception) {
                                                                            showInternetError(
                                                                                viewBinding.button,
                                                                                e
                                                                            )
                                                                        }

                                                                    })
                                                            }.negativeButton(R.string.dialog_close)
                                                        }
                                                    } else {
                                                        Snackbar.make(
                                                            viewBinding.button,
                                                            t.message,
                                                            Snackbar.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }

                                                override fun onFailure(e: Exception) {
                                                    viewBinding.button.isActivated = true
                                                    isLogin = false
                                                    viewBinding.button.setBackgroundColor(
                                                        GlobalMethod.getColorPrimary(
                                                            this@LoginActivity
                                                        )
                                                    )
                                                    viewBinding.button.setText(R.string.login)
                                                    showInternetError(viewBinding.button, e)
                                                }

                                            })
                                    }
                                    else -> {
                                        Snackbar.make(
                                            viewBinding.button,
                                            userData.message,
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }

                        override fun onFailure(e: Exception) {
                            isLogin = false
                            viewBinding.button.setBackgroundColor(GlobalMethod.getColorPrimary(this@LoginActivity))
                            viewBinding.button.setText(R.string.login)
                            showInternetError(viewBinding.button, e)
                        }
                    })
            }
        })

        val agreementAgreed = getString(R.string.agreement_agreed)
        val spannableString = SpannableString(agreementAgreed)
        val serviceAgreement = getString(R.string.service_agreement)
        val privacyPolicy = getString(R.string.privacy_policy)
        val start = agreementAgreed.indexOf(serviceAgreement)
        val start2 = agreementAgreed.indexOf(privacyPolicy)
        if (start > -1 && start2 > -1) {
            spannableString.setSpan(object : ClickableSpan() {
                override fun onClick(p0: View) {
                    val link =
                        ServerConfiguration.getRealLink("/resources/agreement/service_agreement.html")
                    val thisIntent = Intent(this@LoginActivity, BrowserActivity::class.java)
                    thisIntent.putExtra("link", link)
                    startActivity(thisIntent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                    super.updateDrawState(ds)
                }
            }, start, start + serviceAgreement.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(object : ClickableSpan() {
                override fun onClick(p0: View) {
                    val link =
                        ServerConfiguration.getRealLink("/resources/agreement/privacy_policy.html")
                    val thisIntent = Intent(this@LoginActivity, BrowserActivity::class.java)
                    thisIntent.putExtra("link", link)
                    startActivity(thisIntent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                    super.updateDrawState(ds)
                }
            }, start2, start2 + privacyPolicy.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            viewBinding.checkbox.text = spannableString
            viewBinding.checkbox.setHintTextColor(Color.TRANSPARENT)
            viewBinding.checkbox.movementMethod = LinkMovementMethod.getInstance();
        }
        val agree = appSettings.getValue(AppSettings.Setting.AgreePolicy, false)
        viewBinding.checkbox.isChecked = agree
        viewBinding.checkbox.setOnCheckedChangeListener { p0, p1 ->
            setLoginButtonEnable()
            appSettings.setValue(AppSettings.Setting.AgreePolicy, p1)
        }

        viewBinding.registerView.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    RegisterActivity::class.java
                )
            )
        }
        viewBinding.changeServerView.setOnClickListener {
            InputDialog(this).setTitle(R.string.changing_the_server)
                .setMessage(R.string.changing_the_server_tip).setMaxNumber(255).setText(
                    appSettings.getValue(
                        AppSettings.Setting.ServerAddress,
                        ServerConfiguration.website
                    )
                ).setHint(R.string.server_address_configuration).setErrorTip { s, textInputLayout ->
                    textInputLayout.isErrorEnabled =
                        !(s.startsWith("http://") || s.startsWith("https://"))
                }.setPositiveButton(R.string.dialog_ok) { input ->
                    if (input.isNotBlank()) {
                        appSettings.setValue(AppSettings.Setting.ServerAddress, input)
                        ServerConfiguration.website = input
                        Snackbar.make(
                            viewBinding.button,
                            R.string.change_server_complete,
                            Snackbar.LENGTH_SHORT
                        ).show()
                        return@setPositiveButton true
                    } else {
                        return@setPositiveButton false
                    }
                }.setNegativeButton(R.string.dialog_close) {
                }.show()
        }
    }

    /**
     * 检查账号
     * @param account String
     * @param updateView Boolean
     * @return Boolean
     */
    fun checkAccount(account: String, updateView: Boolean = true): Boolean {
        return if (account.isBlank()) {
            if (updateView) {
                setErrorAndInput(
                    viewBinding.accountView,
                    getString(R.string.please_enter_your_account_or_email),
                    viewBinding.accountInputLayout
                )
            }
            false
        } else {
            if (updateView) {
                viewBinding.accountInputLayout.isErrorEnabled = false
            }
            true
        }
    }

    /**
     * 设置登录按钮
     */
    fun setLoginButtonEnable() {
        viewBinding.button.isEnabled =
            checkAccount(viewBinding.accountView.text.toString(), false) && checkPassword(
                viewBinding.passwordView.text.toString(),
                false
            ) && viewBinding.checkbox.isChecked
    }


    /**
     * 检查密码
     * @param passWord String
     * @param updateView Boolean
     * @return Boolean
     */
    fun checkPassword(passWord: String, updateView: Boolean = true): Boolean {
        return if (passWord.isBlank()) {
            if (updateView) {
                setErrorAndInput(
                    viewBinding.passwordView,
                    getString(R.string.please_enter_your_password),
                    viewBinding.passwordInputLayout
                )
            }
            false
        } else {
            if (passWord.matches(Regex("^[a-zA-Z0-9_]{6,20}\$"))) {
                if (updateView) {
                    viewBinding.passwordInputLayout.isErrorEnabled = false
                }
                true
            } else {
                if (updateView) {
                    setErrorAndInput(
                        viewBinding.passwordView,
                        getString(R.string.password_error),
                        viewBinding.passwordInputLayout, false
                    )
                }
                false
            }
        }
    }


    override fun onResume() {
        val account = appSettings.getValue(AppSettings.Setting.Account, "")
        val passWord = appSettings.getValue(AppSettings.Setting.PassWord, "")
        val inputAccount = viewBinding.accountView.text.toString()
        val inputPassWord = viewBinding.passwordView.text.toString()
        if (account.isNotBlank() && inputAccount.isEmpty()) {
            viewBinding.accountView.setText(account)
        }
        if (passWord.isNotBlank() && inputPassWord.isEmpty()) {
            viewBinding.passwordView.setText(passWord)
        }
        super.onResume()
    }

    override fun getViewBindingObject(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            initAction()
        }
    }
}