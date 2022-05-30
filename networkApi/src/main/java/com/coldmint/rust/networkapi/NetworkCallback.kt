package com.coldmint.rust.networkapi

/**
 * 网络回调
 * @param T
 */
interface NetworkCallback<T> {

    fun onSuccess(response: T)


    fun onFail(throwable: Throwable? = null)

}