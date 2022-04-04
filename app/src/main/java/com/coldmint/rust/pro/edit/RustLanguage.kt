package com.coldmint.rust.pro.edit


import android.content.Context
import android.util.Log
import com.coldmint.rust.core.CodeCompiler2
import com.coldmint.rust.core.dataBean.dataset.CodeDataBean
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.interfaces.EnglishMode

import io.github.rosemoe.sora.data.CompletionItem
import io.github.rosemoe.sora.interfaces.AutoCompleteProvider
import io.github.rosemoe.sora.interfaces.CodeAnalyzer
import io.github.rosemoe.sora.interfaces.EditorLanguage
import io.github.rosemoe.sora.interfaces.NewlineHandler
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.SymbolPairMatch

class RustLanguage(
    private val context: Context,
) : EditorLanguage, EnglishMode {
    private val mRustAnalyzer: RustAnalyzer by lazy {
        RustAnalyzer()
    }
    private val autoComplete: RustAutoComplete2 = RustAutoComplete2(context)
    private var isEnglishMode = false

    /**
     * 设置代码数据库
     * @param codeDataBean CodeDataBase
     */
    fun setCodeDataBase(codeDataBase: CodeDataBase) {
        mRustAnalyzer.setCodeDataBase(codeDataBase)
        autoComplete.setCodeDataBase(codeDataBase)
    }

    /**
     * 设置编辑框（同步导航）
     * @param codeEditor
     */
    fun setCodeEditor(codeEditor: CodeEditor?) {
        autoComplete.setCodeEditor(codeEditor)
    }

    fun setFileDataBase(fileDataBase: FileDataBase) {
        autoComplete.setFileDataBase(fileDataBase)
    }


    //语法分析器
    override fun getAnalyzer(): CodeAnalyzer {
        return mRustAnalyzer
    }

    //自动完成器
    override fun getAutoCompleteProvider(): RustAutoComplete2 {
        return autoComplete
    }


    /**
     * 是否为自动完成字符
     *
     * @param ch 字符
     * @return 逻辑值
     */
    override fun isAutoCompleteChar(ch: Char): Boolean {
        return when (ch) {
            ':', '.', '_', ' ', ',' -> false
            else -> true
        }
    }

    //获取分割进度
    override fun getIndentAdvance(content: String): Int {
        return 0
    }

    //用标签
    override fun useTab(): Boolean {
        return false
    }

    //格式化
    override fun format(text: CharSequence): CharSequence {
        return CodeCompiler2.format(text.toString())
    }

    //符号配对之间的匹配
    override fun getSymbolPairs(): SymbolPairMatch? {
        return null
    }

    //获取新线的处理程序
    override fun getNewlineHandlers(): Array<NewlineHandler> {
        return arrayOf()
    }

    override fun isEnglishMode(): Boolean {
        return isEnglishMode
    }

    override fun setEnglish(englishMode: Boolean) {
        mRustAnalyzer.setEnglish(englishMode)
        autoComplete.setEnglish(englishMode)
        isEnglishMode = englishMode
    }


}