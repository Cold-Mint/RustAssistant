package com.coldmint.rust.pro.edit

import com.coldmint.rust.core.CodeTranslate
import com.coldmint.rust.core.tool.DebugHelper
import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.TextRange

class RustFormatter : Formatter {
    private val key = "Rust代码格式化"
    private var formatResultReceiver: Formatter.FormatResultReceiver? = null
    private var running = false
    override fun format(p0: Content, p1: TextRange) {
        running = true
        //io.github.rosemoe. ora.lang. Format . formatter将给定的内容从开始位置格式化到结束位置
        //直接格式化内容，并调用Formatter。FormatResultReceiver，用于在格式化完成时从编辑器接收格式化的内容*
        DebugHelper.printLog(key, "调用了格式化${p0} 文本范围${p1}")

        formatResultReceiver?.onFormatSucceed(CodeTranslate.format(p0.toString()), null)
        running = false
    }

    override fun formatRegion(p0: Content, p1: TextRange, p2: TextRange) {
//        io.github.rosemoe. ora.lang. Format . formatter将给定的内容从开始位置格式化到结束位置
//        直接格式化内容，并调用Formatter。FormatResultReceiver用于在格式化完成时从编辑器接收格式化的内容
        DebugHelper.printLog(key, "调用了格式化区域${p0} 文本范围${p1}")

    }

    override fun setReceiver(p0: Formatter.FormatResultReceiver?) {
//设置结果接收器
        DebugHelper.printLog(key, "设置了结果接收器 为空?${p0 == null}")
        formatResultReceiver = p0
    }

    override fun isRunning(): Boolean {
        return running
    }

    override fun destroy() {

    }
}