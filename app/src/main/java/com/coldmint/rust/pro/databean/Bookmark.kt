package com.coldmint.rust.pro.databean

/**
 * 书签的封装类
 */
data class Bookmark
/**
 * 构造书签
 *
 * @param path 文件路径
 * @param name 书签名
 */(
    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    val path: String,
    /**
     * 获取书签名
     *
     * @return 书签名
     */
    val name: String
)