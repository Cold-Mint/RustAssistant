package com.coldmint.rust.pro

import android.annotation.SuppressLint
import com.coldmint.rust.pro.base.BaseActivity
import org.json.JSONObject
import android.os.Bundle
import android.content.Intent
import com.coldmint.rust.pro.tool.AppSettings
import org.json.JSONException
import org.json.JSONArray
import android.view.inputmethod.EditorInfo
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.coldmint.rust.core.SourceFile
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.TemplatePackage
import com.coldmint.rust.core.dataBean.InputParserDataBean
import com.coldmint.rust.core.dataBean.IntroducingDataBean
import com.coldmint.rust.core.dataBean.ListParserDataBean
import com.coldmint.rust.core.interfaces.TemplateParser
import com.coldmint.rust.core.templateParser.InputParser
import com.coldmint.rust.core.templateParser.IntroducingParser
import com.coldmint.rust.core.templateParser.ListParser
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.databinding.ActivityTemplateParserBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class TemplateParserActivity() : BaseActivity<ActivityTemplateParserBinding>() {
    private lateinit var mCreatePath: String
    private var mRootPath: String? = null
    private lateinit var mTemplatePackage: TemplatePackage
    private var working = false
    private lateinit var mJsonObject: JSONObject
    private var mCreateIndependentFolder = false
    private val gson = Gson()
    lateinit var environmentLanguage: String
    private val templateParserList = ArrayList<TemplateParser>()
    private val executorService = Executors.newSingleThreadExecutor()

    fun getTemplateName(): String {
        return if (mJsonObject.has("name_$environmentLanguage")) {
            mJsonObject.getString("name_$environmentLanguage")
        } else {
            mJsonObject.getString("name")
        }
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
                var data = mJsonObject.getString("data")
                if (data.isBlank()) {
                    data = getString(R.string.not_found_data2)
                }
                MaterialDialog(this).show {
                    title(text = getTemplateName()).message(text = data)
                        .positiveButton(R.string.dialog_ok)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 创建视图
     * @param jsonArray JSONArray js数组
     */
    private fun createView(jsonArray: JSONArray) {
        try {
            var jsonObject: JSONObject
            val len = jsonArray.length() - 1
            val setFileNameEditTextPointToNxt = false
            if (len > -1) {
                for (i in 0..len) {
                    jsonObject = jsonArray.getJSONObject(i)
                    val type = jsonObject.getString("type")
                    when (type) {
                        "input" -> {
                            if (!setFileNameEditTextPointToNxt) {
                                viewBinding.fileNameInputView.imeOptions =
                                    EditorInfo.IME_ACTION_NEXT
                            }
                            val data = gson.fromJson<InputParserDataBean>(
                                jsonObject.toString(),
                                InputParserDataBean::class.java
                            )
                            val inputParser = InputParser(this, data)
                            if (i < len) {
                                inputParser.pointToNextView()
                            }
                            addTemplateParser(inputParser)
                        }
                        "valueSelector" -> {
                            val data = gson.fromJson<ListParserDataBean>(
                                jsonObject.toString(),
                                ListParserDataBean::class.java
                            )
                            val listParser = ListParser(this, data)
                            addTemplateParser(listParser)
                        }
                        "comment" -> {
                            val data = gson.fromJson<IntroducingDataBean>(
                                jsonObject.toString(),
                                IntroducingDataBean::class.java
                            )
                            val introducingParser = IntroducingParser(this, data)
                            addTemplateParser(introducingParser)
                        }
                        else -> {
                            showError(String.format(getString(R.string.unknown_type), type))
                            break
                        }
                    }
                }
            }
        } catch (exception: JSONException) {
            showError(exception.toString())
            exception.printStackTrace()
        }
    }

    /**
     * 添加模板解析器
     * 到视图和数据集合内
     * @param templateParser T
     */
    fun <T : TemplateParser> addTemplateParser(templateParser: T) {
        viewBinding.base.addView(templateParser.contextView)
        templateParserList.add(templateParser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            999 -> if (resultCode == RESULT_OK) {
                mCreatePath = data!!.getStringExtra("Directents").toString()
                Snackbar.make(
                    viewBinding.fab,
                    R.string.change_path_ok,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            else -> if (resultCode == RESULT_OK) {
                val path = data!!.getStringExtra("File")
                //mObjectArrayList[requestCode].setinput(path)
            }
        }
    }

    private fun initAction() {
        viewBinding.fileNameInputView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val index = text.lastIndexOf('.')
                if (index > -1 && index != text.length - 1) {
                    val type = text.substring(index + 1)
                    if (fileTypeTip("ini", type, text))
                    else if (fileTypeTip(
                            "template",
                            type,
                            text
                        )
                    ) else if (fileTypeTip("txt", type, text))
                    else {
                        viewBinding.fileTypeTip.isVisible = false

                    }
                    viewBinding.fileNameInputLayout.helperText =
                        String.format(getString(R.string.file_type_tip), type)

                } else {
                    viewBinding.fileNameInputLayout.helperText =
                        getString(R.string.file_type_define)
                    viewBinding.fileTypeTip.isVisible = false
                }
            }
        })

        viewBinding.fab.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (working) {
                    Snackbar.make(
                        viewBinding.fab,
                        R.string.create_uniting,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return
                }
                viewBinding.fab.setImageResource(R.drawable.wait)
                executorService.submit {
                    working = true
                    val filename = viewBinding.fileNameInputView.text.toString()
                    var needFileType = true
                    if (filename.isBlank()) {
                        setErrorAndInput(
                            viewBinding.fileNameInputView,
                            getString(R.string.unit_name_error), viewBinding.fileNameInputLayout
                        )
                        return@submit
                    } else if (filename.contains(".")) {
                        needFileType = false
                    }
                    //真实创建目录
                    val authenticallyCreateDirectory: String = if (mCreateIndependentFolder) {
                        "$mCreatePath/$filename"
                    } else {
                        mCreatePath
                    }

                    val sourceFileClass = SourceFile(mJsonObject.getString("data"))
                    //执行活动（用户输入）
                    val len = templateParserList.size
                    if (len > 0) {
                        for (parser in templateParserList) {
                            if (parser.needParse) {
                                val input = parser.input
                                if (input.isBlank()) {
                                    runOnUiThread {
                                        parser.setError(getString(R.string.value_data_error))
                                    }
                                    return@submit
                                } else {
                                    val section = parser.section
                                    if (section == null) {
                                        sourceFileClass.writeValue(parser.code, parser.input)
                                    } else {
                                        sourceFileClass.writeValueFromSection(
                                            parser.code,
                                            parser.input,
                                            section
                                        )
                                    }
                                }
                            }
                        }
                    }

                    //复制附加内容
                    if (mJsonObject.has("attachFile")) {
                        try {
                            val jsonArray = mJsonObject.getJSONArray("attachFile")
                            var num = 0
                            while (jsonArray.getString(num) != null) {
                                val copyFile =
                                    File(mRootPath + "/" + jsonArray.getString(num))
                                if (copyFile.exists() && !copyFile.isDirectory) {
                                    val newPath =
                                        File(authenticallyCreateDirectory + "/" + copyFile.name)
                                    if (!FileOperator.copyFile(copyFile, newPath)) {
                                        runOnUiThread(Runnable {
                                            Snackbar.make(
                                                viewBinding.fab,
                                                R.string.copy_file_error_change_directory,
                                                Snackbar.LENGTH_SHORT
                                            ).setAction(
                                                getText(R.string.change_path),
                                                View.OnClickListener {
                                                    val bundle = Bundle()
                                                    val intent = Intent(
                                                        this@TemplateParserActivity,
                                                        FileManagerActivity::class.java
                                                    )
                                                    bundle.putString(
                                                        "type",
                                                        "selectDirectents"
                                                    )
                                                    bundle.putString(
                                                        "path",
                                                        authenticallyCreateDirectory
                                                    )
                                                    bundle.putString(
                                                        "rootpath",
                                                        mRootPath
                                                    )
                                                    intent.putExtra("data", bundle)
                                                    startActivityForResult(
                                                        intent,
                                                        999
                                                    )
                                                }).show()
                                            working = false
                                            viewBinding.fab.setImageResource(
                                                R.drawable.done
                                            )
                                            return@Runnable
                                        })
                                        return@submit
                                    }
                                }
                                num++
                            }
                        } catch (exception: JSONException) {
                            exception.printStackTrace()
                        }
                    }
                    val file: File = if (needFileType) {
                        File("$authenticallyCreateDirectory/${filename}.ini")
                    } else {
                        File("$authenticallyCreateDirectory/$filename")
                    }
                    val folder = File(authenticallyCreateDirectory)
                    if (!folder.exists()) {
                        folder.mkdirs()
                    }
                    if (file.exists()) {
                        setErrorAndInput(
                            viewBinding.fileNameInputView,
                            getString(R.string.unit_error),
                            viewBinding.fileNameInputLayout
                        )
                    } else {
                        if (FileOperator.writeFile(file, sourceFileClass.text)) {
                            val intent = Intent()
                            intent.putExtra("File", file.absolutePath)
                            setResult(RESULT_OK, intent)
                            finish()
                        } else {
                            runOnUiThread(Runnable {
                                Snackbar.make(
                                    viewBinding.fab,
                                    R.string.create_unit_failed,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                working = false
                                viewBinding.fab.setImageResource(R.drawable.done)
                                return@Runnable
                            })
                        }
                    }
                }
            }
        })
    }

    /**
     * 文件名提示
     */
    fun fileTypeTip(key: String, type: String, text: String): Boolean {
        if (key.startsWith(type) && key != type) {
            val spannableString =
                SpannableString(String.format(getString(R.string.file_type_tip2), key))
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val need = key.substring(type.length)
                    val newText = text + need
                    viewBinding.fileNameInputView.setText(newText)
                    viewBinding.fileNameInputView.setSelection(newText.length)
                    viewBinding.fileTypeTip.isVisible = false
                }
            }
            val start = spannableString.indexOf(key)
            val len = key.length
            spannableString.setSpan(
                clickableSpan,
                start,
                start + len,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            viewBinding.fileTypeTip.movementMethod = LinkMovementMethod.getInstance()
            viewBinding.fileTypeTip.text = spannableString
            viewBinding.fileTypeTip.isVisible = true
            return true
        }
        return false
    }

    override fun setErrorAndInput(
        editText: EditText,
        str: String,
        inputLayout: TextInputLayout?,
        selectAll: Boolean,
        requestFocus: Boolean
    ) {
        runOnUiThread {
            working = false
            viewBinding.fab.setImageResource(R.drawable.done)
            super@TemplateParserActivity.setErrorAndInput(
                editText,
                str,
                inputLayout,
                selectAll,
                requestFocus
            )
        }
    }

    override fun getViewBindingObject(): ActivityTemplateParserBinding {
        return ActivityTemplateParserBinding.inflate(
            layoutInflater
        )
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setSupportActionBar(viewBinding.toolbar)
            setReturnButton()
            viewBinding.fab.hide()
            val intent = intent
            val bundle = intent.getBundleExtra("data")
            if (bundle == null) {
                showError("意外的请求")
                return
            } else {
                val dataBasePath = appSettings.getValue(AppSettings.Setting.DatabasePath, "")
                environmentLanguage = appSettings.getValue(
                    AppSettings.Setting.AppLanguage,
                    Locale.getDefault().language
                )
                mCreateIndependentFolder =
                    appSettings.getValue(AppSettings.Setting.IndependentFolder, true)
                mRootPath = bundle.getString("rootFolder")
                mCreatePath = bundle.getString("path").toString()
                val templatePath = bundle.getString("templatePath")
                if (templatePath == null) {
                    showError("模板路径读取错误")
                    return
                }
                mTemplatePackage = TemplatePackage(File(templatePath))
                val jsonData = bundle.getString("json")
                if (jsonData == null) {
                    Toast.makeText(this, "json数据为空", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                thread {
                    try {
                        mJsonObject = JSONObject(jsonData)
                        title = getTemplateName()
                        if (mJsonObject.has("action")) {
                            runOnUiThread {
                                createView(mJsonObject.getJSONArray("action"))
                            }
                        }
                        runOnUiThread {
                            viewBinding.nestedScrollView.isVisible = true
                            viewBinding.linearLayout.isVisible = false
                            viewBinding.fileNameInputView.requestFocus()
                            val imm =
                                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(
                                viewBinding.fileNameInputView,
                                InputMethodManager.SHOW_IMPLICIT
                            )
                            viewBinding.fab.show()
                        }
                    } catch (exception: JSONException) {
                        exception.printStackTrace()
                        runOnUiThread {
                            showError(exception.toString())
                        }
                    }
                }
            }
            initAction()
        }
    }

}