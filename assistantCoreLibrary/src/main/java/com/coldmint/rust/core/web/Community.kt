package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.follow.FollowUserListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

object Community {

    /**
     * 获取关注状态
     * @param account String 账户
     * @param targetAccount String 目标账户
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun getFollowState(
        account: String,
        targetAccount: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account).add("targetAccount", targetAccount)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/community.php?action=getFollowState")
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
     * 加载用户关注列表
     * @param account String 用户
     * @param isFollowMode Boolean 是否加载偶像?
     * @param limit Int 限制返回数量(默认20),输入负数不受限制
     * @param apiCallBack 返回事件监听
     */
    fun getUserList(
        account: String,
        isFollowMode: Boolean = true,
        limit: Int = 20,
        apiCallBack: ApiCallBack<FollowUserListData>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("isFollowMode", isFollowMode.toString())
        if (limit > 0) {
            requestBodyBuilder.add("limit", limit.toString())
        }
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/community.php?action=getList")
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
                    val finalFollowUserListData =
                        gson.fromJson(data, FollowUserListData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalFollowUserListData)
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
     * 获取关注状态
     * @param account String 账户
     * @param targetAccount String 目标账户
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun follow(
        account: String,
        targetAccount: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account).add("targetAccount", targetAccount)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/community.php?action=follow")
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
     * 移除粉丝
     * @param account String 账号
     * @param targetAccount String 目标账号
     * @param apiCallBack ApiCallBack<ApiResponse> 响应
     * @param needBan Boolean 是否加入黑名单
     */
    fun removeFans(
        account: String,
        targetAccount: String,
        apiCallBack: ApiCallBack<ApiResponse>,
        needBan: Boolean = false
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("targetAccount", targetAccount)
        if (needBan) {
            requestBodyBuilder.add("needBan", needBan.toString())
        }
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/community.php?action=removeFans")
                .post(requestBodyBuilder.build()).build()

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
     * 获取关注状态
     * @param account String 账户
     * @param targetAccount String 目标账户
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun deFollow(
        account: String,
        targetAccount: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account).add("targetAccount", targetAccount)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/community.php?action=deFollow")
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