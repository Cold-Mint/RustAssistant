package com.coldmint.rust.pro.databean

data class OptimizeGroup
/**
 * 构造优化组
 *
 * @param groupName 组名
 */(
    /**
     * 获取组名
     *
     * @return 组名
     */
    val groupName: String
) {
    /**
     * 是否启用
     *
     * @return 启用状态
     */
    /**
     * 设置启用状态
     *
     * @param enabled
     */
    var isEnabled = true

}