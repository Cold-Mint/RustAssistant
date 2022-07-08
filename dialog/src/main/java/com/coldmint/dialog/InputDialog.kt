package com.coldmint.dialog

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.coldmint.dialog.databinding.DialogInputBinding

/**
 * 输入对话框
 * @property dialogInputBinding [@androidx.annotation.NonNull] DialogInputBinding
 * @constructor
 */
class InputDialog(context: Context) : BaseAppDialog(context) {

    val dialogInputBinding by lazy {
        DialogInputBinding.inflate(LayoutInflater.from(context))
    }

    private var autoDismiss: Boolean = true
    private var inputCanBeEmpty: Boolean = true

    /**
     * 设置是否自动关闭
     * @param enable Boolean
     */
    fun setAutoDismiss(enable: Boolean): InputDialog {
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
                dialogInputBinding.positiveButton.isEnabled = !(text.isBlank() && !inputCanBeEmpty)
            }

        })
    }


    override fun setTitle(string: String): AppDialog {
        dialogInputBinding.titleView.isVisible = true
        dialogInputBinding.titleView.text = string
        return this
    }

    override fun setTitle(stringRes: Int): AppDialog {
        dialogInputBinding.titleView.isVisible = true
        dialogInputBinding.titleView.setText(stringRes)
        return this
    }

    override fun setMessage(stringRes: Int): AppDialog {
        dialogInputBinding.messageView.isVisible = true
        dialogInputBinding.messageView.setText(stringRes)
        return this
    }

    override fun setMessage(string: String): AppDialog {
        dialogInputBinding.messageView.isVisible = true
        dialogInputBinding.messageView.text = string
        return this
    }

    override fun setPositiveButton(text: String, func: () -> Unit): AppDialog {
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

    override fun setPositiveButton(textRes: Int, func: () -> Unit): AppDialog {
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

    override fun setNegativeButton(text: String, func: () -> Unit): AppDialog {
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

    override fun setNegativeButton(textRes: Int, func: () -> Unit): AppDialog {
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


    @Deprecated("输入对话框无法使用。")
    override fun setIcon(iconRes: Int): AppDialog {
        return super.setIcon(iconRes)
    }

    @Deprecated("输入对话框无法使用。")
    override fun setView(view: View): AppDialog {
        return super.setView(view)
    }

    @Deprecated("输入对话框无法使用。")
    override fun setSingleChoiceItems(
        singleItems: Array<CharSequence>,
        func: (Int, CharSequence) -> Unit,
        checkedItem: Int
    ): AppDialog {
        return super.setSingleChoiceItems(singleItems, func, checkedItem)
    }
}