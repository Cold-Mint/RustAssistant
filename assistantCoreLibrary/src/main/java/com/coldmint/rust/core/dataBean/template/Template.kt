package com.coldmint.rust.core.dataBean.template

import org.json.JSONObject

/**
 * 模板接口
 */
interface Template {

    /**
     * 实现获取json数据方法
     * @return String
     */
    fun getJson(): JSONObject


    /**
     * 获取名称
     * @return String
     */
    fun getName(language: String): String


    /**
     * 获取图标
     * @return String
     */
    fun getIcon(): Any?

    /**
     * 是否为本地模板
     * @return Boolean
     */
    fun isLocal():Boolean


    /**
     * 获取此模板的链接，若[Template.isLocal]为false应返回模板id，若为真返回文件路径
     * @return String
     */
    fun getLink():String

}