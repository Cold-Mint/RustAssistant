package com.coldmint.rust.pro


import android.os.Bundle
import com.coldmint.rust.pro.base.BaseActivity
import android.text.TextWatcher
import android.text.Editable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.coldmint.rust.pro.databinding.ActivityCreateModBinding
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.StringBuilder

class CreateModActivity : BaseActivity<ActivityCreateModBinding>() {

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setTitle(R.string.create_mod_lable)
            setReturnButton()
            initAction()
        }
    }

    fun initAction() {
        viewBinding.modNameEdit.addTextChangedListener {
            val name = it.toString()
            if (name.isBlank()) {
                setErrorAndInput(
                    viewBinding.modNameEdit,
                    String.format(
                        getString(R.string.please_input_value),
                        viewBinding.modDescribeInputLayout.hint.toString()
                    ),
                    viewBinding.modNameInputLayout, false
                )
                return@addTextChangedListener
            }
            val mod_directory =
                File(Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units/" + name)
            if (mod_directory.exists()) {
                setErrorAndInput(
                    viewBinding.modNameEdit,
                    getString(R.string.directory_error),
                    viewBinding.modNameInputLayout, false
                )
            } else {
                viewBinding.modNameInputLayout.isErrorEnabled = false
            }
        }

        viewBinding.modDescribeEdit.addTextChangedListener {
            viewBinding.modDescribeInputLayout.isErrorEnabled = false
        }
        //自动弹出软键盘
        viewBinding.modNameEdit.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(viewBinding.modNameEdit, InputMethodManager.SHOW_IMPLICIT)
        viewBinding.modDescribeEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString().contains("\n")) {
                    setErrorAndInput(
                        viewBinding.modDescribeEdit,
                        getString(R.string.describe_error2),
                        viewBinding.modDescribeInputLayout
                    )
                }
            }
        })
        viewBinding.createbutton.setOnClickListener(View.OnClickListener {
            val name: String = viewBinding.modNameEdit.text.toString()
            if (name.isBlank()) {
                setErrorAndInput(
                    viewBinding.modNameEdit,
                    String.format(
                        getString(R.string.please_input_value),
                        viewBinding.modNameInputLayout.hint.toString()
                    ),
                    viewBinding.modNameInputLayout
                )
                return@OnClickListener
            }
            val describe: String = viewBinding.modDescribeEdit.text.toString()
            if (describe.isEmpty()) {
                setErrorAndInput(
                    viewBinding.modDescribeEdit,
                    getString(R.string.describe_error),
                    viewBinding.modDescribeInputLayout
                )
                return@OnClickListener
            } else if (describe.contains("\n")) {
                setErrorAndInput(
                    viewBinding.modDescribeEdit,
                    getString(R.string.describe_error2),
                    viewBinding.modDescribeInputLayout
                )
                return@OnClickListener
            }
            val stringBuilder = StringBuilder()
            stringBuilder.append("[mod]\ntitle: ")
            stringBuilder.append(name)
            stringBuilder.append("\ndescription: ")
            stringBuilder.append(describe)
            val mod_directory =
                File(Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units/" + name)
            if (mod_directory.exists()) {
                setErrorAndInput(
                    viewBinding.modNameEdit,
                    getString(R.string.directory_error),
                    viewBinding.modNameInputLayout
                )
            } else {
                if (mod_directory.mkdirs()) {
                    try {
                        val fileWriter = FileWriter(mod_directory.absolutePath + "/mod-info.txt")
                        fileWriter.write(stringBuilder.toString())
                        fileWriter.close()
                        setResult(RESULT_OK)
                        finish()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this@CreateModActivity, "你的手机拒绝创建目录", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityCreateModBinding {
        return ActivityCreateModBinding.inflate(layoutInflater)
    }


}