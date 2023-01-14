package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.OrderDataBean
import com.coldmint.rust.core.dataBean.OrderListDataBean
import com.coldmint.rust.core.dataBean.PlanDataBean
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

/**
 * @author Cold Mint
 * @date 2021/12/22 20:12
 */
class ActivationApp private constructor() {

    companion object {
        val instance: ActivationApp by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ActivationApp()
        }
    }


    /**
     * 确认订单
     * @param account String 账号
     * @param appId String AppId
     * @param flag String 标志
     * @param apiCallBack ApiCallBack<ApiResponse> 回调接口
     */
    fun confirmOrder(
        account: String,
        appId: String,
        flag: String,
        payState: Boolean,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("appId", appId).add("flag", flag)
                .add("payState", payState.toString())

        val requestBody = requestBodyBuilder.build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/activation.php?action=confirmOrder")
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
                    LogCat.d("确认订单", data)
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
     * 获取订单列表
     * @param account String 账号
     * @param apiCallBack ApiCallBack<OrderListDataBean>
     */
    fun getOrderList(apiCallBack: ApiCallBack<OrderListDataBean>, account: String? = null) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder = FormBody.Builder()
        val requestBody: FormBody = if (account == null) {
            requestBodyBuilder.build()
        } else {
            requestBodyBuilder.add("account", account).build()
        }
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/activation.php?action=getOrderList")
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
                    val finalOrderListDataBean =
                        gson.fromJson(data, OrderListDataBean::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalOrderListDataBean)
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
     * 获取订单
     * @param account String
     * @param uuid String
     * @param apiCallBack ApiCallBack<OrderDataBean>
     */
    fun getOrderInfo(account: String, uuid: String, apiCallBack: ApiCallBack<OrderDataBean>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account).add("uuid", uuid).build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/activation.php?action=getOrderInfo")
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
                    val finalOrderDataBean =
                        gson.fromJson(data, OrderDataBean::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalOrderDataBean)
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
     * 获取计划列表
     * @param account String 账号
     * @param apiCallBack ApiCallBack<PlanDataBean>
     */
    fun getPlanList(account: String, apiCallBack: ApiCallBack<PlanDataBean>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody = FormBody.Builder().add("account", account).build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/activation.php?action=getPlanList")
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
                    val finalPlanDataBean =
                        gson.fromJson(data, PlanDataBean::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalPlanDataBean)
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
     * 创建订单
     * @param account String
     * @param planId String
     * @param couponsId Int?
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun createOrder(
        account: String,
        planId: String,
        apiCallBack: ApiCallBack<ApiResponse>,
        couponsId: Int? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("planId", planId)
        if (couponsId != null) {
            requestBodyBuilder.add("couponsId", couponsId.toString())
        }
        val requestBody = requestBodyBuilder.build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/activation.php?action=createOrder")
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