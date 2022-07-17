package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.DynamicItemDataBean
import com.coldmint.rust.core.dataBean.PlanDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

/**
 * 动态
 */
class Dynamic private constructor() {

    companion object {
        val instance: Dynamic by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Dynamic()
        }
    }

    /**
     * 删除动态
     * @param account String
     * @param appId String
     * @param dynamicID Int
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun deleteDynamic(
        account: String,
        appId: String,
        dynamicID: Int,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("id", dynamicID.toString())
                .add("appId", appId)
        val requestBody = requestBodyBuilder.build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/dynamic.php?action=delete")
            .post(requestBody).build()
        val call = okHttpClient.newCall(request)
        val handler = Handler(Looper.getMainLooper())
        val gson = Gson()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                handler.post { apiCallBack.onFailure(e) }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = response.body!!.string()
                    val finalApiResponse =
                        gson.fromJson(data, ApiResponse::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalApiResponse)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.post {
                        apiCallBack.onFailure(e)
                    }
                }
            }

        })
    }


    /**
     * 获取所有偶像的动态
     * @param apiCallBack ApiCallBack<DynamicItemDataBean>
     * @param account String?
     * @param limit String?
     */
    fun getFollowAllDynamic(
        account: String,
        apiCallBack: ApiCallBack<DynamicItemDataBean>,
        limit: String? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder = FormBody.Builder().add("account", account)
        if (limit != null) {
            requestBodyBuilder.add("limit", limit)
        }
        val requestBody = requestBodyBuilder.build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/dynamic.php?action=getAllDynamic")
            .post(requestBody).build()
        val call = okHttpClient.newCall(request)
        val handler = Handler(Looper.getMainLooper())
        val gson = Gson()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                handler.post { apiCallBack.onFailure(e) }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = response.body!!.string()
                    val finalDynamicItemDataBean =
                        gson.fromJson(data, DynamicItemDataBean::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalDynamicItemDataBean)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.post {
                        apiCallBack.onFailure(e)
                    }
                }
            }

        })
    }

    /**
     * 获取动态列表
     * @param account String? 账号
     * @param limit String? 限制数量
     */
    fun getList(
        apiCallBack: ApiCallBack<DynamicItemDataBean>,
        account: String? = null,
        limit: String? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder = FormBody.Builder()
        if (account != null) {
            requestBodyBuilder.add("account", account)
        }
        if (limit != null) {
            requestBodyBuilder.add("limit", limit)
        }
        val requestBody = requestBodyBuilder.build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/dynamic.php?action=list")
            .post(requestBody).build()
        val call = okHttpClient.newCall(request)
        val handler = Handler(Looper.getMainLooper())
        val gson = Gson()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                handler.post { apiCallBack.onFailure(e) }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = response.body!!.string()
                    val finalDynamicItemDataBean =
                        gson.fromJson(data, DynamicItemDataBean::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalDynamicItemDataBean)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.post {
                        apiCallBack.onFailure(e)
                    }
                }
            }

        })
    }

    /**
     * 发布动态
     * @param token String 令牌
     * @param context String 内容
     */
    fun send(
        token: String,
        context: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("token", token).add("context", context)
        val requestBody = requestBodyBuilder.build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/dynamic.php?action=send")
            .post(requestBody).build()
        val call = okHttpClient.newCall(request)
        val handler = Handler(Looper.getMainLooper())
        val gson = Gson()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                handler.post { apiCallBack.onFailure(e) }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = response.body!!.string()
                    val finalApiResponse =
                        gson.fromJson(data, ApiResponse::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalApiResponse)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.post {
                        apiCallBack.onFailure(e)
                    }
                }
            }

        })
    }


}