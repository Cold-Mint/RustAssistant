package com.coldmint.rust.core.interfaces

/**
 * 文件搜索器-搜索完成监听
 */
interface FindCompleteListener {
    fun whenFindComplete(result: Boolean)
}