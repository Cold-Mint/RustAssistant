package com.coldmint.rust.pro.base

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.coldmint.rust.pro.tool.AppSettings
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.pro.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.hjq.language.MultiLanguages
import java.util.*

/*主活动，所有活动都应该继承于此*/
abstract class BaseActivity<ViewBingType : ViewBinding> :
    AppCompatActivity() {


    abstract fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean)

    abstract fun getViewBindingObject(layoutInflater: LayoutInflater): ViewBingType


    protected val viewBinding: ViewBingType by lazy {
        getViewBindingObject(LayoutInflater.from(this))
    }

    protected val inputMethodManager: InputMethodManager by lazy {
        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        whenCreateActivity(savedInstanceState, false)
        setContentView(viewBinding.root)
        val toolBar = findViewById<MaterialToolbar>(R.id.toolbar)
        if (toolBar != null) {
            setSupportActionBar(toolBar)
        }
        whenCreateActivity(savedInstanceState, true)
    }


    /**
     * 展示Toast
     * @param str String
     */
    fun showToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    /**
     * 设置返回按钮
     */
    protected fun setReturnButton() {
        if (supportActionBar != null) {
            //将显示主页设置为已启用
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * 设置错误信息，并转到其编辑框
     *
     * @param editText 编辑框
     */
    protected open fun setErrorAndInput(
        editText: EditText,
        str: String,
        inputLayout: TextInputLayout? = null,
        selectAll: Boolean = true,
        requestFocus: Boolean = true
    ) {
        if (selectAll) {
            editText.selectAll()
        }
        if (inputLayout == null) {
            editText.error = str
        } else {
            inputLayout.error = str
            inputLayout.isErrorEnabled = true
        }
        if (requestFocus) {
            if (!editText.hasFocus()) {
                editText.requestFocus()
            }
        }
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 展示加载错误
     * @param msg String 错误信息
     */
    protected fun showError(msg: String) {
        MaterialDialog(this).show {
            title(R.string.error).message(text = msg).cancelable(false)
                .positiveButton(R.string.dialog_close) {
                    finish()
                }
        }
    }

    /**
     * 显示对话框
     * @param msg String
     */
    protected fun showDialog(msg: String) {
        MaterialDialog(this).show {
            message(text = msg)
        }
    }

    /**
     * 展示网络错误Snackbar,若未指名视图则显示对话框
     * @param view View 视图
     * @param exception Exception 异常信息
     */
    protected fun showInternetError(
        view: View? = null,
        exception: Exception,
        msg: String? = null
    ) {
        val thisMsg: String = if (msg == null) {
            exception.toString()
        } else {
            "${msg}\n${exception}"
        }
        if (view == null) {
            MaterialDialog(this).show {
                title(R.string.details).message(text = thisMsg)
                    .positiveButton(R.string.dialog_ok)
            }
        } else {
            Snackbar.make(
                view,
                R.string.network_error,
                Snackbar.LENGTH_SHORT
            )
                .setAction(R.string.show_details) {
                    MaterialDialog(this).show {
                        title(R.string.details).message(text = thisMsg)
                            .positiveButton(R.string.dialog_ok)
                    }
                }.show()
        }
    }


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(MultiLanguages.attach(newBase))
    }
}