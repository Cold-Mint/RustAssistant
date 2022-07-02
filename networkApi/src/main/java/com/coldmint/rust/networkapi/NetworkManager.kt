package com.coldmint.rust.networkapi

import com.coldmint.rust.core.tool.DebugHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 网络管理器
 */

@Deprecated("已废弃", level = DeprecationLevel.HIDDEN)
object NetworkManager {

    private var baseUrl: String? = null

    private var retrofit: Retrofit? = null


    const val debugKey = "网络管理器"

    /**
     * 设置BaseUrl
     * @param url String
     */
    fun setBaseUrl(url: String) {
        if (retrofit == null) {
            baseUrl = url
        } else {
            retrofit = createRetrofit(url)
        }
        DebugHelper.printLog(debugKey, "已设置Url:${url}", "设置Url")
    }

    /**
     * 创建接口实例
     * @param service Class<T> 类名
     * @return T
     */
    fun <T> create(service: Class<T>): T {
        if (retrofit == null) {
            retrofit = createRetrofit(baseUrl!!)
        }
        DebugHelper.printLog(debugKey, "已创建${service.name}接口实例。", "创建")
        return retrofit!!.create(service)
    }


    /**
     * 执行异步请求
     * @receiver Call<T>
     */
    fun <T> Call<T>.doEnqueue(networkCallback: NetworkCallback<T>) {
        val key = "异步请求"
        this.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody == null) {
                        DebugHelper.printLog(
                            debugKey,
                            "${call.request().url.toString()}，响应体为空。",
                            key,
                            true
                        )
                        networkCallback.onFail(null)
                    } else {
                        DebugHelper.printLog(
                            debugKey,
                            "${call.request().url.toString()}，响应成功。",
                            key
                        )
                        networkCallback.onSuccess(responseBody)
                    }
                } else {
                    DebugHelper.printLog(
                        debugKey,
                        "${call.request().url.toString()}，错误的响应码:${response.code()}",
                        key,
                        true
                    )
                    networkCallback.onFail(null)
                }

            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                DebugHelper.printLog(debugKey, "${call.request().url.toString()}，请求失败。", key, true)
                networkCallback.onFail(t)
            }

        })
    }

    /**
     * 创建Retrofit对象
     * @param url String baseUrl
     * @return Retrofit
     */
    private fun createRetrofit(url: String): Retrofit {
        return Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


}