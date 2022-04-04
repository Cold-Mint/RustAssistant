package com.coldmint.rust.pro.interfaces

import com.coldmint.rust.pro.databean.ThemeInfo


interface ThemeItemListener {
    /**
     * 当选中主题更改
     *
     * @param info 主题信息
     */
    fun whenUseTheme(info: ThemeInfo)
}