package com.coldmint.rust.pro.edit


import android.content.Context
import android.os.Bundle
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.interfaces.EnglishMode
import com.coldmint.rust.core.tool.DebugHelper
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete
import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandleResult
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.text.TextRange
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.SymbolPairMatch

class RustLanguage(
    private val context: Context,
) : Language, EnglishMode {
    private val mRustAnalyzer: RustAnalyzer by lazy {
        RustAnalyzer()
    }

    private val autoComplete by lazy {
        RustAutoComplete(context)
    }

    //    private val autoComplete: RustAutoComplete = RustAutoComplete(context)
    private var isEnglishMode = false
    private val rustAnalyzeManager by lazy {
        RustIncrementalAnalyzeManager()
    }


    /**
     * 设置代码数据库
     * @param codeDataBean CodeDataBase
     */
    fun setCodeDataBase(codeDataBase: CodeDataBase) {
        mRustAnalyzer.setCodeDataBase(codeDataBase)
//        autoComplete.setCodeDataBase(codeDataBase)
    }

    /**
     * 设置编辑框（同步导航）
     * @param codeEditor
     */
    fun setCodeEditor(codeEditor: CodeEditor?) {
//        autoComplete.setCodeEditor(codeEditor)
    }

    fun setFileDataBase(fileDataBase: FileDataBase) {
//        autoComplete.setFileDataBase(fileDataBase)
    }


    //语法分析器
//    override fun getAnalyzer(): CodeAnalyzer {
//        return mRustAnalyzer
//    }

    //自动完成器
//    override fun getAutoCompleteProvider(): RustAutoComplete {
//        return autoComplete
//    }


//    /**
//     * 是否为自动完成字符
//     *
//     * @param ch 字符
//     * @return 逻辑值
//     */
//    override fun isAutoCompleteChar(ch: Char): Boolean {
//        return when (ch) {
//            ':', '.', '_', ' ', ',' -> false
//            else -> true
//        }
//    }


    override fun getAnalyzeManager(): AnalyzeManager {
        return rustAnalyzeManager
    }

    override fun getInterruptionLevel(): Int {
        return Language.INTERRUPTION_LEVEL_STRONG
    }

    override fun requireAutoComplete(
        p0: ContentReference,
        p1: CharPosition,
        p2: CompletionPublisher,
        p3: Bundle
    ) {
        val line = p0.getLine(p1.getLine())
        p2.addItem(RustCompletionItem("121221"))
        p2.updateList()
//        autoComplete.requireAutoComplete(line,p2,p1.getLine(),p1.getColumn())
    }

    override fun getIndentAdvance(p0: ContentReference, p1: Int, p2: Int): Int {
        return 0
    }

    //用标签
    override fun useTab(): Boolean {
        return true
    }

    override fun getFormatter(): Formatter {
        return RustFormatter()
    }

    override fun getSymbolPairs(): SymbolPairMatch {
        return SymbolPairMatch()
    }

    override fun getNewlineHandlers(): Array<NewlineHandler>? {
        //行处理程序，按下回车时
        return null
    }


    override fun destroy() {
    }

    override fun isEnglishMode(): Boolean {
        return isEnglishMode
    }

    override fun setEnglish(englishMode: Boolean) {
        mRustAnalyzer.setEnglish(englishMode)
//        autoComplete.setEnglish(englishMode)
        isEnglishMode = englishMode
    }


}