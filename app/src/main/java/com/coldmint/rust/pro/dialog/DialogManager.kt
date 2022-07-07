package com.coldmint.rust.pro.dialog

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog

/**
 * 对话框管理器
 */
object DialogManager {

    fun getDialog(context: Context): AppDialog {
        MaterialDialog
        return BaseAppDialog().init(context)
    }

}