package com.coldmint.rust.pro.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 基础App对话框
 */
abstract class BaseAppDialog(context: Context) : AppDialog {

    protected val materialAlertDialogBuilder: MaterialAlertDialogBuilder by lazy {
        MaterialAlertDialogBuilder(context)
    }


    override fun setTitle(string: String): AppDialog {
        materialAlertDialogBuilder.setTitle(string)
        return this
    }

    override fun setTitle(stringRes: Int): AppDialog {
        materialAlertDialogBuilder.setTitle(stringRes)
        return this
    }

    override fun setMessage(stringRes: Int): AppDialog {
        materialAlertDialogBuilder.setMessage(stringRes)
        return this
    }

    override fun setMessage(string: String): AppDialog {
        materialAlertDialogBuilder.setMessage(string)
        return this
    }

    override fun show(): AppDialog {
        materialAlertDialogBuilder.show()
        return this
    }

    override fun setPositiveButton(text: String, func: () -> Unit): AppDialog {
        materialAlertDialogBuilder.setPositiveButton(text) { i, i2 ->
            func.invoke()
        }
        return this
    }

    override fun setPositiveButton(textRes: Int, func: () -> Unit): AppDialog {
        materialAlertDialogBuilder.setPositiveButton(textRes) { i, i2 ->
            func.invoke()
        }
        return this
    }

    override fun setNegativeButton(text: String, func: () -> Unit): AppDialog {
        materialAlertDialogBuilder.setNegativeButton(text) { i, i2 ->
            func.invoke()
        }
        return this
    }

    override fun setNegativeButton(textRes: Int, func: () -> Unit): AppDialog {
        materialAlertDialogBuilder.setNegativeButton(textRes) { i, i2 ->
            func.invoke()
        }
        return this
    }

    override fun setNeutralButton(text: String, func: () -> Unit): AppDialog {
        materialAlertDialogBuilder.setNeutralButton(text) { i, i2 ->
            func.invoke()
        }
        return this
    }

    override fun setNeutralButton(textRes: Int, func: () -> Unit): AppDialog {
        materialAlertDialogBuilder.setNeutralButton(textRes) { i, i2 ->
            func.invoke()
        }
        return this
    }

    override fun setCancelable(cancelable: Boolean): AppDialog {
        materialAlertDialogBuilder.setCancelable(cancelable)
        return this
    }

    override fun setIcon(iconRes: Int): AppDialog {
        materialAlertDialogBuilder.setIcon(iconRes)
        return this
    }

    override fun setSingleChoiceItems(
        singleItems: Array<CharSequence>,
        func: (Int, CharSequence) -> Unit,
        checkedItem: Int
    ): AppDialog {
        materialAlertDialogBuilder.setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
            func.invoke(which, singleItems[which])
        }
        return this
    }


    override fun setView(view: View): AppDialog {
        materialAlertDialogBuilder.setView(view)
        return this
    }
}
