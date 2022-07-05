package com.coldmint.rust.pro.tool

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView

/**
 * 邮箱自动完成助手
 */
class EmailAutoCompleteHelper(val context: Context) {
    private val emailList =
        listOf<String>(
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

    private val adapter = ArrayAdapter<String>(
        context,
        android.R.layout.simple_expandable_list_item_1
    )

    //最大提示数量
    val maxNum = 3

    /**
     * 获取邮箱提示列表
     * @param string String
     * @return List<String>?
     */

    private fun updateList(string: String) {
        adapter.clear()
        val index = string.indexOf("@")
        if (index > -1) {
            val end = string.substring(index)
            var num = 0
            for (email in emailList) {
                if (email.startsWith(end)) {
                    num++
                    val value = string.substring(0..index) + email.substring(
                        if (end.length > 1) {
                            end.length - 1
                        } else {
                            1
                        }
                    )
                    adapter.add(value)
                }
                if (num == maxNum) {
                    break
                }
            }
        } else {
            var num = 0
            for (email in emailList) {
                num++
                adapter.add(string + email)
                if (num == maxNum) {
                    break
                }
            }
        }
    }

    /**
     * 绑定控件
     * @param autoCompleteTextView MaterialAutoCompleteTextView
     */
    fun onBindAutoCompleteTextView(autoCompleteTextView: MaterialAutoCompleteTextView) {
        autoCompleteTextView.threshold = 0
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                if (text.isNotBlank()) {
                    updateList(text)
                }
            }

            override fun afterTextChanged(p0: Editable?) {


            }
        })
    }
}