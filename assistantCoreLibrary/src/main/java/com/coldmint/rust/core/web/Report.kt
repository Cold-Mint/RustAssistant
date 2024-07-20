package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.dataBean.report.ReportItemDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

/**
 * @author Cold Mint
 * @date 2022/1/6 19:45
 */
class Report private constructor() {

    companion object {
        val instance: Report by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Report()
        }
    }

    /**
     * 处理举报
     * @param account String
     * @param reportId String
     * @param state Boolean
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun dispose(
        account: String,
        reportId: String,
        state: Boolean,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("id", reportId)
                .add("state", state.toString())

        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/report.php?action=dispose")
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
                    val body = response.body
                    if (body == null) {
                        handler.post {
                            apiCallBack.onFailure(NullPointerException())
                        }
                    } else {
                        val data = body.string()
                        val finalApiResponse =
                            gson.fromJson(data, ApiResponse::class.java)
                        handler.post {
                            apiCallBack.onResponse(finalApiResponse)
                        }
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
     * 获取举报列表
     * @param apiCallBack ApiCallBack<ReportItemDataBean> 回调接口
     */
    fun list(apiCallBack: ApiCallBack<ReportItemDataBean>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder()
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/report.php?action=reportRecordList")
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
                    val body = response.body
                    if (body == null) {
                        handler.post {
                            apiCallBack.onFailure(NullPointerException())
                        }
                    } else {
                        val data = body.string()
                        val finalReportItemDataBean =
                            gson.fromJson(data, ReportItemDataBean::class.java)
                        handler.post {
                            apiCallBack.onResponse(finalReportItemDataBean)
                        }
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
     * 举报
     * @param account String
     * @param type String
     * @param target String
     * @param why String
     * @param describe String
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun send(
        account: String,
        type: String,
        target: String,
        why: String,
        describe: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("type", type).add("target", target)
                .add("describe", describe)
                .add("why", why)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/report.php?action=send")
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
                    val body = response.body
                    if (body == null) {
                        handler.post {
                            apiCallBack.onFailure(NullPointerException())
                        }
                    } else {
                        val data = body.string()
                        val finalApiResponse =
                            gson.fromJson(data, ApiResponse::class.java)
                        handler.post {
                            apiCallBack.onResponse(finalApiResponse)
                        }
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