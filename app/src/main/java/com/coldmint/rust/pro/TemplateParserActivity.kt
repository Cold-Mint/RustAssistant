package com.coldmint.rust.pro

import android.annotation.SuppressLint
import com.coldmint.rust.pro.base.BaseActivity
import org.json.JSONObject
import android.os.Bundle
import android.content.Intent
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
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.InputParserDataBean
import com.coldmint.rust.core.dataBean.IntroducingDataBean
import com.coldmint.rust.core.dataBean.ListParserDataBean
import com.coldmint.rust.core.dataBean.template.LocalTemplateFile
import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.core.interfaces.TemplateParser
import com.coldmint.rust.core.templateParser.InputParser
import com.coldmint.rust.core.templateParser.IntroducingParser
import com.coldmint.rust.core.templateParser.ListParser
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.databinding.ActivityTemplateParserBinding
import com.coldmint.rust.pro.tool.UnitAutoCompleteHelper
import com.coldmint.rust.pro.viewmodel.TemplateParserViewModel
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
                CoreDialog(this).setTitle(viewModel.getTemplateName(language)).setMessage(code)
                    .setPositiveButton(R.string.dialog_ok) {

                    }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {

            setReturnButton()
            val link = intent.getStringExtra("link")
            if (link == null) {
                Toast.makeText(this, "请设置链接", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            val isLocal = intent.getBooleanExtra("isLocal", false)
            if (isLocal) {
                Log.d("模板解析器", "读取本地模板$link")
                viewModel.setTemplate(LocalTemplateFile(File(link)))
                val templateParserList = viewModel.getTemplateParserList(this)
                templateParserList.forEach {
                    viewBinding.base.addView(it.contextView)
                }
                viewBinding.linearLayout.isVisible = false
                viewBinding.nestedScrollView.isVisible = true
                title = viewModel.getTemplateName(language)
            } else {

            }
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityTemplateParserBinding {
        return ActivityTemplateParserBinding.inflate(layoutInflater)
    }


}