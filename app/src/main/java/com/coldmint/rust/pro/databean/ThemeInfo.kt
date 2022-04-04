package com.coldmint.rust.pro.databean

class ThemeInfo
/**
 * 封装主题信息类
 *
 * @param name      名称
 * @param id        主题id
 * @param mainColor 主要颜色
 */(val name: String, val id: Int, val mainColor: String) {
    private var mDisplaysName: String? = null
    /**
     * 获取显示名称，若未设置显示名称则返回名称。
     *
     * @return
     */
    /**
     * 设置显示名称
     *
     * @param displaysName
     */
    var displaysName: String?
        get() = if (mDisplaysName == null) {
            name
        } else {
            mDisplaysName
        }
        set(displaysName) {
            mDisplaysName = displaysName
        }
}