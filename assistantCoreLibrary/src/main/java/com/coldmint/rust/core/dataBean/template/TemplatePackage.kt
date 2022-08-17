package com.coldmint.rust.core.dataBean.template

/**
 * 模板包接口
 */
interface TemplatePackage {

    /**
     * 实现获取名称方法
     * @return String
     */
    fun getName(): String


    /**
     * 获取描述
     * @return String
     */
    fun getDescription(): String

    /**
     * 实现删除方法，本地模板删除，远程模板退订
     * @return Boolean
     */
    fun delete(token: String, func: (Boolean) -> Unit)


    /**
     * 是否为本地模板包
     * @return Boolean
     */
    fun isLocal():Boolean


}