package com.coldmint.rust.pro.interfaces

import com.google.android.material.textfield.MaterialAutoCompleteTextView

/**
 * 自动完成提示器
 */
interface AutoCompleteHelper {

    /**
     * 需要实现绑定自动完成组件的方法
     * @param autoCompleteTextView MaterialAutoCompleteTextView
     */
    fun onBindAutoCompleteTextView(autoCompleteTextView: MaterialAutoCompleteTextView)
}