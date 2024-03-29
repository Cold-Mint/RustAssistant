package com.coldmint.rust.pro

import android.annotation.SuppressLint
import com.coldmint.rust.pro.base.BaseActivity
import org.json.JSONObject
import android.os.Bundle
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.coldmint.rust.pro.tool.AppSettings
import org.json.JSONException
import org.json.JSONArray
import android.view.inputmethod.EditorInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.coldmint.rust.core.SourceFile
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.InputParserDataBean
import com.coldmint.rust.core.dataBean.IntroducingDataBean
import com.coldmint.rust.core.dataBean.ListParserDataBean
import com.coldmint.rust.core.dataBean.template.LocalTemplateFile
import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.core.dataBean.template.WebTemplateData
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.TemplateParser
import com.coldmint.rust.core.templateParser.InputParser
import com.coldmint.rust.core.templateParser.IntroducingParser
import com.coldmint.rust.core.templateParser.ListParser
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.databinding.ActivityTemplateParserBinding
import com.coldmint.rust.pro.tool.UnitAutoCompleteHelper
import com.coldmint.rust.pro.viewmodel.TemplateParserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class TemplateParserActivity : BaseActivity<ActivityTemplateParserBinding>() {

    val viewModel: TemplateParserViewModel by lazy {
        TemplateParserViewModel()
    }

    private val language by lazy {
        AppSettings.getValue(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_template, menu)
        return true
    }

    @SuppressLint("CheckResult")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.preview_static_code -> {
                var code = viewModel.getCode()
                if (code.isBlank()) {
                    code = getString(R.string.not_found_data2)
                }
                MaterialAlertDialogBuilder(this).setTitle(viewModel.getTemplateName(language))
                    .setMessage(code).setPositiveButton(R.string.dialog_ok) { i, i2 ->
                    }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            val unitAutoCompleteHelper = UnitAutoCompleteHelper(this)
            unitAutoCompleteHelper.onBindAutoCompleteTextView(viewBinding.fileNameInputView)
            viewBinding.fileNameInputView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    val text = p0.toString()
                    if (text.isBlank()) {
                        viewBinding.fileNameInputLayout.error = getString(R.string.file_name_error)
                    } else {
                        viewBinding.fileNameInputLayout.isErrorEnabled = false
                    }
                }

            })
            viewBinding.fab.setOnClickListener {
                val name = viewBinding.fileNameInputView.text.toString()
                if (name.isNotBlank()) {
                    val build = viewModel.buildFile(this, name)
                    if (build) {
                        val data = Intent()
                        data.putExtra("File", viewModel.getOutputPath())
                        setResult(RESULT_OK, data)
                        finish()
                    } else {
                        Snackbar.make(
                            viewBinding.fab,
                            R.string.create_unit_failed,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            val createDirectory = intent.getStringExtra("createDirectory")
            if (createDirectory == null) {
                Toast.makeText(this, "没有设置创建目录", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            viewModel.setCreateDirectory(createDirectory)
            val link = intent.getStringExtra("link")
            if (link == null) {
                Toast.makeText(this, "请设置链接", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            val isLocal = intent.getBooleanExtra("isLocal", false)
            if (isLocal) {
                LogCat.d("模板解析器", "读取本地模板$link")
                viewModel.setTemplate(LocalTemplateFile(File(link)))
                val templateParserList = viewModel.getTemplateParserList(this)
                templateParserList.forEach {
                    viewBinding.base.addView(it.contextView)
                }
                viewBinding.linearLayout.isVisible = false
                viewBinding.nestedScrollView.isVisible = true
                title = viewModel.getTemplateName(language)
            } else {
                LogCat.d("模板解析器", "加载网络模板$link")
                TemplatePhp.instance.getTemplate(link, object : ApiCallBack<WebTemplateData> {
                    override fun onResponse(t: WebTemplateData) {
                        if (t.code == ServerConfiguration.Success_Code) {
                            viewModel.setTemplate(t.data)
                            val templateParserList =
                                viewModel.getTemplateParserList(this@TemplateParserActivity)
                            templateParserList.forEach {
                                viewBinding.base.addView(it.contextView)
                            }
                            viewBinding.linearLayout.isVisible = false
                            viewBinding.nestedScrollView.isVisible = true
                            title = viewModel.getTemplateName(language)
                        } else {
                            Toast.makeText(
                                this@TemplateParserActivity,
                                t.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }

                    override fun onFailure(e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@TemplateParserActivity,
                            R.string.network_error,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                })
            }

        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityTemplateParserBinding {
        return ActivityTemplateParserBinding.inflate(layoutInflater)
    }


}