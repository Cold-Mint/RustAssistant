package com.coldmint.rust.pro

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import com.coldmint.rust.pro.base.BaseActivity
import android.view.View
import androidx.core.view.isVisible
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
import com.coldmint.rust.pro.tool.EmailAutoCompleteHelper
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
                setRegisterButtonEnable()
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
                setRegisterButtonEnable()
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
                setRegisterButtonEnable()
            }

        })

        viewBinding.confirmPasswordView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val confirmPassword = p0.toString()
                checkConfirmPassword(confirmPassword)
                setRegisterButtonEnable()
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
                setRegisterButtonEnable()
            }

        })
        val emailAutoCompleteHelper = EmailAutoCompleteHelper(this)
        emailAutoCompleteHelper.onBindAutoCompleteTextView(viewBinding.emailView)

        viewBinding.registerButton.setOnClickListener(View.OnClickListener { v ->
            inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            if (!isRegister) {
                val account = viewBinding.accountView.text.toString()
                val passWord = viewBinding.passwordView.text.toString()
                val userName = viewBinding.userNameView.text.toString()
                val email = viewBinding.emailView.text.toString()
                val confirmPassword = viewBinding.confirmPasswordView.text.toString()

                if (!checkConfirmPassword(confirmPassword)) {
                    return@OnClickListener
                }

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


    /**
     * 检查邮箱
     * @param email String
     * @return Boolean
     */
    fun checkEmail(email: String, updateView: Boolean = true): Boolean {
        return if (email.isBlank()) {
            if (updateView) {
                setErrorAndInput(
                    viewBinding.emailView,
                    String.format(
                        getString(R.string.please_input_value),
                        viewBinding.emailInputLayout.hint.toString()
                    ), viewBinding.emailInputLayout
                )
            }
            false
        } else {
            if (email.matches(Regex("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$"))) {
                if (updateView) {
                    viewBinding.emailInputLayout.isErrorEnabled = false
                }
                true
            } else {
                if (updateView) {
                    setErrorAndInput(
                        viewBinding.emailView,
                        getString(R.string.email_error), viewBinding.emailInputLayout, false
                    )
                }
                false
            }
        }
    }

    /**
     * 检查确认密码
     * @param confirmPassword String
     * @return Boolean
     */
    fun checkConfirmPassword(confirmPassword: String, updateView: Boolean = true): Boolean {
        return if (confirmPassword.isBlank()) {
            if (updateView) {
                setErrorAndInput(
                    viewBinding.confirmPasswordView, String.format(
                        getString(R.string.please_input_value),
                        viewBinding.confirmPasswordInputLayout.hint.toString()
                    ), viewBinding.confirmPasswordInputLayout
                )
            }
            false
        } else {
            val passWord = viewBinding.passwordView.text.toString()
            if (passWord == confirmPassword) {
                if (updateView) {
                    viewBinding.confirmPasswordInputLayout.isErrorEnabled = false
                }
                true
            } else {
                if (updateView) {
                    setErrorAndInput(
                        viewBinding.confirmPasswordView,
                        getString(R.string.confirm_password_error),
                        viewBinding.confirmPasswordInputLayout,
                        false
                    )
                }
                false
            }

        }
    }

    /**
     * 检查密码
     * @param passWord String
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

    /**
     * 检查用户名
     * @param userName String
     * @return Boolean
     */
    fun checkUserName(userName: String, updateView: Boolean = true): Boolean {
        return if (userName.isBlank()) {
            if (updateView) {
                setErrorAndInput(
                    viewBinding.userNameView,
                    String.format(
                        getString(R.string.please_input_value),
                        viewBinding.userNameInputLayout.hint.toString()
                    ),
                    viewBinding.userNameInputLayout
                )
            }
            false
        } else {
            if (updateView) {
                viewBinding.userNameInputLayout.isErrorEnabled = false
            }
            true
        }
    }

    /**
     * 检查账号是否合法
     * @param account String
     * @return Boolean
     */
    fun checkAccount(account: String, updateView: Boolean = true): Boolean {
        if (account.isBlank()) {
            if (updateView) {
                setErrorAndInput(
                    viewBinding.accountView,
                    getString(R.string.please_enter_your_account), viewBinding.accountInputLayout
                )
            }
            return false
        } else {
            return if (account.matches(Regex("^[A-Za-z0-9_]+\$"))) {
                if (updateView) {
                    viewBinding.accountInputLayout.isErrorEnabled = false
                }
                true
            } else {
                if (updateView) {
                    setErrorAndInput(
                        viewBinding.accountView,
                        getString(R.string.account_error), viewBinding.accountInputLayout, false
                    )
                }
                false

            }
        }
    }

    /**
     * 设置注册按钮启用状态
     */
    fun setRegisterButtonEnable() {
        val account = viewBinding.accountView.text.toString()
        val passWord = viewBinding.passwordView.text.toString()
        val userName = viewBinding.userNameView.text.toString()
        val email = viewBinding.emailView.text.toString()
        val confirmPassword = viewBinding.confirmPasswordView.text.toString()
        viewBinding.registerButton.isEnabled =
            checkConfirmPassword(confirmPassword, false) && checkAccount(
                account,
                false
            ) && checkUserName(userName, false) && checkPassword(
                passWord
            ) && checkEmail(email, false)
    }

    override fun getViewBindingObject(): ActivityRegisterBinding {
        return ActivityRegisterBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getString(R.string.register)
            setReturnButton()
            initAction()
        }
    }
}