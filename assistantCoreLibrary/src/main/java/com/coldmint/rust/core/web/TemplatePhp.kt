package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.SubscriptionData
import com.coldmint.rust.core.dataBean.WebTemplatePackageDetailsData
import com.coldmint.rust.core.dataBean.WebTemplatePackageListData
import com.coldmint.rust.core.dataBean.template.WebTemplateData
import com.coldmint.rust.core.debug.LogCat
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
                    LogCat.d("网络模板包数据", data)
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
     *获取模板详情
     * @param apiCallBack ApiCallBack<CouponListDataBean>
     */
    fun getTemplate(
        id: String,
        apiCallBack: ApiCallBack<WebTemplateData>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("id", id)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=getTemplate")
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
                    LogCat.d("获取网络模板详情", data)
                    val finalWebTemplatePackageListData =
                        gson.fromJson(data, WebTemplateData::class.java)
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
     *获取模板列表详情
     * @param apiCallBack ApiCallBack<getTemplateList>
     */
    fun getTemplateList(
        packageId: String,
        apiCallBack: ApiCallBack<WebTemplatePackageDetailsData>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("packageId", packageId)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=getTemplateList")
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
                    LogCat.d("获取网络模板详情", data)
                    val finalWebTemplatePackageListData =
                        gson.fromJson(data, WebTemplatePackageDetailsData::class.java)
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
     *获用户订阅的模板列表
     * @param apiCallBack ApiCallBack<SubscriptionData>
     */
    fun getSubscriptionDataList(
        token: String,
        apiCallBack: ApiCallBack<SubscriptionData>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("token", token)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=getSubscriptionData")
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
                    LogCat.d("获取网络订阅", data)
                    val finalSubscriptionData =
                        gson.fromJson(data, SubscriptionData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalSubscriptionData)
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
     *订阅模板
     * @param apiCallBack ApiCallBack<CouponListDataBean>
     */
    fun subscription(
        token: String,
        packageId: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("token", token).add("packageId", packageId)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=subscription")
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
                    LogCat.d("订阅模板", data)
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
     *退订模板
     * @param apiCallBack ApiCallBack<CouponListDataBean>
     */
    fun deleteSubscription(
        token: String,
        packageId: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("token", token).add("packageId", packageId)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=deleteSubscription")
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
                    LogCat.d("退订模板", data)
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
     *获取用户创建的模板列表
     * @param apiCallBack ApiCallBack<CouponListDataBean>
     */
    fun getTemplatePackageList(
        token: String,
        apiCallBack: ApiCallBack<WebTemplatePackageListData>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("token", token)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=getTemplatePackageList")
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
                    LogCat.d("获取用户创建的模板包列表", data)
                    val finalApiResponse =
                        gson.fromJson(data, WebTemplatePackageListData::class.java)
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
     * 添加模板
     * @param token String
     * @param title String
     * @param content String
     * @param packageId String
     * @param apiCallBack ApiCallBack<WebTemplatePackageListData>
     */
    fun addTemplate(
        id: String,
        token: String, title: String, content: String, packageId: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("token", token).add("title", title).add("content", content)
                .add("packageId", packageId).add("id", id)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/template.php?action=addTemplate")
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
                    LogCat.d("添加模板", data)
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
                    LogCat.d("创建模板包", data)
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