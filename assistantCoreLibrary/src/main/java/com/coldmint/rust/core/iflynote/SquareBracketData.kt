package com.coldmint.rust.core.iflynote

/**
 * 方括号数据解析生成器
 * 适配旧版本更新日志
 */
@Deprecated("已停用，因为可能后台被ban")
class SquareBracketData(private val text: String) {
    /**
     * 获取值
     *
     * @param key 值
     * @return 成功返回值，失败返回null
     */
    fun getValue(key: String): String? {
        val head = "$key["
        val tail = "]"
        val start = text.indexOf(head)
        val end = text.indexOf(tail, start + head.length)
        return if (start > -1 && end > -1) {
            text.substring(start + head.length, end)
        } else {
            null
        }
    }

    /**
     * 获取值
     *
     * @param key 值
     * @param defaultValue 默认值
     * @return 成功返回值，失败返回默认值
     */
    fun getValue(key: String, defaultValue: String): String {
        val value = getValue(key)
        return value ?: defaultValue
    }
}