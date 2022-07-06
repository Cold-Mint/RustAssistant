package com.coldmint.rust.pro.tool

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import com.coldmint.rust.pro.base.BaseAppendAutoCompleteHelper
import com.coldmint.rust.pro.interfaces.AutoCompleteHelper
import com.google.android.material.textfield.MaterialAutoCompleteTextView

/**
 * 邮箱自动完成助手
 */
class EmailAutoCompleteHelper(val context: Context) : BaseAppendAutoCompleteHelper(context) {

    override fun getDataList(): List<String> {
        return listOf<String>(
            "@qq.com",
            "@gmail.com",
            "@163.com",
            "@live.com",
            "@yahoo.com",
            "@sina.com",
            "@sohu.com",
            "@139.com",
            "@126.com",
            "@aliyun.com", "@tom.com"
        )
    }

    override fun getSymbol(): Char {
        return '@'
    }


}