package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.CouponListDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

/**
 * 错误报告
 */
class ErrorReport {

    companion object {
        val instance: ErrorReport by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ErrorReport()
        }
    }

    /**
     * 发送错误报告
     * @param message String 消息
     * @param versionName 版本名
     * @param versionNumber 版本号
     * @param apiCallBack ApiCallBack<CouponListDataBean>
     */
    fun send(
        message: String,
        versionName: String,
        versionNumber: Int,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("message", message).add("versionName", versionName)
                .add("versionNumber", versionNumber.toString())
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/error.php?action=send")
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
                    Log.d("错误反馈数据",data)
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