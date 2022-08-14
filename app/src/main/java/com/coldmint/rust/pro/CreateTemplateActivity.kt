package com.coldmint.rust.pro


import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityCreateTemplateBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.google.android.material.snackbar.Snackbar

class CreateTemplateActivity : BaseActivity<ActivityCreateTemplateBinding>() {
    private var path: String? = null
    private var editMode = false


    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setTitle(R.string.create_template)
            setReturnButton()
//            initView()
            initAction()
        }
    }

    fun initView() {
//        val intent = intent
//        val bundle = intent.getBundleExtra("data")
//        if (bundle != null) {
//            val jsonText = bundle.getString("json")
//            val temPath = bundle.getString("path")
//            if (temPath == null) {
//                showError("请输入路径")
//                return
//            }
//            path = temPath
//            if (jsonText != null) {
//                val jsonObject = JSONObject(jsonText)
//                viewBinding.templateNameEdit.setText(jsonObject.getString("name"))
//                viewBinding.templateDescribeEdit.setText(jsonObject.getString("description"))
//                viewBinding.templateDeveloperEdit.setText(jsonObject.getString("developer"))
//                viewBinding.templateUpdateEdit.setText(jsonObject.getString("update"))
//                viewBinding.templateVersionName.setText(jsonObject.getString("versionName"))
//                viewBinding.templateVersionNum.setText(jsonObject.getString("versionNum"))
//                editMode = true
//                viewBinding.createbutton.setText(R.string.edit_function)
//            }
//        }
    }

    /**
     * 检查名称，返回是否有错误
     * @return Boolean
     */
    fun checkName(): Boolean {
        val text = viewBinding.templateNameEdit.text.toString()
        return if (text.isBlank()) {
            setErrorAndInput(
                viewBinding.templateNameEdit,
                getString(R.string.template_name_error),
                viewBinding.templateNameInputLayout
            )
            true
        } else {
            viewBinding.templateNameInputLayout.isErrorEnabled = false
            false
        }
    }

    /**
     * 检查描述
     * @return Boolean
     */
    fun checkDescribe(): Boolean {
        val text = viewBinding.templateDescribeEdit.text.toString()
        return if (text.isBlank()) {
            setErrorAndInput(
                viewBinding.templateDescribeEdit,
                getString(R.string.template_description_error),
                viewBinding.templateDescribeInputLayout
            )
            true
        } else {
            viewBinding.templateDescribeInputLayout.isErrorEnabled = false
            false
        }
    }


    /**
     * 检查版本名错误
     * @return Boolean
     */
    fun checkTemplateVersionName(): Boolean {
        val text = viewBinding.templateVersionName.text.toString()
        return if (text.isBlank()) {
            setErrorAndInput(
                viewBinding.templateVersionName,
                getString(R.string.template_version_name_error),
                viewBinding.templateVersionNameLayout
            )
            true
        } else {
            viewBinding.templateVersionNameLayout.isErrorEnabled = false
            false
        }
    }


    /**
     * 检查id
     * @return Boolean
     */
    fun checkId(): Boolean {
        val text = viewBinding.templateIdEdit.text.toString()
        return if (text.isBlank()) {
            setErrorAndInput(
                viewBinding.templateIdEdit,
                getString(R.string.template_id_error),
                viewBinding.templateIdInputLayout
            )
            true
        } else {
            viewBinding.templateIdInputLayout.isErrorEnabled = false
            false
        }
    }


    fun initAction() {
        viewBinding.templateNameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                checkName()
            }

        })
        viewBinding.templateDescribeEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                checkDescribe()
            }

        })
        viewBinding.templateIdEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                checkId()
            }

        })
        viewBinding.templateVersionName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                checkTemplateVersionName()
            }

        })
        viewBinding.createbutton.setOnClickListener {
            if (checkId()) {
                return@setOnClickListener
            }
            if (checkName()) {
                return@setOnClickListener
            }
            if (checkDescribe()) {
                return@setOnClickListener
            }
            if (checkTemplateVersionName()) {
                return@setOnClickListener
            }
            if (checkId()) {
                return@setOnClickListener
            }
            var versionName = "版本号"
            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                versionName = packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            TemplatePhp.instance.createTemplatePackage(
                viewBinding.templateIdEdit.text.toString(),
                AppSettings.getValue(AppSettings.Setting.Token, ""),
                viewBinding.templateNameEdit.text.toString(),
                viewBinding.templateDescribeEdit.text.toString(),
                viewBinding.templateVersionName.text.toString(),
                versionName,
                AppOperator.getAppVersionNum(this), true, object : ApiCallBack<ApiResponse> {
                    override fun onResponse(t: ApiResponse) {
                        if (t.code == 0) {
                            finish()
                        } else {
                            if (ServerConfiguration.isEvent(t.message)) {
                                if (t.message == "@event:id重复") {
                                    viewBinding.templateIdInputLayout.error =
                                        getString(R.string.template_id_error2)
                                } else if (t.message == "@event:名称重复") {
                                    viewBinding.templateNameInputLayout.error =
                                        getString(R.string.template_name_error2)
                                }
                            } else {
                                Toast.makeText(
                                    this@CreateTemplateActivity,
                                    t.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(e: Exception) {
                        e.printStackTrace()
                        Snackbar.make(viewBinding.createbutton, e.toString(), Snackbar.LENGTH_SHORT)
                            .show()
                    }

                }
            )
//            val templateInfo = TemplateInfo(
//                now,
//                description,
//                developer,
//                name,
//                update,
//                versionName,
//                versionNum.toInt()
//            )
//            val directent = AppSettings.getValue(
//                AppSettings.Setting.TemplateDirectory,
//                this@CreateTemplateActivity.filesDir.absolutePath + "/template/"
//            )
//            val newDiectent = "$directent$name/"
//            val targetFile = File(newDiectent)
//            if (targetFile.exists()) {
//                setErrorAndInput(viewBinding.templateNameEdit, getString(R.string.template_error))
//                return@setOnClickListener
//            }
//            val templateClass = LocalTemplatePackage(targetFile)
//            if (editMode) {
//                templateClass.create(templateInfo, File(path))
//            } else {
//                templateClass.create(templateInfo)
//            }
//            finish()
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityCreateTemplateBinding {
        return ActivityCreateTemplateBinding.inflate(
            layoutInflater
        )
    }


}