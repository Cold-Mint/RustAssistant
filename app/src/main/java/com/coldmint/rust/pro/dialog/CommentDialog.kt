package com.coldmint.rust.pro.dialog

import android.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.coldmint.dialog.AppDialog
import com.coldmint.dialog.BaseAppDialog
import com.coldmint.dialog.BaseBottomDialog
import com.coldmint.dialog.InputDialog
import com.coldmint.rust.pro.databinding.DialogCommentBinding
import com.coldmint.rust.pro.tool.LinkAutoCompleteHelper
import com.coldmint.rust.pro.tool.TextStyleMaker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputLayout


/**
 * 评论对话框，部分方法与输入对话框逻辑相同
 * @property autoDismiss Boolean
 * @property inputCanBeEmpty Boolean
 * @property errorTipFunction Function2<String, TextInputLayout, Unit>?
 */
class CommentDialog(context: Context) : BaseAppDialog<CommentDialog>(context) {

    val viewBinding: DialogCommentBinding by lazy {
        DialogCommentBinding.inflate(LayoutInflater.from(context))
    }


    private val adapter = ArrayAdapter<String>(
        context,
        R.layout.simple_expandable_list_item_1
    )

    private val dataList =
        listOf<String>("@user{", "@mod{", "@tag{", "@link{", "@qqGroup{", "@activate{}")

    private var submitFun: ((Button, TextInputLayout, String, AlertDialog) -> Unit)? = null

    init {
        setView(viewBinding.root)
        viewBinding.textInputLayout.isExpandedHintEnabled = false
        viewBinding.textInputEditText.threshold = 0
        viewBinding.textInputEditText.setAdapter(adapter)
        viewBinding.textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.clear()
                val text = p0.toString()
                if (text.isNotBlank()) {
                    val laseIndex = text.lastIndexOf('@')
                    if (laseIndex > -1) {
                        val sIndex = text.lastIndexOf('{')
                        if (sIndex > laseIndex) {
                            val eIndex = text.lastIndexOf('}')
                            if (eIndex > sIndex)
                            {
                                //已闭合{}。
                            }else{
                                //正在输入内容
                                adapter.add(text + '}')
                            }
                        } else {
                            //没有指定{开始位置
                            val data = text.substring(laseIndex)
                            dataList.forEach {
                                if (it.startsWith(data)) {
                                    adapter.add(text + it.substring(data.length))
                                }
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null && p0.isNotBlank()) {
                    viewBinding.positiveButton.isEnabled = true
                    TextStyleMaker.instance.setStyle(p0, { type, data ->
                        TextStyleMaker.instance.clickEvent(context, type, data)
                    }, context)
                } else {
                    viewBinding.positiveButton.isEnabled = false
                }
            }
        })
        viewBinding.negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        viewBinding.positiveButton.setOnClickListener {
            if (submitFun == null) {
                dialog.dismiss()
            } else {
                submitFun!!.invoke(
                    viewBinding.positiveButton,
                    viewBinding.textInputLayout,
                    viewBinding.textInputEditText.text.toString(), dialog
                )
            }
        }
    }

    /**
     * 设置提交函数
     * 返回true则结束对话框
     */
    fun setSubmitFun(submitFun: ((Button, TextInputLayout, String, AlertDialog) -> Unit)?): CommentDialog {
        this.submitFun = submitFun
        return this
    }
}