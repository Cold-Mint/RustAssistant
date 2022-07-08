package com.coldmint.rust.pro.dialog

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog

/**
 * 对话框管理器
 */
object DialogManager {

    enum class DialogType {
        Default, Input
    }

    /**
     * 获取对话框
     * @param context Context
     * @param dialogType DialogType
     * @return AppDialog
     */
    fun getDialog(context: Context, dialogType: DialogType = DialogType.Default): AppDialog {
        return when (dialogType) {
            DialogType.Default -> {
                RustDialog(context)
            }
            DialogType.Input -> {
                InputDialog(context)
            }
            else -> {
                RustDialog(context)
            }
        }
    }

}