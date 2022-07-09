package com.coldmint.dialog

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.coldmint.dialog.databinding.DialogInputBinding
import com.google.android.material.textfield.TextInputLayout

/**
 * 输入对话框
 * @property dialogInputBinding [@androidx.annotation.NonNull] DialogInputBinding
 * @constructor
 */
class InputDialog(context: Context) : BaseAppDialog<InputDialog>(context) {

    private val dialogInputBinding by lazy {
        DialogInputBinding.inflate(LayoutInflater.from(context))
    }

    private var autoDismiss: Boolean = true
    private var inputCanBeEmpty: Boolean = true
    private var errorTipFunction: ((String, TextInputLayout) -> Unit)? = null

    /**
     * 设置错误提示，若设置了错误提示，按钮空检查将失效
     * @param func Function2<String, TextInputLayout, Boolean>?
     */
    fun setErrorTip(func: ((String, TextInputLayout) -> Unit)?): InputDialog {
        errorTipFunction = func
        dialogInputBinding.positiveButton.isEnabled = func == null
        return this
    }

    /**
     * 设置编辑框文本
     * @param string String
     * @return InputDialog
     */
    fun setText(string: String):InputDialog{
        dialogInputBinding.textInputEditText.setText(string)
        return this
    }

    /**
     * 设置最大输入数量
     * @param number Int 小于0禁用
     * @return InputDialog
     */
    fun setMaxNumber(number: Int): InputDialog {
        if (number > 0) {
            dialogInputBinding.textInputLayout.counterMaxLength = number
            dialogInputBinding.textInputLayout.isCounterEnabled = true
        } else {
            dialogInputBinding.textInputLayout.isCounterEnabled = false
        }
        return this
    }

    /**
     * 设置是否自动关闭
     * @param enable Boolean
     */
    override fun setAutoDismiss(enable: Boolean): InputDialog {
        autoDismiss = enable
        return this
    }

    /**
     * 是否输入可空，若为true，则在空状态禁用确定按钮
     * @param can Boolean
     */
    fun setInputCanBeEmpty(can: Boolean): InputDialog {
        inputCanBeEmpty = can
        val text = dialogInputBinding.textInputEditText.text.toString()
        dialogInputBinding.positiveButton.isEnabled = text.isNotBlank()
        return this
    }


    init {
        setView(dialogInputBinding.root)
        dialogInputBinding.textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val text = p0.toString()
                if (errorTipFunction != null) {
                    errorTipFunction!!.invoke(text, dialogInputBinding.textInputLayout)
                } else {
                    dialogInputBinding.positiveButton.isEnabled =
                        !(text.isBlank() && !inputCanBeEmpty)
                }
//如果启用计数并且，超过最大字数
                if (dialogInputBinding.textInputLayout.isCounterEnabled && text.length > dialogInputBinding.textInputLayout.counterMaxLength) {
                    dialogInputBinding.textInputLayout.isErrorEnabled = true
                }
                //如果处于错误状态禁用按钮
                dialogInputBinding.positiveButton.isEnabled =
                    !dialogInputBinding.textInputLayout.isErrorEnabled
            }

        })
    }


    fun setHint(string: String): InputDialog {
        dialogInputBinding.textInputLayout.hint = string
        return this
    }

    fun setHint(stringRes: Int): InputDialog {
        dialogInputBinding.textInputLayout.setHint(stringRes)
        return this
    }

    override fun setTitle(string: String): InputDialog {
        dialogInputBinding.titleView.isVisible = true
        dialogInputBinding.titleView.text = string
        return this
    }

    override fun setTitle(stringRes: Int): InputDialog {
        dialogInputBinding.titleView.isVisible = true
        dialogInputBinding.titleView.setText(stringRes)
        return this
    }

    override fun setMessage(stringRes: Int): InputDialog {
        dialogInputBinding.messageView.isVisible = true
        dialogInputBinding.messageView.setText(stringRes)
        return this
    }

    override fun setMessage(string: String): InputDialog {
        dialogInputBinding.messageView.isVisible = true
        dialogInputBinding.messageView.text = string
        return this
    }

    @Deprecated("已废弃")
    override fun setPositiveButton(text: String, func: () -> Unit): InputDialog {
        dialogInputBinding.buttonContainer.isVisible = true
        dialogInputBinding.positiveButton.isVisible = true
        dialogInputBinding.positiveButton.text = text
        dialogInputBinding.positiveButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    @Deprecated("已废弃")
    override fun setPositiveButton(textRes: Int, func: () -> Unit): InputDialog {
        dialogInputBinding.buttonContainer.isVisible = true
        dialogInputBinding.positiveButton.isVisible = true
        dialogInputBinding.positiveButton.setText(textRes)
        dialogInputBinding.positiveButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    fun setPositiveButton(text: String, func: (String) -> Boolean): InputDialog {
        dialogInputBinding.buttonContainer.isVisible = true
        dialogInputBinding.positiveButton.isVisible = true
        dialogInputBinding.positiveButton.text = text
        dialogInputBinding.positiveButton.setOnClickListener {
            val d = func.invoke(
                dialogInputBinding.textInputEditText.text.toString()
            )
            if (d) {
                dialog.dismiss()
                return@setOnClickListener
            }
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    fun setPositiveButton(textRes: Int, func: (String) -> Boolean): InputDialog {
        dialogInputBinding.buttonContainer.isVisible = true
        dialogInputBinding.positiveButton.isVisible = true
        dialogInputBinding.positiveButton.setText(textRes)
        dialogInputBinding.positiveButton.setOnClickListener {
            val d = func.invoke(
                dialogInputBinding.textInputEditText.text.toString()
            )
            if (d) {
                dialog.dismiss()
                return@setOnClickListener
            }
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    override fun setNegativeButton(text: String, func: () -> Unit): InputDialog {
        dialogInputBinding.buttonContainer.isVisible = true
        dialogInputBinding.negativeButton.isVisible = true
        dialogInputBinding.negativeButton.text = text
        dialogInputBinding.negativeButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    override fun setNegativeButton(textRes: Int, func: () -> Unit): InputDialog {
        dialogInputBinding.buttonContainer.isVisible = true
        dialogInputBinding.negativeButton.isVisible = true
        dialogInputBinding.negativeButton.setText(textRes)
        dialogInputBinding.negativeButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }


    @Deprecated("无法使用。")
    override fun setIcon(iconRes: Int): InputDialog {
        return super.setIcon(iconRes)
    }

    @Deprecated("无法使用。")
    override fun setView(view: View): InputDialog {
        return super.setView(view)
    }

    @Deprecated("无法使用。")
    override fun setSingleChoiceItems(
        singleItems: Array<CharSequence>,
        func: (Int, CharSequence) -> Unit,
        checkedItem: Int
    ): InputDialog {
        return super.setSingleChoiceItems(singleItems, func, checkedItem)
    }
}