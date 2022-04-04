package com.coldmint.rust.pro

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodInfo
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.pro.adapters.AttachFileAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityGlobalOperationsBinding
import com.coldmint.rust.pro.viewmodel.GlobalOperationsViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

/**
 * 全局操作活动
 */
class GlobalOperationsActivity : BaseActivity<ActivityGlobalOperationsBinding>() {
    val viewModel by lazy {
        ViewModelProvider(this).get(GlobalOperationsViewModel::class.java)
    }

    val operationList by lazy {
        resources.getStringArray(R.array.operation_list)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setSupportActionBar(viewBinding.toolbar)
            setReturnButton()
            val modPath = intent.getStringExtra("modPath")
            if (modPath == null) {
                showError("请设置模组路径")
                finish()
                return
            }
            val modClass = ModClass(File(modPath))
            val modConfiguration = modClass.modConfigurationManager
            val modConfigurationData = modConfiguration?.readData()
            if (modConfigurationData == null) {
                viewBinding.ruleInputEditText.setText(".+\\.ini|.+\\.template")
            } else {
                viewBinding.ruleInputEditText.setText(modConfigurationData.sourceFileFilteringRule)
            }
            viewModel.setModPath(modPath)
            loadTextWatcher()
            loadOnClickListener()
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
            viewModel.fileListLiveData.observe(this) {
                viewBinding.operationCard.isVisible = true
                viewBinding.resultCard.isVisible = true
                val tip = String.format(getString(R.string.list_of_affected), it.size)
                viewBinding.listTipView.text = tip
                viewBinding.recyclerView.adapter = AttachFileAdapter(this, it)
            }
            viewBinding.enableReCheckBox.setOnCheckedChangeListener { p0, p1 ->
                if (p1) {
                    viewBinding.ruleInputLayout.hint = getString(R.string.search_rule)
                } else {
                    viewBinding.ruleInputLayout.hint = getString(R.string.search_file_name)
                }
                viewBinding.button.isEnabled =
                    !checkTextIsBlank(viewBinding.ruleInputEditText, viewBinding.ruleInputLayout)
            }


            viewBinding.typeSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val data = operationList[p2]
                        when (data) {
                            getString(R.string.replace_text) -> {
                                viewBinding.actionInputLayout1.hint =
                                    getString(R.string.original_content)
                                viewBinding.actionInputLayout2.hint =
                                    getString(R.string.new_content)
                                viewBinding.actionButton.text = getString(R.string.replace)
                                viewBinding.actionInputLayout2.isVisible = true
                                val thisBlank =
                                    viewBinding.actionEditText1.text.toString().isBlank()
                                val otherBlank =
                                    viewBinding.actionEditText2.text.toString().isBlank()
                                viewBinding.actionButton.isEnabled = !(thisBlank || otherBlank)
                            }
                            getString(R.string.text_head_additional), getString(R.string.text_tail_additional) -> {
                                viewBinding.actionInputLayout1.hint =
                                    getString(R.string.content)
                                viewBinding.actionButton.text = getString(R.string.additional)
                                viewBinding.actionInputLayout2.isVisible = false
                                viewBinding.actionButton.isEnabled =
                                    !viewBinding.actionEditText1.text.toString().isBlank()
                            }
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }
        }
    }

    /**
     * 加载点击事件监听器
     */
    fun loadOnClickListener() {
        viewBinding.button.setOnClickListener {
            viewModel.findFile(
                viewBinding.ruleInputEditText.text.toString(),
                viewBinding.enableReCheckBox.isChecked
            )
        }


        viewBinding.actionButton.setOnClickListener {
            val data = operationList[viewBinding.typeSpinner.selectedItemPosition]
            val type = when (data) {
                getString(R.string.replace_text) -> {
                    GlobalOperationsViewModel.OperationType.Replace
                }
                getString(R.string.text_head_additional) -> {
                    GlobalOperationsViewModel.OperationType.BeginningAdditional
                }
                getString(R.string.text_tail_additional) -> {
                    GlobalOperationsViewModel.OperationType.EndingAdditional
                }
                else -> {
                    GlobalOperationsViewModel.OperationType.EndingAdditional
                }
            }
            viewModel.operationFile(
                type,
                viewBinding.actionEditText1.text.toString(),
                viewBinding.actionEditText2.text.toString()
            )
            MaterialDialog(this).title(text = data).message(R.string.figure_out)
                .cancelable(false)
                .positiveButton(R.string.dialog_ok).show()
        }
    }

    /**
     * 加载监听器
     */
    fun loadTextWatcher() {
        viewBinding.ruleInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                viewBinding.button.isEnabled =
                    !checkTextIsBlank(viewBinding.ruleInputEditText, viewBinding.ruleInputLayout)
            }
        })
        viewBinding.actionEditText1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val data = operationList[viewBinding.typeSpinner.selectedItemPosition]
                when (data) {
                    getString(R.string.replace_text) -> {
                        val thisBlank = checkTextIsBlank(
                            viewBinding.actionEditText1,
                            viewBinding.actionInputLayout1
                        )
                        val otherBlank = viewBinding.actionEditText2.text.toString().isBlank()
                        val equal =
                            viewBinding.actionEditText1.text.toString() == viewBinding.actionEditText2.text.toString()
                        if (equal && !thisBlank) {
                            viewBinding.actionInputLayout1.error =
                                getString(R.string.substitution_is_same)
                        } else {
                            viewBinding.actionInputLayout1.isErrorEnabled = false
                            viewBinding.actionInputLayout2.isErrorEnabled = false
                        }
                        viewBinding.actionButton.isEnabled =
                            (!thisBlank && !otherBlank) && !equal
                    }
                    getString(R.string.text_head_additional), getString(R.string.text_tail_additional) -> {
                        viewBinding.actionButton.isEnabled = !checkTextIsBlank(
                            viewBinding.actionEditText1,
                            viewBinding.actionInputLayout1
                        )
                    }
                }
            }

        })

        viewBinding.actionEditText2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val data = operationList[viewBinding.typeSpinner.selectedItemPosition]
                when (data) {
                    getString(R.string.replace_text) -> {
                        val thisBlank = checkTextIsBlank(
                            viewBinding.actionEditText2,
                            viewBinding.actionInputLayout2
                        )
                        val otherBlank = viewBinding.actionEditText1.text.toString().isBlank()
                        val equal =
                            viewBinding.actionEditText1.text.toString() == viewBinding.actionEditText2.text.toString()
                        if (equal && !thisBlank) {
                            viewBinding.actionInputLayout2.error =
                                getString(R.string.substitution_is_same)
                        } else {
                            viewBinding.actionInputLayout1.isErrorEnabled = false
                            viewBinding.actionInputLayout2.isErrorEnabled = false
                        }
                        viewBinding.actionButton.isEnabled =
                            (!thisBlank && !otherBlank) && !equal
                    }
                    getString(R.string.text_head_additional), getString(R.string.text_tail_additional) -> {
                        viewBinding.actionButton.isEnabled = !checkTextIsBlank(
                            viewBinding.actionEditText2,
                            viewBinding.actionInputLayout2
                        )
                    }
                }
            }

        })
    }


    /**
     * 检查字段是否为空
     * @param inputEditText TextInputEditText
     * @param inputLayout TextInputLayout
     * @return Boolean
     */
    fun checkTextIsBlank(inputEditText: TextInputEditText, inputLayout: TextInputLayout): Boolean {
        val text = inputEditText.text.toString()
        return if (text.isBlank()) {
            inputLayout.error =
                String.format(getString(R.string.please_input_value), inputLayout.hint.toString())
            true
        } else {
            inputLayout.isErrorEnabled = false
            false
        }
    }


    override fun getViewBindingObject(): ActivityGlobalOperationsBinding {
        return ActivityGlobalOperationsBinding.inflate(layoutInflater)
    }
}