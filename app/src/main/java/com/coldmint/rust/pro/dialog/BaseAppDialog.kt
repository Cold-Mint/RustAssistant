package com.coldmint.rust.pro.dialog

import android.app.Dialog
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 基础App对话框
 */
class BaseAppDialog : AppDialog {

    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    override fun init(context: Context): AppDialog {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
        return this
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


}
