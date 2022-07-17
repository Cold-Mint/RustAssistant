package com.coldmint.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.coldmint.dialog.BaseAppDialog
import com.coldmint.dialog.databinding.DialogCoreBinding

/**
 * 核心对话框
 * @constructor
 */
class CoreDialog(context: Context) : BaseAppDialog<CoreDialog>(context) {

    val dialogCoreBinding by lazy {
        DialogCoreBinding.inflate(LayoutInflater.from(context))
    }

    private var autoDismiss: Boolean = true

    init {
        setView(dialogCoreBinding.root)
    }

    /**
     * 设置是否自动关闭
     * @param enable Boolean
     */
    override fun setAutoDismiss(enable: Boolean): CoreDialog {
        autoDismiss = enable
        return this
    }

    override fun setTitle(string: String): CoreDialog {
        dialogCoreBinding.titleView.isVisible = true
        dialogCoreBinding.titleView.text = string
        return this
    }

    /**
     * 设置选择按钮
     * @param title String?
     * @return CoreDialog
     */
    fun setCheckboxBox(title: String? = null): CoreDialog {
        dialogCoreBinding.checkbox.isVisible = title != null
        if (title != null) {
            dialogCoreBinding.checkbox.text = title
        }
        return this
    }

    /**
     * 是否选择了
     * @return Boolean
     */
    fun isChecked():Boolean{
        return dialogCoreBinding.checkbox.isChecked
    }

    override fun setTitle(stringRes: Int): CoreDialog {
        dialogCoreBinding.titleView.isVisible = true
        dialogCoreBinding.titleView.setText(stringRes)
        return this
    }

    override fun setMessage(stringRes: Int): CoreDialog {
        dialogCoreBinding.messageView.isVisible = true
        dialogCoreBinding.messageView.setText(stringRes)
        return this
    }

    override fun setMessage(string: String): CoreDialog {
        dialogCoreBinding.messageView.isVisible = true
        dialogCoreBinding.messageView.text = string
        return this
    }

    override fun setPositiveButton(text: String, func: () -> Unit): CoreDialog {
        dialogCoreBinding.buttonContainer.isVisible = true
        dialogCoreBinding.positiveButton.isVisible = true
        dialogCoreBinding.positiveButton.text = text
        dialogCoreBinding.positiveButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    override fun setPositiveButton(textRes: Int, func: () -> Unit): CoreDialog {
        dialogCoreBinding.buttonContainer.isVisible = true
        dialogCoreBinding.positiveButton.isVisible = true
        dialogCoreBinding.positiveButton.setText(textRes)
        dialogCoreBinding.positiveButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    override fun setNegativeButton(text: String, func: () -> Unit): CoreDialog {
        dialogCoreBinding.buttonContainer.isVisible = true
        dialogCoreBinding.negativeButton.isVisible = true
        dialogCoreBinding.negativeButton.text = text
        dialogCoreBinding.negativeButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }

    override fun setNegativeButton(textRes: Int, func: () -> Unit): CoreDialog {
        dialogCoreBinding.buttonContainer.isVisible = true
        dialogCoreBinding.negativeButton.isVisible = true
        dialogCoreBinding.negativeButton.setText(textRes)
        dialogCoreBinding.negativeButton.setOnClickListener {
            func.invoke()
            if (autoDismiss) {
                dialog.dismiss()
            }
        }
        return this
    }


    @Deprecated("无法使用。")
    override fun setIcon(iconRes: Int): CoreDialog {
        return super.setIcon(iconRes)
    }

    @Deprecated("无法使用。")
    override fun setView(view: View): CoreDialog {
        return super.setView(view)
    }

    @Deprecated("无法使用。")
    override fun setSingleChoiceItems(
        singleItems: Array<CharSequence>,
        func: (Int, CharSequence) -> Unit,
        checkedItem: Int
    ): CoreDialog {
        return super.setSingleChoiceItems(singleItems, func, checkedItem)
    }

}