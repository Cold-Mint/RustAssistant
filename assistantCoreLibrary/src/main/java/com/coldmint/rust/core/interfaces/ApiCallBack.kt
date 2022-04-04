package com.coldmint.rust.core.interfaces

/**
 * api请求回调
 */

interface ApiCallBack<T> {
    /**
     * 响应成功
     *
     * @param t 响应泛型
     */
    fun onResponse(t: T)

    /**
     * 相应失败
     *
     * @param e 异常
     */
    fun onFailure(e: Exception)
}