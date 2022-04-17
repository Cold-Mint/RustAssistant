package com.coldmint.rust.pro

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import com.coldmint.rust.pro.base.BaseActivity
import android.view.View
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.RegisterRequestData
import com.coldmint.rust.core.web.User
import com.coldmint.rust.pro.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {
    private var isRegister = false

    private fun initAction() {
        viewBinding.userNameView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val userName = s.toString()
                checkUserName(userName)

            }
        })

        viewBinding.accountView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val account = s.toString()
                checkAccount(account)
            }

        })

        viewBinding.passwordView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val passWord = s.toString()
                checkPassword(passWord)
            }

        })

        viewBinding.emailView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                checkEmail(email)
            }

        })

        viewBinding.mailHelpTextView.setOnClickListener {
            //预填充qq号
            val tail = "@qq.com"
            val oldEmailValue = viewBinding.emailView.text.toString()
            val hasOldQQ = oldEmailValue.endsWith(tail)
            var oldQQ = ""
            if (hasOldQQ) {
                oldQQ = oldEmailValue.subSequence(0, oldEmailValue.length - tail.length).toString()
            }
            //显示对话框
            MaterialDialog(this).show {
                title(R.string.email).message(R.string.mail_helper_tip)
                    .input(
                        hintRes = R.string.qq_number,
                        maxLength = viewBinding.emailInputLayout.counterMaxLength - tail.length,
                        inputType = InputType.TYPE_CLASS_NUMBER,
                        prefill = oldQQ
                    ) { materialDialog, charSequence ->
                        val email = "${charSequence}${tail}"
                        viewBinding.emailView.setText(email)
                        Snackbar.make(
                            viewBinding.registerButton,
                            R.string.email_fill_complete,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    .positiveButton(R.string.dialog_ok).negativeButton(R.string.dialog_close)
            }

        }


        viewBinding.registerButton.setOnClickListener(View.OnClickListener { v ->
            inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            if (!isRegister) {
                val account = viewBinding.accountView.text.toString()
                val passWord = viewBinding.passwordView.text.toString()
                val userName = viewBinding.userNameView.text.toString()
                val email = viewBinding.emailView.text.toString()

                if (!checkAccount(account)) {
                    return@OnClickListener
                }

                if (!checkUserName(userName)) {
                    return@OnClickListener
                }

                if (!checkPassword(passWord)) {
                    return@OnClickListener
                }

                if (!checkEmail(email)) {
                    return@OnClickListener
                }

                isRegister = true
                viewBinding.registerButton.setBackgroundColor(
                    GlobalMethod.getThemeColor(
                        this@RegisterActivity,
                        R.attr.colorPrimaryVariant
                    )
                )
                viewBinding.registerButton.setText(R.string.request_data)
                val appID = appSettings.getValue(AppSettings.Setting.AppID, "")
                User.register(
                    RegisterRequestData(account, passWord, userName, email, appID),
                    object : ApiCallBack<ApiResponse> {
                        override fun onResponse(apiResponse: ApiResponse) {
                            isRegister = false
                            viewBinding.registerButton.setBackgroundColor(
                                GlobalMethod.getColorPrimary(
                                    this@RegisterActivity
                                )
                            )
                            viewBinding.registerButton.setText(R.string.register)
                            if (apiResponse.code == ServerConfiguration.Success_Code) {
                                appSettings.forceSetValue(AppSettings.Setting.Account, account)
                                appSettings.forceSetValue(AppSettings.Setting.PassWord, passWord)
                                appSettings.forceSetValue(AppSettings.Setting.UserName, userName)
                                MaterialDialog(this@RegisterActivity).show {
                                    title(R.string.register_successed).message(R.string.registration_success_message)
                                        .cancelable(false).positiveButton(R.string.close) {
                                            finish()
                                        }
                                }
                            } else {
                                val data = apiResponse.data
                                if (data != null && ServerConfiguration.isEvent(data)) {
                                    when (data) {
                                        "@event:账号占用" -> {
                                            setErrorAndInput(
                                                viewBinding.accountView,
                                                getString(R.string.account_error2),
                                                viewBinding.accountInputLayout
                                            )
                                        }
                                        "@event:用户名占用" -> {
                                            setErrorAndInput(
                                                viewBinding.userNameView,
                                                getString(R.string.user_name_error),
                                                viewBinding.userNameInputLayout
                                            )
                                        }
                                        "@event:邮箱占用" -> {
                                            setErrorAndInput(
                                                viewBinding.emailView,
                                                getString(R.string.email_error2),
                                                viewBinding.emailInputLayout
                                            )
                                        }
                                        else -> {
                                            Snackbar.make(
                                                viewBinding.registerButton,
                                                apiResponse.message,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Snackbar.make(
                                        viewBinding.registerButton,
                                        apiResponse.message,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onFailure(e: Exception) {
                            isRegister = false
                            viewBinding.registerButton.setBackgroundColor(
                                GlobalMethod.getColorPrimary(
                                    this@RegisterActivity
                                )
                            )
                            viewBinding.registerButton.setText(R.string.register)
                            showInternetError(viewBinding.registerButton, e)
                        }
                    })
            }
        })
    }


    fun checkEmail(email: String): Boolean {
        return if (email.isBlank()) {
            setErrorAndInput(
                viewBinding.emailView,
                String.format(
                    getString(R.string.please_input_value),
                    viewBinding.emailInputLayout.hint.toString()
                ), viewBinding.emailInputLayout
            )
            false
        } else {
            if (email.matches(Regex("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$"))) {
                viewBinding.emailInputLayout.isErrorEnabled = false
                true
            } else {
                setErrorAndInput(
                    viewBinding.emailView,
                    getString(R.string.email_error), viewBinding.emailInputLayout, false
                )
                false
            }
        }
    }


    fun checkPassword(passWord: String): Boolean {
        return if (passWord.isBlank()) {
            setErrorAndInput(
                viewBinding.passwordView,
                getString(R.string.please_enter_your_password),
                viewBinding.passwordInputLayout
            )
            false
        } else {
            if (passWord.matches(Regex("^[a-zA-Z0-9_]{6,20}\$"))) {
                viewBinding.passwordInputLayout.isErrorEnabled = false
                true
            } else {
                setErrorAndInput(
                    viewBinding.passwordView,
                    getString(R.string.password_error),
                    viewBinding.passwordInputLayout, false
                )
                false
            }
        }
    }

    /**
     * 检查用户名
     * @param userName String
     * @return Boolean
     */
    fun checkUserName(userName: String): Boolean {
        return if (userName.isBlank()) {
            setErrorAndInput(
                viewBinding.userNameView,
                String.format(
                    getString(R.string.please_input_value),
                    viewBinding.userNameInputLayout.hint.toString()
                ),
                viewBinding.userNameInputLayout
            )
            false
        } else {
            viewBinding.userNameInputLayout.isErrorEnabled = false
            true
        }
    }

    /**
     * 检查账号是否合法
     * @param account String
     * @return Boolean
     */
    fun checkAccount(account: String): Boolean {
        if (account.isBlank()) {
            setErrorAndInput(
                viewBinding.accountView,
                getString(R.string.please_enter_your_account), viewBinding.accountInputLayout
            )
            return false
        } else {
            return if (account.matches(Regex("^[A-Za-z0-9_]+\$"))) {
                viewBinding.accountInputLayout.isErrorEnabled = false
                true
            } else {
                setErrorAndInput(
                    viewBinding.accountView,
                    getString(R.string.account_error), viewBinding.accountInputLayout, false
                )
                false

            }
        }
    }

    override fun getViewBindingObject(): ActivityRegisterBinding {
        return ActivityRegisterBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            viewBinding.toolbar.setTitle(R.string.register)
            setSupportActionBar(viewBinding.toolbar)
            setReturnButton()
            initAction()
        }
    }
}