package com.coldmint.rust.core.interfaces

interface LineParserEvent {
    /**
     * 处理行数据
     *
     * @param lineNum  行号
     * @param lineData 行数据
     * @param isEnd    是否为结尾
     * @return 若返回假，则停止循环
     */
    fun processingData(lineNum: Int, lineData: String, isEnd: Boolean): Boolean

}