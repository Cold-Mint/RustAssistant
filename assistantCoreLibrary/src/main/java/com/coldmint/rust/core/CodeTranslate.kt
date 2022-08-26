package com.coldmint.rust.core

/**
 * 代码编辑器类
 */
class CodeCompiler(val input: String) {


    //英文模式
    private var englishMode: Boolean = false

    /**
     * 设置是否启用英文模式 当启用时禁用翻译和编译功能
     * @param enable Boolean
     */
    fun setEnglishMode(enable: Boolean) {
        this.englishMode = enable
    }


}