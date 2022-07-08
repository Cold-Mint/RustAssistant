package com.coldmint.rust.pro.dialog

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import com.coldmint.rust.pro.databinding.DialogInputBinding

/**
 * 输入对话框
 * @property dialogInputBinding [@androidx.annotation.NonNull] DialogInputBinding
 * @constructor
 */
class InputDialog(context: Context) : BaseAppDialog(context) {

    val dialogInputBinding by lazy {
        DialogInputBinding.inflate(LayoutInflater.from(context))
    }

    init {
        setView(dialogInputBinding.root)
    }



}