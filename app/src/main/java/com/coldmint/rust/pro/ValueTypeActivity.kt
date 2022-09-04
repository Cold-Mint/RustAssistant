package com.coldmint.rust.pro


import android.os.Bundle
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.core.dataBean.ValueTypeDataBean
import com.google.gson.Gson
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ArrayAdapter
import android.text.TextWatcher
import android.text.Editable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.adapters.ValueAdapter
import com.coldmint.rust.pro.databinding.ActivityValueTypeBinding
import com.coldmint.rust.pro.databinding.EditValueBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import java.io.File
import java.util.ArrayList

/**
 * @author Cold Mint
 */
class ValueTypeActivity : BaseActivity<ActivityValueTypeBinding>() {
    private val valueTypeDataBeans: ArrayList<ValueTypeDataBean> by lazy {
        val temList =
            FileDataBase.readValueTypeFile(FileDataBase.getDefaultValueFile(this@ValueTypeActivity))
        temList ?: ArrayList()
    }
    private val gson = Gson()
    private var valueAdapter: ValueAdapter? = null

    /**
     * 加载列表视图
     */
    private fun loadList() {
        if (valueTypeDataBeans.size > 0) {
            viewBinding.progressBar.isVisible = false
            viewBinding.valueError.isVisible = false
            viewBinding.valueList.isVisible = true
            if (valueAdapter == null) {
                valueAdapter = ValueAdapter(this, valueTypeDataBeans)
            } else {
                valueAdapter!!.setNewDataList(valueTypeDataBeans)
            }
            valueAdapter!!.setItemEvent { i, valueItemBinding, viewHolder, valueTypeDataBean ->
                valueItemBinding.removeView.setOnClickListener {
                    valueAdapter!!.removeItem(viewHolder.adapterPosition)
                }
                valueItemBinding.editView.setOnClickListener {
                    showEditDialog(valueTypeDataBean)
                }
            }
            viewBinding.valueList.adapter = valueAdapter
        } else {
            viewBinding.progressBar.isVisible = false
            viewBinding.valueList.isVisible = false
            viewBinding.valueError.isVisible = true
            viewBinding.valueError.setText(R.string.not_found_data)
        }
    }

    /**
     * 展示窗口
     *
     * @param dataBean 如果传入null，则为添加模式
     */
    fun showEditDialog(dataBean: ValueTypeDataBean?) {
        var alertDialog: AlertDialog? = null
        var editValueBinding: EditValueBinding = EditValueBinding.inflate(layoutInflater)
        editValueBinding.autoCompleteText.setAdapter(
            ArrayAdapter(
                this@ValueTypeActivity,
                android.R.layout.simple_list_item_1,
                FileDataBase.methodCollection
            )
        )
        editValueBinding.autoCompleteText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                val data = s.toString()
                if (data.isEmpty()) {
                    alertDialog?.setMessage(getString(R.string.rule_tip))
                } else if (data.startsWith("@method ")) {
                    var end = data.indexOf('(')
                    if (end < 0) {
                        end = data.length
                    }
                    val name = data.substring("@method ".length, end)
                    var have = false
                    for (item in FileDataBase.methodCollection) {
                        if (item.startsWith("@method $name")) {
                            have = true
                            break
                        }
                    }
                    if (have) {
                        alertDialog?.setMessage(getMethodTip(name))
                    } else {
                        alertDialog?.setMessage(getString(R.string.unrecognized_method))
                    }
                } else {
                    alertDialog?.setMessage(getString(R.string.regular_expression))
                }
            }
        })
        var title: String = getString(R.string.add)
        if (dataBean != null) {
            title = getString(R.string.edit)
            editValueBinding.typeView.setText(dataBean.type)
            editValueBinding.autoCompleteText.setText(dataBean.data)
            editValueBinding.nameView.setText(dataBean.name)
            editValueBinding.descriptionView.setText(dataBean.describe)
            val scope = dataBean.scope
            when (scope) {
                FileDataBase.scopeGlobal -> {
                    editValueBinding.spacer.setSelection(0)
                }
                FileDataBase.scopeFilePath -> {
                    editValueBinding.spacer.setSelection(1)
                }
                FileDataBase.scopeThisFile -> {
                    editValueBinding.spacer.setSelection(2)
                }
            }
        }
        alertDialog =
            MaterialAlertDialogBuilder(this).setView(editValueBinding.root).setTitle(title)
                .setPositiveButton(R.string.dialog_ok) { dialog, which ->
                    val name = editValueBinding.nameView.text.toString()
                    val describe = editValueBinding.descriptionView.text.toString()
                    val scopeId = editValueBinding.spacer.selectedItemPosition
                    val scope: String
                    scope = when (scopeId) {
                        1 -> FileDataBase.scopeFilePath
                        2 -> FileDataBase.scopeThisFile
                        else -> FileDataBase.scopeGlobal
                    }
                    val type = editValueBinding.typeView.text.toString()
                    val data = editValueBinding.autoCompleteText.text.toString()
                    if (name.isEmpty()) {
                        setErrorAndInput(
                            editValueBinding.nameView,
                            getString(R.string.value_name_error)
                        )
                    }
                    if (describe.isEmpty()) {
                        setErrorAndInput(
                            editValueBinding.descriptionView,
                            getString(R.string.value_describe_error)
                        )
                    }
                    if (type.isEmpty()) {
                        setErrorAndInput(
                            editValueBinding.typeView,
                            getString(R.string.value_identifier_error)
                        )
                    }
                    if (dataBean == null && valueTypeDataBeans.size > 0) {
                        for (valueTypeDataBean in valueTypeDataBeans) {
                            val oldType = valueTypeDataBean.type
                            if (oldType == type) {
                                setErrorAndInput(
                                    editValueBinding.typeView,
                                    getString(R.string.value_idenrifier_error2)
                                )
                            }
                        }
                    }
                    if (data.isEmpty()) {
                        setErrorAndInput(
                            editValueBinding.autoCompleteText,
                            getString(R.string.value_data_error)
                        )
                    } else if (data.startsWith("@method absoluteSectionName(") && scope == FileDataBase.scopeThisFile) {
                        setErrorAndInput(
                            editValueBinding.autoCompleteText,
                            getString(R.string.value_data_error2)
                        )
                    }
                    if (dataBean == null) {
                        val valueTypeDataBean = ValueTypeDataBean(name, describe, type, data, scope)
                        valueTypeDataBeans.add(valueTypeDataBean)
                        val index = valueTypeDataBeans.size - 1
                        if (valueTypeDataBeans.size == 1) {
                            loadList()
                        } else {
                            valueAdapter!!.notifyItemChanged(index)
                        }
                    } else {
                        dataBean.data = data
                        dataBean.type = type
                        dataBean.describe = describe
                        dataBean.name = name
                        dataBean.scope = scope
                        val index = valueTypeDataBeans.indexOf(dataBean)
                        valueAdapter!!.notifyItemChanged(index)
                    }
                }.setNegativeButton(R.string.dialog_close, null).create()
        alertDialog.show()
    }

    /**
     * 获取方法提示
     *
     * @param methodName 方法名
     * @return 方法信息
     */
    private fun getMethodTip(methodName: String): String {
        return if (methodName == "readValue") {
            getString(R.string.read_value_tip)
        } else if (methodName == "absoluteSectionName") {
            getString(R.string.absolute_section_name)
        } else if (methodName == "fileName") {
            getString(R.string.method_file_name)
        } else {
            getString(R.string.unrecognized_method)
        }
    }

    /**
     * 保持数据到文件内
     */
    private fun saveData() {
        val jsonArray = JSONArray()
        if (valueTypeDataBeans.size > 0) {
            for (dataBean in valueTypeDataBeans) {
                val s = gson.toJson(dataBean)
                try {
                    val jsonObject = JSONObject(s)
                    jsonArray.put(jsonObject)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        FileOperator.writeFile(
            FileDataBase.getDefaultValueFile(this),
            jsonArray.toString()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                saveData()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            saveData()
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityValueTypeBinding {
        return ActivityValueTypeBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getText(R.string.value_type_manager)
            viewBinding.valueList.layoutManager = LinearLayoutManager(this@ValueTypeActivity)
            setReturnButton()
            loadList()
            viewBinding.fab.setOnClickListener { showEditDialog(null) }
        }
    }
}