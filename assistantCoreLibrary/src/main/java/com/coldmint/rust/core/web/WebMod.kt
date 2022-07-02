package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.*
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.ProgressListener
import com.coldmint.rust.core.tool.ProgressMultipartBody
import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class WebMod private constructor() {

    enum class SortMode(private val value: String) {

        Latest_Time("latestTime"), Download_Number("downloadNumber");

        /**
         * 获取枚举代表的值
         * @return String
         */
        fun getValue(): String {
            return value
        }

    }

    companion object {
        val instance: WebMod by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            WebMod()
        }

        const val maxDescribeLength = 15
    }


    /**
     * 加载随机推荐
     * @param number Int 推荐数量
     * @param apiCallBack ApiCallBack<WebModListData>
     */
    fun randomRecommended(number: Int, apiCallBack: ApiCallBack<WebModListData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("number", number.toString())
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=random")
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
                        val finalWebModListData =
                            gson.fromJson(data, WebModListData::class.java)
                        handler.post {
                            apiCallBack.onResponse(finalWebModListData)
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
     * 独家推荐
     * 获取关注者新作
     * @param account String 账号
     * @param apiCallBack ApiCallBack<WebModListData> 回调接口
     * @param limit String? 限制返回数量
     */
    fun soleRecommended(
        account: String,
        apiCallBack: ApiCallBack<WebModListData>,
        limit: String? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account)
        if (limit != null) {
            requestBodyBuilder.add("limit", limit)
        }
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=soleRecommended")
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
                        val finalWebModListData =
                            gson.fromJson(data, WebModListData::class.java)
                        handler.post {
                            apiCallBack.onResponse(finalWebModListData)
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
     * 添加下载数
     * @param modId String 模组Id
     * @param apiCallBack ApiCallBack<ApiResponse> api监听
     */
    fun addDownloadNum(modId: String, apiCallBack: ApiCallBack<ApiResponse>? = null) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("modId", modId)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=addDownloadNumber")
                .post(requestBody).build()
        val call = okHttpClient.newCall(request)
        val handler = Handler(Looper.getMainLooper())
        val gson = Gson()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                handler.post { apiCallBack?.onFailure(e) }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val data = response.body!!.string()
                    val finalApiResponse = gson.fromJson(data, ApiResponse::class.java)
                    handler.post {
                        apiCallBack?.onResponse(finalApiResponse)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.post {
                        apiCallBack?.onFailure(e)
                    }
                }
            }

        })
    }

    //audit
    /**
     * 审核模组
     * @param account String 账号（管理员）
     * @param modId String 模组id
     * @param state Boolean 通过状态
     * @param apiCallBack ApiCallBack<ApiResponse> 回调接口
     */
    fun auditMod(
        account: String,
        modId: String,
        state: Boolean,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("modId", modId)
                .add("state", state.toString())
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=audit")
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
                    val finalApiResponse = gson.fromJson(data, ApiResponse::class.java)
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
     * 重新审核模组
     * @param account String 账号
     * @param modId String 模组id
     * @param apiCallBack ApiCallBack<ApiResponse> 回调接口
     */
    fun afreshAuditMod(account: String, modId: String, apiCallBack: ApiCallBack<ApiResponse>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("modId", modId)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=afreshAuditMod")
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
                    val finalApiResponse = gson.fromJson(data, ApiResponse::class.java)
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
     * 下架模组(任何人都可以)
     * @param account String 账号
     * @param modId String 模组id
     * @param apiCallBack ApiCallBack<ApiResponse>
     */
    fun soldOutMod(account: String, modId: String, apiCallBack: ApiCallBack<ApiResponse>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account).add("modId", modId)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=soldOutMod")
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
                    val finalApiResponse = gson.fromJson(data, ApiResponse::class.java)
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
     * 获取用户制作的全部模组数据（包括未通过审核的）
     * @param account String 账号
     * @param apiCallBack ApiCallBack<WebModAllInfoData> 接口
     * @param sortMode SortMode? 排序方式
     * @param limit String? 限制数量
     */
    fun getUserModListAllInfo(
        account: String,
        apiCallBack: ApiCallBack<WebModAllInfoData>,
        sortMode: SortMode? = null,
        limit: String? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account)
        if (sortMode != null) {
            requestBodyBuilder.add("sortMode", sortMode.getValue())
        }
        if (limit != null) {
            requestBodyBuilder.add("limit", limit)
        }
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=userModListAllInfo")
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
                    val finalWebModAllInfoData =
                        gson.fromJson(data, WebModAllInfoData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebModAllInfoData)
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
     * 搜索模组
     * @param keyWord String
     * @param apiCallBack ApiCallBack<WebModListData>
     */
    fun search(keyWord: String, apiCallBack: ApiCallBack<WebModListData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("name", keyWord)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=searchMod")
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
                    val finalWebModData = gson.fromJson(data, WebModListData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebModData)
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
     * 获取用户制作的 模组数据
     * @param account String 用户名
     * @param apiCallBack ApiCallBack<WebModListData>
     * @param sortMode SortMode? 排序方式
     * @param limit Int? 限制数量
     */
    fun getUserModList(
        account: String,
        apiCallBack: ApiCallBack<WebModListData>,
        sortMode: SortMode? = null,
        limit: String? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account)
        if (sortMode != null) {
            requestBodyBuilder.add("sortMode", sortMode.getValue())
        }
        if (limit != null) {
            requestBodyBuilder.add("limit", limit)
        }
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=userModList")
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
                    val finalWebModData = gson.fromJson(data, WebModListData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebModData)
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
     *获取评论列表
     * @param modId String 模组Id
     * @param apiCallBack ApiCallBack<ApiResponse> 接口
     */
    fun getCommentsList(modId: String, apiCallBack: ApiCallBack<WebModCommentData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("modId", modId).build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=commentsList")
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
                    val finalWebModCommentData =
                        gson.fromJson(data, WebModCommentData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebModCommentData)
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
     * 发布评论
     * @param account String 账号
     * @param appId String appid
     * @param modId String 模组id
     * @param content String 评论内容
     * @param apiCallBack ApiCallBack<ApiResponse> 结果
     */
    fun sendComment(
        account: String,
        appId: String,
        modId: String,
        content: String,
        apiCallBack: ApiCallBack<ApiResponse>
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account).add("appId", appId).add("modId", modId)
                .add("content", content).build()
        val request =
            Request.Builder().url(ServerConfiguration.website + "php/mod.php?action=comments")
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
     * 查看模组信息
     * @param account String 账号
     * @param modId String 模组Id
     */
    fun getInfo(account: String, modId: String, apiCallBack: ApiCallBack<WebModInfoData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody =
            FormBody.Builder().add("account", account).add("modId", modId).build()
        val request =
            Request.Builder().url(ServerConfiguration.website + "php/mod.php?action=getInfo")
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
                    val finalWebModInfoData =
                        gson.fromJson(data, WebModInfoData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebModInfoData)
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
     * 加载模组列表
     * @param apiCallBack ApiCallBack<WebModListData> 回调接口
     * @param tag String? 标签
     * @param sortMode SortMode? 排序模式
     * @param limit String? 限制数量
     */
    fun list(
        apiCallBack: ApiCallBack<WebModListData>,
        tag: String? = null,
        sortMode: SortMode? = null,
        limit: String? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder()
        if (tag != null) {
            requestBodyBuilder.add("tag", tag)
        }
        if (sortMode != null) {
            requestBodyBuilder.add("sortMode", sortMode.getValue())
        }
        if (limit != null) {
            requestBodyBuilder.add("limit", limit)
        }
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder().url(ServerConfiguration.website + "php/mod.php?action=list")
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
                    val finalWebModData = gson.fromJson(data, WebModListData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebModData)
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
     * 获取审核列表
     * @param apiCallBack ApiCallBack<WebModListData>
     */
    fun getAuditList(
        apiCallBack: ApiCallBack<WebModListData>, sortMode: SortMode? = null,
        limit: String? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder()
        if (sortMode != null) {
            requestBodyBuilder.add("sortMode", sortMode.getValue())
        }
        if (limit != null) {
            requestBodyBuilder.add("limit", limit)
        }
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder().url(ServerConfiguration.website + "php/mod.php?action=auditList")
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
                    val finalWebModData = gson.fromJson(data, WebModListData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebModData)
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
     * 获取更新日志
     * @param modId String 模组id
     * @param apiCallBack ApiCallBack<WebModUpdateLogData> 回调接口
     */
    fun getUpdateRecord(modId: String, apiCallBack: ApiCallBack<WebModUpdateLogData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("modId", modId)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/mod.php?action=getUpdateRecord")
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
                    val finalWebModUpdateLogData =
                        gson.fromJson(data, WebModUpdateLogData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalWebModUpdateLogData)
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
     * 升级模组
     * @param appId String
     * @param modId String
     * @param account String
     * @param modName String
     * @param describe String
     * @param tags String
     * @param unitNum Int
     * @param versionName String
     * @param updateLog String
     * @param iconLink String?
     * @param file File
     * @param apiCallBack ApiCallBack<ApiResponse>
     * @param progressListener ProgressListener?
     * @param screenshotList ArrayList<String>?
     */
    fun updateMod(
        appId: String,
        modId: String,
        account: String,
        modName: String,
        describe: String,
        updateLog: String,
        tags: String,
        unitNum: Int,
        versionName: String,
        iconLink: String?,
        file: File,
        apiCallBack: ApiCallBack<ApiResponse>,
        progressListener: ProgressListener? = null, screenshotList: ArrayList<String>? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val builder =
            MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("appID", appId)
                .addFormDataPart("modId", modId).addFormDataPart("updateLog", updateLog)
                .addFormDataPart("account", account).addFormDataPart("modName", modName)
                .addFormDataPart("describe", describe).addFormDataPart("tags", tags)
                .addFormDataPart("versionName", versionName)
                .addFormDataPart("unitNumber", unitNum.toString())
                .addFormDataPart("file", file.name, file.asRequestBody())
        if (iconLink != null) {
            if (ServerConfiguration.canConvertedToFile(iconLink)) {
                val iconFile = File(iconLink)
                if (iconFile.exists()) {
                    builder.addFormDataPart("icon", iconFile.name, iconFile.asRequestBody())
                }
            } else {
                builder.addFormDataPart("icon", iconLink)
            }
        }
        if (screenshotList != null && screenshotList.size > 0) {
            for (i in 0 until 6) {
                if (screenshotList.size > i) {
                    val key = "screenshot_${i}"
                    val value = screenshotList[i]
                    if (ServerConfiguration.canConvertedToFile(value)) {
                        val screenshotFile = File(value)
                        if (screenshotFile.exists()) {
                            builder.addFormDataPart(
                                key,
                                screenshotFile.name,
                                screenshotFile.asRequestBody()
                            )
                        } else {
                            break
                        }
                    } else {
                        builder.addFormDataPart(key, value)
                    }
                } else {
                    break
                }
            }
        }
        val progressMultipartBody = ProgressMultipartBody(builder.build())
        val requestBody = progressMultipartBody

        val request =
            Request.Builder().url(ServerConfiguration.website + "php/mod.php?action=update")
                .post(requestBody).build()
        val call = okHttpClient.newCall(request)
        val handler = Handler(Looper.getMainLooper())
        if (progressListener != null) {
            progressMultipartBody.setProgressListener(progressListener)
        }
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
     * 发布模组
     * @param account String 账号
     * @param modName String 模组名称
     * @param describe String 描述
     * @param tags String 标签
     * @param unitNum Int 单位数
     * @param file File 文件
     */
    fun releaseMod(
        appId: String,
        modId: String,
        account: String,
        modName: String,
        describe: String,
        tags: String,
        unitNum: Int,
        versionName: String,
        iconLink: String?,
        file: File,
        apiCallBack: ApiCallBack<ApiResponse>,
        progressListener: ProgressListener? = null, screenshotList: ArrayList<String>? = null
    ) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val builder =
            MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("appID", appId)
                .addFormDataPart("modId", modId)
                .addFormDataPart("account", account).addFormDataPart("modName", modName)
                .addFormDataPart("describe", describe).addFormDataPart("tags", tags)
                .addFormDataPart("versionName", versionName)
                .addFormDataPart("unitNumber", unitNum.toString())
                .addFormDataPart("file", file.name, file.asRequestBody())
        if (iconLink != null) {
            if (ServerConfiguration.canConvertedToFile(iconLink)) {
                val iconFile = File(iconLink)
                if (iconFile.exists()) {
                    builder.addFormDataPart("icon", iconFile.name, iconFile.asRequestBody())
                }
            } else {
                builder.addFormDataPart("icon", iconLink)
            }
        }
        if (screenshotList != null && screenshotList.size > 0) {
            for (i in 0 until 6) {
                if (screenshotList.size > i) {
                    val key = "screenshot_${i}"
                    val value = screenshotList[i]
                    if (ServerConfiguration.canConvertedToFile(value)) {
                        val screenshotFile = File(value)
                        if (screenshotFile.exists()) {
                            builder.addFormDataPart(
                                key,
                                screenshotFile.name,
                                screenshotFile.asRequestBody()
                            )
                        } else {
                            break
                        }
                    } else {
                        builder.addFormDataPart(key, value)
                    }
                } else {
                    break
                }
            }
        }
        val progressMultipartBody = ProgressMultipartBody(builder.build())
        val requestBody = progressMultipartBody

        val request =
            Request.Builder().url(ServerConfiguration.website + "php/mod.php?action=release")
                .post(requestBody).build()
        val call = okHttpClient.newCall(request)
        val handler = Handler(Looper.getMainLooper())
        if (progressListener != null) {
            progressMultipartBody.setProgressListener(progressListener)
        }
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
}