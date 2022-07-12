package com.coldmint.rust.pro.tool

import android.content.Context
import com.coldmint.rust.pro.base.BaseAppendAutoCompleteHelper

/**
 * 链接自动完成帮助
 * 处理 @ at字符
 */
class LinkAutoCompleteHelper(context: Context) : BaseAppendAutoCompleteHelper(context) {
    override fun getDataList(): List<String> {
        return listOf("@mod{}","@user{}","@tag{}","@link{}","@qqGroup{}","@activate{}")
    }

    override fun getSymbol(): Char {
        return '@'
    }

}