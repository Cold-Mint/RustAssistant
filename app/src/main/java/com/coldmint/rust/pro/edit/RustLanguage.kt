package com.coldmint.rust.pro.edit


import android.content.Context
import android.os.Bundle
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.interfaces.EnglishMode
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.pro.edit.autoComplete.CodeAutoCompleteJob
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
import java.util.*

class RustLanguage() : Language, EnglishMode {

    private var isEnglishMode = false
    private val rustAnalyzeManager by lazy {
        RustIncrementalAnalyzeManager()
    }
    private val codeAutoCompleteJob: CodeAutoCompleteJob by lazy {
        CodeAutoCompleteJob()
    }

    private val newlineHandler: Array<NewlineHandler> by lazy {
        arrayOf<NewlineHandler>(object : NewlineHandler {
            override fun matchesRequirement(beforeText: String?, afterText: String?): Boolean {
                return true
            }

            override fun handleNewline(
                beforeText: String,
                afterText: String?,
                tabSize: Int
            ): NewlineHandleResult {
                var text = "\n"
                if (beforeText.startsWith("[")) {
                    if (beforeText.endsWith("_")) {
                        text = "name]"
                    } else if (!beforeText.endsWith("]")) {
                        text = "]"
                    }
                }
                val newlineHandleResult = NewlineHandleResult(text, 0)
                return newlineHandleResult
            }

        })
    }


    private val autoCompleteProvider: RustAutoCompleteProvider by lazy {
        val a = RustAutoCompleteProvider()
        a.addJob(codeAutoCompleteJob)
        a
    }
    private var codeDataBase: CodeDataBase? = null



    /**
     * 设置代码数据库
     * @param codeDataBean CodeDataBase
     */
    fun setCodeDataBase(codeDataBase: CodeDataBase) {
        this.codeDataBase = codeDataBase
        this.codeAutoCompleteJob.setCodeDataBase(codeDataBase)
    }

    /**
     * 设置编辑框（同步导航）
     * @param codeEditor
     */
    fun setCodeEditor(codeEditor: CodeEditor?) {
//        autoComplete.setCodeEditor(codeEditor)
    }

    fun setFileDataBase(fileDataBase: FileDataBase) {
        codeAutoCompleteJob.setFileDataBase(fileDataBase)
    }


    override fun getAnalyzeManager(): AnalyzeManager {
        return rustAnalyzeManager
    }

    override fun getInterruptionLevel(): Int {
        return Language.INTERRUPTION_LEVEL_STRONG
    }

    override fun requireAutoComplete(
        contentReference: ContentReference,
        charPosition: CharPosition,
        completionPublisher: CompletionPublisher,
        bundle: Bundle
    ) {
        autoCompleteProvider.requireAutoComplete(
            contentReference,
            charPosition,
            completionPublisher,
            bundle
        )
    }

    override fun getIndentAdvance(p0: ContentReference, p1: Int, p2: Int): Int {
        return 0
    }

    //用标签
    override fun useTab(): Boolean {
        return false
    }

    override fun getFormatter(): Formatter {
        return RustFormatter()
    }

    override fun getSymbolPairs(): SymbolPairMatch {
        return SymbolPairMatch()
    }

    override fun getNewlineHandlers(): Array<NewlineHandler>? {
        //行处理程序，按下回车时
        return newlineHandler
    }


    override fun destroy() {
    }

    override fun isEnglishMode(): Boolean {
        return isEnglishMode
    }

    override fun setEnglish(englishMode: Boolean) {
//        autoComplete.setEnglish(englishMode)
        isEnglishMode = englishMode
    }


}