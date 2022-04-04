package com.coldmint.rust.core.interfaces

import com.coldmint.rust.core.dataBean.CompileConfiguration

/**
 * @author Cold Mint
 * @date 2022/1/27 16:37
 */
interface CodeCompilerInterface {

    /**
     * 实现翻译方法
     * @param code String
     * @param translatorListener CodeTranslatorListener
     */
    fun translation(code: String, translatorListener: CodeTranslatorListener)


    /**
     * 编译方法
     * @param code String
     * @param compileConfiguration CompileConfiguration
     * @param compilerListener CodeCompilerListener?
     */
    fun compile(
        code: String,
        compileConfiguration: CompileConfiguration,
        compilerListener: CodeCompilerListener? = null
    )
}