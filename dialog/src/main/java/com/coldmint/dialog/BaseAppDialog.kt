package com.coldmint.dialog

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 基础App对话框
 */
abstract class BaseAppDialog<DialogType : AppDialog>(context: Context) :
    AppDialog {

    protected val materialAlertDialogBuilder: MaterialAlertDialogBuilder by lazy {
        MaterialAlertDialogBuilder(context)
    }

    //是否自动关闭
    protected var autoDismiss : Boolean = true

    protected lateinit var dialog: AlertDialog


    override fun dismiss() {
        dialog.dismiss()
    }

    override fun setTitle(string: String): DialogType {
        materialAlertDialogBuilder.setTitle(string)
        return this as DialogType
    }

    override fun setTitle(stringRes: Int): DialogType {
        materialAlertDialogBuilder.setTitle(stringRes)
        return this as DialogType
    }

    override fun setMessage(stringRes: Int): DialogType {
        materialAlertDialogBuilder.setMessage(stringRes)
        return this as DialogType
    }

    override fun setMessage(string: String): DialogType {
        materialAlertDialogBuilder.setMessage(string)
        return this as DialogType
    }

    override fun show(): DialogType {
        dialog = materialAlertDialogBuilder.show()
        return this as DialogType
    }

    override fun setPositiveButton(text: String, func: () -> Unit): DialogType {
        materialAlertDialogBuilder.setPositiveButton(text) { i, i2 ->
            func.invoke()
            if (autoDismiss){
                dialog.dismiss()
            }
        }
        return this as DialogType
    }

    override fun setPositiveButton(textRes: Int, func: () -> Unit): DialogType {
        materialAlertDialogBuilder.setPositiveButton(textRes) { i, i2 ->
            func.invoke()
            if (autoDismiss){
                dialog.dismiss()
            }
        }
        return this as DialogType
    }

    override fun setNegativeButton(text: String, func: () -> Unit): DialogType {
        materialAlertDialogBuilder.setNegativeButton(text) { i, i2 ->
            func.invoke()
            if (autoDismiss){
                dialog.dismiss()
            }
        }
        return this as DialogType
    }

    override fun setNegativeButton(textRes: Int, func: () -> Unit): DialogType {
        materialAlertDialogBuilder.setNegativeButton(textRes) { i, i2 ->
            func.invoke()
            if (autoDismiss){
                dialog.dismiss()
            }
        }
        return this as DialogType
    }

    override fun setNeutralButton(text: String, func: () -> Unit): DialogType {
        materialAlertDialogBuilder.setNeutralButton(text) { i, i2 ->
            func.invoke()
            if (autoDismiss){
                dialog.dismiss()
            }
        }
        return this as DialogType
    }

    override fun setNeutralButton(textRes: Int, func: () -> Unit): DialogType {
        materialAlertDialogBuilder.setNeutralButton(textRes) { i, i2 ->
            func.invoke()
            if (autoDismiss){
                dialog.dismiss()
            }
        }
        return this as DialogType
    }

    override fun setCancelable(cancelable: Boolean): DialogType {
        materialAlertDialogBuilder.setCancelable(cancelable)
        return this as DialogType
    }

    override fun setIcon(iconRes: Int): DialogType {
        materialAlertDialogBuilder.setIcon(iconRes)
        return this as DialogType
    }

    override fun setSingleChoiceItems(
        singleItems: Array<CharSequence>,
        func: (Int, CharSequence) -> Unit,
        checkedItem: Int
    ): DialogType {
        materialAlertDialogBuilder.setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
            func.invoke(which, singleItems[which])
        }
        return this as DialogType
    }

    override fun setAutoDismiss(enable: Boolean): DialogType {
        autoDismiss = enable
        return this as DialogType
    }

    override fun setView(view: View): DialogType {
        materialAlertDialogBuilder.setView(view)
        return this as DialogType
    }
}
