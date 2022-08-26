package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.LoginRequestData
import com.coldmint.rust.core.dataBean.RegisterRequestData
import com.coldmint.rust.core.dataBean.user.*

import com.google.gson.Gson
import com.coldmint.rust.core.interfaces.ApiCallBack
import kotlin.Throws
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.lang.NullPointerException

/**
 * 用户类
 */
object User {

    /**
     * 是否为邮箱
     * @param string String
     * @return Boolean
     */
    fun isEmail(string: String): Boolean {
        return string.matches(Regex("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$"))
    }

    /**
     * 更改Appid
     * @param account String
     * @param key String
     * @param appId String
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun changeAppId(
        account: String,
        key: String,
        appId: String,
        apiCallBack: ApiCallBack<ApiResponse>,
        isEmail: Boolean = isEmail(account)
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("key", key).add("account", account)
                .add("appID", appId).add("isEmail", isEmail.toString())
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=changeAppId")
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
     * 请求修改密码（发送邮件）
     * @param account String
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun requestChangePassword(
        account: String,
        apiCallBack: ApiCallBack<ApiResponse>,
        isEmail: Boolean = isEmail(account)
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account).add("isEmail", isEmail.toString())
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=requestChangePassword")
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
     * 修改密码
     * @param account String
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun changePassword(
        account: String,
        code: Int,
        newPassword: String,
        apiCallBack: ApiCallBack<ApiResponse>,
        isEmail: Boolean = isEmail(account)
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account).add("isEmail", isEmail.toString())
                .add("code", code.toString()).add("newPassword", newPassword)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=changePassword")
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
     * 发送设备验证请求
     * @param account String
     * @param passWord String
     * @param appId String
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun verification(
        account: String,
        passWord: String,
        appId: String,
        apiCallBack: ApiCallBack<ApiResponse>,
        isEmail: Boolean = isEmail(account)
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("passWord", passWord).add("account", account)
                .add("appID", appId).add("isEmail", isEmail.toString())
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=verification")
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
     * 获取用户头像
     * @param account String
     * @param apiCallBack ApiCallBack<IconData>
     */
    @Deprecated("不建议使用")
    fun getIcon(
        account: String,
        apiCallBack: ApiCallBack<IconData>,
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=getUserIcon")
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
                    val finalUserIcon =
                        gson.fromJson(data, IconData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalUserIcon)
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
     * 更新用户资料
     * @param token String 令牌
     * @param userName String 用户名
     * @param introduce String 介绍
     * @param gender Int 性别(1为男生，-1为女生)
     * @param apiCallBack ApiCallBack<ApiResponse> 事件处理接口
     * @param iconLink String? 图片链接（内部自行转换文件）
     * @param coverLink String? 封面链接（内部自行转换文件）
     */
    fun updateSpaceInfo(
        token: String,
        userName: String,
        introduce: String,
        gender: Int, apiCallBack: ApiCallBack<ApiResponse>,
        iconLink: String? = null,
        coverLink: String? = null,
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBuilder =
            MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("token", token).addFormDataPart("userName", userName)
                .addFormDataPart("introduce", introduce)
                .addFormDataPart("gender", gender.toString())
        if (iconLink != null) {
            if (ServerConfiguration.canConvertedToFile(iconLink)) {
                val iconFile = File(iconLink)
                if (iconFile.exists()) {
                    requestBuilder.addFormDataPart(
                        "icon",
                        iconFile.name,
                        iconFile.asRequestBody()
                    )
                }
            } else {
                requestBuilder.addFormDataPart("icon", iconLink)
            }
        }

        if (coverLink != null) {
            if (ServerConfiguration.canConvertedToFile(coverLink)) {
                val coverFile = File(coverLink)
                if (coverFile.exists()) {
                    requestBuilder.addFormDataPart(
                        "cover",
                        coverFile.name,
                        coverFile.asRequestBody()
                    )
                }
            } else {
                requestBuilder.addFormDataPart("cover", coverLink)
            }
        }
        val requestBody = requestBuilder.build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/user.php?action=updateSpaceInfo")
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
                    if (body != null) {
                        val data = body.string()
                        val finalApiResponse = gson.fromJson(data, ApiResponse::class.java)
                        handler.post {
                            apiCallBack.onResponse(finalApiResponse)
                        }
                    } else {
                        handler.post {
                            apiCallBack.onFailure(NullPointerException("body为空"))
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
     * 激活账号
     */
    fun activateAccount(
        account: String,
        key: String,
        apiCallBack: ApiCallBack<ApiResponse>,
        isEmail: Boolean = isEmail(account)
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("key", key).add("account", account)
                .add("isEmail", isEmail.toString())
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=enableAccount")
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
     * 获取空间信息
     * @param account String 用户
     * @param apiCallBack ApiCallBack<SpaceInfoData> 返回空间信息
     */
    fun getSpaceInfo(account: String, apiCallBack: ApiCallBack<SpaceInfoData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=getSpaceInfo")
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
                    val finalSpaceInfoData =
                        gson.fromJson(data, SpaceInfoData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalSpaceInfoData)
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
     * 获取用户激活信息
     * @param token String
     * @param apiCallBack ApiCallBack<ActivationInfo>
     */
    fun getUserActivationInfo(token: String, apiCallBack: ApiCallBack<ActivationInfo>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("token", token)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=getUserActivationInfo")
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
                    val finalActivationInfo =
                        gson.fromJson(data, ActivationInfo::class.java)
                    if (finalActivationInfo == null) {
                        handler.post {
                            apiCallBack.onFailure(Exception("激活信息错误"))
                        }
                    } else {
                        handler.post {
                            apiCallBack.onResponse(finalActivationInfo)
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
     * 获取用户社交信息
     * @param apiCallBack ApiCallBack<WebModData>
     */
    fun getSocialInfo(account: String, apiCallBack: ApiCallBack<SocialInfoData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=getSocialInfo")
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
                    val finalSocialInfoData =
                        gson.fromJson(data, SocialInfoData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalSocialInfoData)
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
     * 获取用户信息
     * @param apiCallBack ApiCallBack<WebModData>
     */
    fun getInfo(account: String, apiCallBack: ApiCallBack<UserData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account)
                .build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/user.php?action=getInfo")
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
                    val finalUserData =
                        gson.fromJson(data, UserData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalUserData)
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


    private var delayed = 300

    /**
     * 登录
     */
    fun login(loginRequestData: LoginRequestData, apiCallBack: ApiCallBack<UserData>) {
        val requestBody: FormBody =
            FormBody.Builder().add("account", loginRequestData.account)
                .add("passWord", loginRequestData.passWord).add("appID", loginRequestData.appId)
                .add("isEmail", loginRequestData.isEmail.toString())
                .build()
        val request: Request =
            Request.Builder().url(ServerConfiguration.website + "php/user.php?action=login")
                .post(requestBody).build()
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val call = okHttpClient.newCall(request)
        val gson = Gson()
        val handler = Handler(Looper.getMainLooper())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                handler.postDelayed({ apiCallBack.onFailure(e) }, delayed.toLong())
            }

            @Throws(Exception::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = response.body!!.string()
                    val finalUserData = gson.fromJson(data, UserData::class.java)
                    handler.postDelayed({
                        apiCallBack.onResponse(finalUserData)
                    }, delayed.toLong())
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.postDelayed({
                        apiCallBack.onFailure(e)
                    }, delayed.toLong())
                }
            }
        })
    }


    /**
     * 注册
     * @param userName String
     * @param email String
     * @param apiCallBack ApiCallBack<ApiResponse>
     * @throws Exception
     */
    fun register(registerRequestData: RegisterRequestData, apiCallBack: ApiCallBack<ApiResponse>) {
        val requestBody: FormBody =
            FormBody.Builder().add("account", registerRequestData.account)
                .add("passWord", registerRequestData.passWord)
                .add("email", registerRequestData.email)
                .add("userName", registerRequestData.userName)
                .add("appID", registerRequestData.appID).build()
        val request: Request =
            Request.Builder().url(ServerConfiguration.website + "php/user.php?action=register")
                .post(requestBody).build()
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val call = okHttpClient.newCall(request)
        val handler = Handler(Looper.getMainLooper())
        val gson = Gson()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.postDelayed({ apiCallBack.onFailure(e) }, delayed.toLong())
            }

            @Throws(Exception::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = response.body!!.string()
                    val apiResponse = gson.fromJson(data, ApiResponse::class.java)
                    handler.postDelayed({
                        apiCallBack.onResponse(apiResponse)
                    }, delayed.toLong())
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.postDelayed({
                        apiCallBack.onFailure(e)
                    }, delayed.toLong())
                }
            }
        })
    }
}