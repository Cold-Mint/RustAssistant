package com.coldmint.rust.core.interfaces

/**
 * 文件搜索器接口（所有的文件搜索器都应该实现此接口）
 */
interface FileFinderInterface {

    /**
     * 开始方法
     */
    fun onStart(): Boolean

    /**
     * 设置查找监听器
     * @param finderListener 设置监听器
     */
    fun setFinderListener(finderListener: FileFinderListener?)

}