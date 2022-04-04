package com.coldmint.rust.pro


import android.os.Bundle
import com.coldmint.rust.core.TemplatePackage
import com.coldmint.rust.core.dataBean.template.TemplateInfo
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityCreateTemplateBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File

class CreateTemplateActivity : BaseActivity<ActivityCreateTemplateBinding>() {
    private var path: String? = null
    private var editMode = false
    private val gson by lazy { Gson() }


    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setTitle(R.string.create_template)
            setReturnButton()
            initView()
            initAction()
        }
    }

    fun initView() {
        val intent = intent
        val bundle = intent.getBundleExtra("data")
        if (bundle != null) {
            val jsonText = bundle.getString("json")
            val temPath = bundle.getString("path")
            if (temPath == null) {
                showError("请输入路径")
                return
            }
            path = temPath
            if (jsonText != null) {
                val jsonObject = JSONObject(jsonText)
                viewBinding.templateNameEdit.setText(jsonObject.getString("name"))
                viewBinding.templateDescribeEdit.setText(jsonObject.getString("description"))
                viewBinding.templateDeveloperEdit.setText(jsonObject.getString("developer"))
                viewBinding.templateUpdateEdit.setText(jsonObject.getString("update"))
                viewBinding.templateVersionName.setText(jsonObject.getString("versionName"))
                viewBinding.templateVersionNum.setText(jsonObject.getString("versionNum"))
                editMode = true
                viewBinding.createbutton.setText(R.string.edit_function)
            }
        }
    }

    fun initAction() {
        viewBinding.createbutton.setOnClickListener {
            val name = viewBinding.templateNameEdit.text.toString()
            if (name.isEmpty()) {
                setErrorAndInput(
                    viewBinding.templateNameEdit,
                    getString(R.string.template_name_error)
                )
                return@setOnClickListener
            }
            val description = viewBinding.templateDescribeEdit.text.toString()
            if (description.isEmpty()) {
                setErrorAndInput(
                    viewBinding.templateDescribeEdit,
                    getString(R.string.template_description_error)
                )
                return@setOnClickListener
            }
            val developer = viewBinding.templateDeveloperEdit.text.toString()
            if (developer.isEmpty()) {
                setErrorAndInput(
                    viewBinding.templateDeveloperEdit,
                    getString(R.string.template_developer_error)
                )
                return@setOnClickListener
            }
            val update = viewBinding.templateUpdateEdit.text.toString()
            if (update.isEmpty()) {
                setErrorAndInput(
                    viewBinding.templateUpdateEdit,
                    getString(R.string.template_update_error)
                )
                return@setOnClickListener
            }
            val versionName = viewBinding.templateVersionName.text.toString()
            if (update.isEmpty()) {
                setErrorAndInput(
                    viewBinding.templateVersionName,
                    getString(R.string.template_version_name_error)
                )
                return@setOnClickListener
            }
            val versionNum = viewBinding.templateVersionNum.text.toString()
            if (update.isEmpty()) {
                setErrorAndInput(
                    viewBinding.templateVersionNum,
                    getString(R.string.template_version_num_error)
                )
                return@setOnClickListener
            }
            val now = AppOperator.getAppVersionNum(this, this.packageName)
            val templateInfo = TemplateInfo(
                now,
                description,
                developer,
                name,
                update,
                versionName,
                versionNum.toInt()
            )
            val directent = appSettings.getValue(
                AppSettings.Setting.TemplateDirectory,
                this@CreateTemplateActivity.filesDir.absolutePath + "/template/"
            )
            val newDiectent = "$directent$name/"
            val targetFile = File(newDiectent)
            if (targetFile.exists()) {
                setErrorAndInput(viewBinding.templateNameEdit, getString(R.string.template_error))
                return@setOnClickListener
            }
            val templateClass = TemplatePackage(targetFile)
            if (editMode) {
                templateClass.create(templateInfo, File(path))
            } else {
                templateClass.create(templateInfo)
            }
            finish()
        }
    }

    override fun getViewBindingObject(): ActivityCreateTemplateBinding {
        return ActivityCreateTemplateBinding.inflate(
            layoutInflater
        )
    }


}