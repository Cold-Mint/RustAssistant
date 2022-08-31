package com.coldmint.rust.pro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.User
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databean.CodeData
import com.coldmint.rust.pro.databinding.ActivityChangePasswordBinding
import com.coldmint.rust.pro.tool.EmailAutoCompleteHelper

class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        setReturnButton()
        title = getString(R.string.retrieve_password)
        val emailAutoCompleteHelper = EmailAutoCompleteHelper(this)
        emailAutoCompleteHelper.onBindAutoCompleteTextView(viewBinding.accountEdit)
        viewBinding.accountEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val userName = s.toString()
                checkAccount(userName)
                viewBinding.verificationCodeLayout.isErrorEnabled = false
                setButtonEnable()
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
                setButtonEnable()
            }

        })

        viewBinding.confirmPasswordView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val passWord = s.toString()
                checkConfirmPassword(passWord)
                setButtonEnable()
            }
        })

        viewBinding.verificationCodeView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val code = s.toString()
                checkCode(code)
                setButtonEnable()
            }

        })
        viewBinding.verificationCodeLayout.setEndIconOnClickListener {
            val account = viewBinding.accountEdit.text.toString()
            if (account.isNotBlank()) {
                User.requestChangePassword(account, object : ApiCallBack<ApiResponse> {
                    override fun onResponse(t: ApiResponse) {
                        if (t.code == ServerConfiguration.Success_Code) {
                            viewBinding.verificationCodeLayout.endIconDrawable = null
                            CoreDialog(this@ChangePasswordActivity).setTitle(R.string.retrieve_password)
                                .setMessage(t.message).setPositiveButton(R.string.dialog_ok) {

                                }.setCancelable(false).show()
                        } else {
                            viewBinding.verificationCodeLayout.error = t.message
                        }
                    }

                    override fun onFailure(e: Exception) {
                        e.printStackTrace()
                        viewBinding.verificationCodeLayout.error = e.toString()
                    }

                })
            } else {
                val tip =
                    String.format(
                        getString(R.string.please_input_value),
                        viewBinding.accountInputLayout.hint.toString()
                    )
                viewBinding.verificationCodeLayout.error = tip
            }
        }
        viewBinding.button.setOnClickListener {
            val account = viewBinding.accountEdit.text.toString()
            val passWord = viewBinding.passwordView.text.toString()
            val code = viewBinding.verificationCodeView.text.toString()
            User.changePassword(account, code.toInt(), passWord, object : ApiCallBack<ApiResponse> {
                override fun onResponse(t: ApiResponse) {
                    if (t.code == ServerConfiguration.Success_Code) {
                        CoreDialog(this@ChangePasswordActivity).setTitle(R.string.retrieve_password)
                            .setMessage(R.string.save_complete2)
                            .setPositiveButton(R.string.dialog_ok) {
                                finish()
                            }.setCancelable(false).show()
                    } else {
                        setErrorAndInput(
                            viewBinding.verificationCodeView,
                            t.message,
                            viewBinding.verificationCodeLayout
                        )
                    }
                }

                override fun onFailure(e: Exception) {
                    e.printStackTrace()
                    showToast(e.toString())
                }

            })
        }
    }

    /**
     * 设置注册按钮启用状态
     */
    fun setButtonEnable() {
        val account = viewBinding.accountEdit.text.toString()
        val passWord = viewBinding.passwordView.text.toString()
        val code = viewBinding.verificationCodeView.text.toString()
        val confirmPassword = viewBinding.confirmPasswordView.text.toString()
        viewBinding.button.isEnabled =
            checkConfirmPassword(confirmPassword, false) && checkAccount(
                account,
                false
            ) && checkCode(code, false) && checkPassword(
                passWord, false
            )
    }


    private fun checkCode(code: String, updateView: Boolean = true): Boolean {
        return if (code.isBlank()) {
            if (updateView) {
                setErrorAndInput(
                    viewBinding.verificationCodeView,
                    String.format(
                        getString(R.string.please_input_value),
                        viewBinding.verificationCodeLayout.hint.toString()
                    ),
                    viewBinding.verificationCodeLayout
                )
            }
            false
        } else {
            if (updateView) {
                viewBinding.verificationCodeLayout.isErrorEnabled = false
            }
            true
        }
    }

    private fun checkAccount(userName: String, updateView: Boolean = true): Boolean {
        return if (userName.isBlank()) {
            if (updateView) {
                setErrorAndInput(
                    viewBinding.accountEdit,
                    String.format(
                        getString(R.string.please_input_value),
                        viewBinding.accountInputLayout.hint.toString()
                    ),
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

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityChangePasswordBinding {
        return ActivityChangePasswordBinding.inflate(layoutInflater)
    }
}