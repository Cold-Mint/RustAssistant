package com.coldmint.rust.core.interfaces

/**
 * 实现了英文模式转换的接口
 * @property isEnglishMode Boolean
 */
interface EnglishMode {

    /**
     * 获取英文模式
     */
    fun isEnglishMode(): Boolean

    /**
     * 设置英文模式
     * @return Boolean
     */
    fun setEnglish(englishMode: Boolean)

}