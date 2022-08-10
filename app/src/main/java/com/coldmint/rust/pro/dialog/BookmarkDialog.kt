package com.coldmint.rust.pro.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import com.coldmint.dialog.BaseAppDialog
import com.coldmint.rust.pro.FileManagerActivity
import com.coldmint.rust.pro.databinding.EditBookmarkBinding

class BookmarkDialog(context: Context) : BaseAppDialog<BookmarkDialog>(context) {


    private val editBookmarkBinding: EditBookmarkBinding by lazy {
        EditBookmarkBinding.inflate(LayoutInflater.from(context))
    }

    init {
        setView(editBookmarkBinding.root)
        editBookmarkBinding.pathEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                editBookmarkBinding.pathInputLayout.isErrorEnabled = false
            }
        })
        editBookmarkBinding.nameView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                editBookmarkBinding.nameInputLayout.isErrorEnabled = false
            }

        })
    }

    /**
     * 设置按钮点击事件
     * @param func Function1<[@kotlin.ParameterName] EditBookmarkBinding, Unit>
     */
    fun setButtonAction(func: ((editBookmarkBinding: EditBookmarkBinding) -> Unit)) {
        editBookmarkBinding.button.setOnClickListener {
            func.invoke(editBookmarkBinding)
        }
    }

    /**
     * 设置路径文本
     * @param text String
     */
    fun setPathViewText(text: String?) {
        editBookmarkBinding.pathEdit.setText(text)
    }


    /**
     * 返回路径名称
     * @return String
     */
    fun getPath(): String {
        return editBookmarkBinding.pathEdit.text.toString()
    }

    /**
     * 获取名称输入框内容
     */
    fun getName(): String {
        return editBookmarkBinding.nameView.text.toString()
    }

    /**
     * 设置名称输入框文本
     * @param text String?
     */
    fun setNameViewText(text: String?) {
        editBookmarkBinding.nameView.setText(text)
    }


    override fun setTitle(string: String): BookmarkDialog {
        editBookmarkBinding.titleView.text = string
        return this
    }

    override fun setTitle(stringRes: Int): BookmarkDialog {
        editBookmarkBinding.titleView.setText(stringRes)
        return this
    }


    @Deprecated("已废弃，请改用带参调用")
    override fun setPositiveButton(text: String, func: () -> Unit): BookmarkDialog {
        return super.setPositiveButton(text, func)
    }

    @Deprecated("已废弃，请改用带参调用")
    override fun setPositiveButton(textRes: Int, func: () -> Unit): BookmarkDialog {
        return super.setPositiveButton(textRes, func)
    }

    fun setPositiveButton(
        text: String,
        func: (EditBookmarkBinding) -> Unit
    ): BookmarkDialog {
        editBookmarkBinding.positiveButton.text = text
        editBookmarkBinding.positiveButton.setOnClickListener {
            func.invoke(editBookmarkBinding)
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    fun setPositiveButton(
        textRes: Int,
        func: (EditBookmarkBinding) -> Unit
    ): BookmarkDialog {
        editBookmarkBinding.positiveButton.setText(textRes)
        editBookmarkBinding.positiveButton.setOnClickListener {
            func.invoke(editBookmarkBinding)
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }


    override fun setNegativeButton(text: String, func: () -> Unit): BookmarkDialog {
        editBookmarkBinding.negativeButton.text = text
        editBookmarkBinding.negativeButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    override fun setNegativeButton(textRes: Int, func: () -> Unit): BookmarkDialog {
        editBookmarkBinding.negativeButton.setText(textRes)
        editBookmarkBinding.negativeButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }
}