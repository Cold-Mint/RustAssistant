package com.coldmint.rust.pro.interfaces

interface BookmarkListener {
    /**
     * 找到数据
     *
     * @param path 路径
     * @param name 书签名
     */
    fun find(path: String, name: String)
}