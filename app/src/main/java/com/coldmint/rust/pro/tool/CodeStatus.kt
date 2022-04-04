package com.coldmint.rust.pro.tool

import com.coldmint.rust.core.SourceFile
import java.io.File

//代码状态封装类
class CodeStatus(val sourceFile: SourceFile) {
    var oldContent: String? = null
    var useLocalLanguage = true
    private var mNeedSave = false

    //设置保存完成
    fun setSaveComplete() {
        mNeedSave = false
    }

    //需要保存
    fun isNeedSave(): Boolean {
        return mNeedSave
    }

    /**
     * 文本是否内容改变
     * 此代码会影响 [CodeStatus.isNeedSave] 的结果。
     *
     * @param editText 编辑框内容
     * @return 是否改变
     */
    fun isEditTextChanged(editText: String?): Boolean {
        if (editText == null || oldContent == null) {
            return false
        }
        mNeedSave = oldContent != editText
        return mNeedSave
    }

    //获取文件
    fun getFile(): File {
        return sourceFile.file
    }

    //获取代码
    fun getCode(): String {
        return sourceFile.text
    }

    //设置代码
    fun setCode(newCode: String?) {
        sourceFile.text = newCode!!
    }


}