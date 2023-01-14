package com.coldmint.rust.pro.base

import android.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.pro.interfaces.AutoCompleteHelper
import com.google.android.material.textfield.MaterialAutoCompleteTextView

/**
 * 基本附加提示列表
 * 附加提示列表用于在某些文本后附加特殊符号，例如文件和邮箱的自动补全
 * @property mDataList List<String>
 * @property mSymbol Char
 * @property adapter ArrayAdapter<String>
 * @property maxNum Int
 * @constructor
 */
abstract class BaseAppendAutoCompleteHelper(context: Context) : AutoCompleteHelper {
    abstract fun getDataList(): List<String>

    abstract fun getSymbol(): Char

    private val mDataList by lazy {
        getDataList()
    }

    private val mSymbol by lazy {
        getSymbol()
    }

    private val adapter = ArrayAdapter<String>(
        context,
        R.layout.simple_expandable_list_item_1
    )

    //最大提示数量
    private var maxNum = 3

    /**
     * 设置最大显示数量（数量必须大于0）
     * @param number Int
     */
    fun setMaxNum(number: Int) {
        if (number > 0) {
            maxNum = number
        }
    }

    /**
     * 获取邮箱提示列表
     * @param string String
     * @return List<String>?
     */

    private fun updateList(string: String) {
        adapter.clear()
        val index = string.indexOf(mSymbol)
        if (index > -1) {
            val end = string.substring(index)
            var num = 0
            LogCat.d("附加提示列表", "截取 " + end)
            for (data in mDataList) {
                if (data.startsWith(end)) {
                    num++
                    val h1 = string.substring(0..index)
                    val h2 = data.substring(
                        1
                    )
                    val value = h1 + h2
                    LogCat.d("附加提示列表", "提示 " + h1 + " | " + h2)
                    adapter.add(value)
                }
                if (num == maxNum) {
                    break
                }
            }
        } else {
            var num = 0
            LogCat.d("附加提示列表", "没有符号 " + string)
            for (email in mDataList) {
                num++
                adapter.add(string + email)
                if (num == maxNum) {
                    break
                }
            }
        }
    }


    override fun onBindAutoCompleteTextView(autoCompleteTextView: MaterialAutoCompleteTextView) {
        autoCompleteTextView.threshold = 0
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                if (text.isNotBlank()) {
                    updateList(text)
                }else{
                    adapter.clear()
                }
            }

            override fun afterTextChanged(p0: Editable?) {


            }
        })
    }
}