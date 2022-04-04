package com.coldmint.rust.pro.databean

import android.text.SpannableString

class OptimizeItem<T>
/**
 * 构造优化项目
 * @param name  名称
 * @param group 优化组
 * @param object
 */(
    /**
     * 获取项目名称
     *
     * @return
     */
    val name: String,
    /**
     * 获取优化组
     *
     * @return 优化组
     */
    val group: OptimizeGroup,
    /**
     * 获取操作对象
     *
     * @return
     */
    val `object`: T
) {

    /**
     * 是否启用
     *
     * @return 启用状态
     */
    /**
     * 设置启用状态
     *
     * @param enabled 启用状态
     */
    var isEnabled = true
    /**
     * 获取项目描述
     *
     * @return 项目描述,没有设置返回null
     */
    /**
     * 设置项目描述
     *
     * @param description
     */
    var description: SpannableString? = null

}