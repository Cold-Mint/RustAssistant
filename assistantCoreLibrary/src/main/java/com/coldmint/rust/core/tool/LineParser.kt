package com.coldmint.rust.core.tool

import com.coldmint.rust.core.interfaces.LineParserEvent
import java.lang.StringBuilder

/**
 * 行解析器
 */
class LineParser(text: CharSequence? = null) {

    /**
     * 获取文本内容
     *
     * @return 文本内容
     */
    var text: String = text.toString()

    /**
     * 获取行号
     *
     * @return 总行号
     */
    var lineNum = 0
        private set
    var symbol = "\n"

    /**
     * 最后的开始位置
     */
    var lastStartIndex = 0

    /**
     * 是否循环到结尾
     *
     * @return 若代码跳出返回false
     */
    var isLoopEnd = false
        private set

    /**
     * 需要移除空格？
     */
    var needTrim = false

    /**
     * 分析符号?
     */
    var parserSymbol = false

    /**
     * 分析
     *
     * @param event 行分析事件
     */
    fun analyse(event: LineParserEvent) {
        lineNum = 0
        val result = StringBuilder()
        var startIndex = text.indexOf(symbol)
        lastStartIndex = 0
        var lastIndex = 0
        if (startIndex > -1) {
            while (startIndex > -1) {
                result.clear()
                lastStartIndex = lastIndex
                if (needTrim) {
                    result.append(text.substring(lastIndex, startIndex).trim { it <= ' ' })
                } else {
                    result.append(text.substring(lastIndex, startIndex))
                }
                if (!event.processingData(lineNum, result.toString(), false)) {
                    isLoopEnd = false
                    return
                }
                lastIndex = startIndex + symbol.length
                startIndex = text.indexOf(symbol, lastIndex)
                lineNum++
                if (parserSymbol && !event.processingData(lineNum, symbol, false)) {
                    isLoopEnd = false
                    return
                }
            }
            result.clear()
            if (needTrim) {
                result.append(text.substring(lastIndex).trim { it <= ' ' })
            } else {
                result.append(text.substring(lastIndex))
            }
            isLoopEnd = true
            event.processingData(lineNum, result.toString(), true)
        } else {
            event.processingData(lineNum, text, true)
        }
    }

    /**
     * 分析
     *
     * @param event 行分析事件
     */
    fun analyse(event: (lineNum: Int, lineData: String, isEnd: Boolean) -> Boolean) {
        analyse(object : LineParserEvent {
            override fun processingData(lineNum: Int, lineData: String, isEnd: Boolean): Boolean {
                return event.invoke(lineNum, lineData, isEnd)
            }
        })
    }


}