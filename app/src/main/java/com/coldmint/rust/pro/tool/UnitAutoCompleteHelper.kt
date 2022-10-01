package com.coldmint.rust.pro.tool

import android.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.base.BaseAppendAutoCompleteHelper
import com.coldmint.rust.pro.interfaces.AutoCompleteHelper
import com.google.android.material.textfield.MaterialAutoCompleteTextView

/**
 * 单位自动完成帮助
 */
class UnitAutoCompleteHelper(context: Context) : BaseAppendAutoCompleteHelper(context) {

    val dataList by lazy {
        val arrayList = ArrayList<String>()
        val data = AppSettings.getValue(AppSettings.Setting.SourceFileType,"ini,template")
        val line = LineParser(data)
        line.symbol = ","
        line.analyse { lineNum, lineData, isEnd ->
            arrayList.add(".${lineData}")
            true
        }
        arrayList
    }

    override fun getDataList(): List<String> {
        return dataList
    }

    override fun getSymbol(): Char {
        return '.'
    }


}