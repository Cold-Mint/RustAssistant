package com.coldmint.rust.core.dataBean


/**
 * 模板类
 * @property action List<Action> 活动列表
 * @property attachFile List<String> 附加文件路径
 * @property `data` String 数据
 * @property icon String 图标
 * @property language String 语言
 * @property name String 名称
 * @constructor
 */
data class TemplateDataBean(
    var action: List<Action>? = null,
    val attachFile: List<String>? = null,
    val `data`: String,
    var icon: String? = null,
    val language: String,
    val name: String
) {
    /**
     * 活动类
     * @property key String 键
     * @property name String 活动显示名称
     * @property section String 节
     * @property tag String 标签
     * @property type String 模板解析器名称
     * @constructor
     */
    data class Action(
        val key: String,
        val name: String,
        val section: String,
        val tag: String,
        val type: String
    )
}