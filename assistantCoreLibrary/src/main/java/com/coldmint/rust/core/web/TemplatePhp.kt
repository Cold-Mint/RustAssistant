package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.webTemplate.WebTemplatePackageListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class TemplatePhp {


    companion object {
        val instance: TemplatePhp by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TemplatePhp()
        }
    }

    private constructor()

    /**
     * 发送错误报告
     * @param website 网址(如果程序已崩溃，需要重新读取设置的网址)
     * @param message String 消息
     * @param versionName 版本名
     * @param versionNumber 版本号
     * @param apiCallBack ApiCallBack<CouponListDataBean>
     */
    fun send(
        message: String,
        versionName: String,
        versionNumber: Int,
        apiCallBack: ApiCallBack<ApiResponse>, website: String = ServerConfiguration.website
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("message", message).add("versionName", versionName)
                .add("versionNumber", versionNumber.toString())
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(website + "php/error.php?action=send")
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
                    Log.d("错误反馈数据", data)
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
     *获取公开的模板包列表
     * @param apiCallBack ApiCallBack<CouponListDataBean>
     */
    fun getPublicTemplatePackageList(
        token: String,
        apiCallBack: ApiCallBack<WebTemplatePackageListData>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("token", token)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=getPublicTemplatePackageList")
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
                    Log.d("网络模板包数据", data)
                    val finalWebTemplatePackageListData =
                        gson.fromJson(data, WebTemplatePackageListData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebTemplatePackageListData)
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
     * 创建模板包
     * @param id String 模板包id
     * @param token String 令牌
     * @param name String 模板包名称
     * @param describe String 模板包描述
     * @param versionName String 版本名
     * @param appVersionName String app版本名
     * @param appVersionNumber String 版本号
     * @param publicState Boolean 状态
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun createTemplatePackage(
        id: String,
        token: String,
        name: String,
        describe: String,
        versionName: String,
        appVersionName: String,
        appVersionNumber: Int,
        publicState: Boolean,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("id", id).add("token", token).add("name", name)
                .add("describe", describe).add("versionName", versionName)
                .add("appVersionName", appVersionName)
                .add("appVersionNumber", appVersionNumber.toString())
                .add("publicState", publicState.toString())
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=createTemplatePackage")
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
                    Log.d("创建模板包", data)
                    val finalWebTemplatePackageListData =
                        gson.fromJson(data, ApiResponse::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebTemplatePackageListData)
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