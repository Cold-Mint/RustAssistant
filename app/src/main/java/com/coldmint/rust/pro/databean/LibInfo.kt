package com.coldmint.rust.pro.databean

data class LibInfo
/**
 * 构造库信息类
 *
 * @param title       标题
 * @param description 描述
 * @param link        链接
 * @param agreement   开源协议
 */(
    /**
     * 获取标题
     *
     * @return
     */
    val title: String,
    /**
     * 获取描述
     *
     * @return
     */
    val description: String,
    /**
     * 获取链接
     *
     * @return
     */
    val link: String,
    /**
     * 获取开源协议
     *
     * @return
     */
    val agreement: String
) {

    /**
     * 获取提示
     * @return
     */
    /**
     * 设置提示
     * @param tip
     */
    var tip: String? = null

}