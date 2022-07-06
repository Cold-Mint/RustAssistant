package com.coldmint.rust.pro.tool

import android.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import com.coldmint.rust.pro.base.BaseAppendAutoCompleteHelper
import com.coldmint.rust.pro.interfaces.AutoCompleteHelper
import com.google.android.material.textfield.MaterialAutoCompleteTextView

/**
 * 单位自动完成帮助
 */
class UnitAutoCompleteHelper(context: Context) : BaseAppendAutoCompleteHelper(context) {


    override fun getDataList(): List<String> {
        return listOf<String>(
            ".ini", ".txt", ".template"
        )
    }

    override fun getSymbol(): Char {
        return '.'
    }


}