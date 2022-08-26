package com.coldmint.rust.pro.edit

import com.coldmint.rust.core.CodeTranslate
import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager
import io.github.rosemoe.sora.lang.analysis.IncrementalAnalyzeManager
import io.github.rosemoe.sora.lang.styling.CodeBlock
import io.github.rosemoe.sora.lang.styling.Span
import io.github.rosemoe.sora.lang.styling.TextStyle
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * Rust增量分析器
 */
class RustIncrementalAnalyzeManager :
    AsyncIncrementalAnalyzeManager<Int, RustCodeToken>() {
    override fun getInitialState(): Int {
        //获取初始状态
        return 0
    }

    override fun stateEquals(state: Int?, another: Int?): Boolean {
        return true
    }

    override fun tokenizeLine(
        line: CharSequence?,
        state: Int?,
        lineIndex: Int
    ): IncrementalAnalyzeManager.LineTokenizeResult<Int, RustCodeToken> {
        if (line.isNullOrBlank()) {
            return IncrementalAnalyzeManager.LineTokenizeResult(0, null)
        }
        val tokens = ArrayList<RustCodeToken>()
        if (line.startsWith("#")) {
            //是注释
            tokens.add(RustCodeToken())
            return IncrementalAnalyzeManager.LineTokenizeResult(0, tokens)
        }

        val index = line.lastIndexOf(':')
        if (index > 0) {
            //是代码
            tokens.add(RustCodeToken(0, CodeTranslate.CodeBlockType.Key))
            tokens.add(RustCodeToken(index, CodeTranslate.CodeBlockType.Symbol))
            tokens.add(RustCodeToken(index + 1, CodeTranslate.CodeBlockType.Value))
            return IncrementalAnalyzeManager.LineTokenizeResult(0, tokens)
        }


        if (line.startsWith('[') && line.endsWith(']')) {
            //是节
            tokens.add(RustCodeToken(0, CodeTranslate.CodeBlockType.Section))
            return IncrementalAnalyzeManager.LineTokenizeResult(0, tokens)
        }

        tokens.add(RustCodeToken())
        return IncrementalAnalyzeManager.LineTokenizeResult(0, tokens)
    }

    override fun generateSpansForLine(tokens: IncrementalAnalyzeManager.LineTokenizeResult<Int, RustCodeToken>?): MutableList<Span> {
        val spans = ArrayList<Span>()
        val tokenList = tokens?.tokens
        if (tokenList == null || tokenList.isEmpty()) {
            spans.add(
                Span.obtain(
                    0,
                    TextStyle.makeStyle(EditorColorScheme.COMMENT)
                )
            )
            return spans
        }
        tokenList.forEach {
            when (it.codeBlockType) {
                CodeTranslate.CodeBlockType.Section -> {
                    spans.add(
                        Span.obtain(
                            it.offset,
                            TextStyle.makeStyle(
                                EditorColorScheme.FUNCTION_NAME,
                                0,
                                true,
                                false,
                                false
                            )
                        )
                    )
                }
                CodeTranslate.CodeBlockType.Value -> {
                    spans.add(
                        Span.obtain(
                            it.offset,
                            TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL)
                        )
                    )
                }
                CodeTranslate.CodeBlockType.Symbol -> {
                    spans.add(
                        Span.obtain(
                            it.offset,
                            TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL)
                        )
                    )
                }
                CodeTranslate.CodeBlockType.Key -> {
                    spans.add(
                        Span.obtain(
                            it.offset,
                            TextStyle.makeStyle(EditorColorScheme.KEYWORD)
                        )
                    )
                }
                else -> {
                    spans.add(
                        Span.obtain(
                            it.offset,
                            TextStyle.makeStyle(EditorColorScheme.COMMENT)
                        )
                    )
                }
            }
        }
        return spans
    }

    override fun computeBlocks(
        text: Content?,
        delegate: CodeBlockAnalyzeDelegate?
    ): MutableList<CodeBlock> {
        return ArrayList<CodeBlock>()
    }

}