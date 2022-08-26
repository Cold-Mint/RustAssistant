package com.coldmint.rust.core

import com.coldmint.rust.core.tool.FileOperator
import java.io.File

/**
 * 打开的源文件类（用于存储打开的源文件信息）
 * @author Cold Mint
 * @date 2022/2/4 16:29
 */
data class OpenedSourceFile(val file: File) {

    //次改造方法传入文件路径
    constructor(filePath: String) : this(File(filePath))

    //次构造方法传入源文件
    constructor(sourceFile: SourceFile) : this(sourceFile.file)

    //代码翻译(编辑框显示内容)
    private var translation: String = ""

    //是否需要保存
    private var needSave = false

    //临时的内容（未保存的）
    private var temText: String? = null

    /**
     * 获取编辑框显示的内容
     * @return String
     */
    fun getEditText(): String {
        return temText ?: translation
    }

    /**
     * 编辑框内容是否有更改
     * @param text String 编辑框内容
     * @return Boolean 是否改变
     */
    fun isChanged(text: String): Boolean {
        val result = translation != text
        if (result) {
            temText = text
        }
        needSave = result
        return result
    }


    /**
     * 是否需要保存
     * @return Boolean
     */
    fun isNeedSave(): Boolean {
        return needSave
    }


    /**
     * 保存文件
     * @param code String 文件内容
     * @return Boolean 是否保存成功
     */
    fun save(code: String): Boolean {
        val save = FileOperator.writeFile(file, code)
        if (save) {
            needSave = false
            val finalTemText = temText
            if (finalTemText != null) {
                translation = finalTemText
            }
        }
        return save
    }

    /**
     * 设置翻译内容
     * @param text String
     */
    fun setTranslation(text: String) {
        translation = text
    }

}