package com.coldmint.rust.core.interfaces

/**
 * @author Cold Mint
 * @date 2021/12/8 15:52
 */
interface ProgressListener {
    fun onProgress(totalLength: Long, currentLength: Long)
}