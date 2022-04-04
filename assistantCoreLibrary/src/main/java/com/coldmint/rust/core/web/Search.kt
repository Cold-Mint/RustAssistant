package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import com.coldmint.rust.core.dataBean.SearchResultDataBean
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class Search private constructor() {

    companion object {
        val instance: Search by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Search()
        }
    }

    /**
     * 全局搜索
     * @param key String 关键字
     * @param apiCallBack ApiCallBack<SearchResultDataBean>
     */
    fun searchAll(key: String, apiCallBack: ApiCallBack<SearchResultDataBean>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("key", key)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/search.php?action=searchAll")
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
                    val finalSearchResultDataBean =
                        gson.fromJson(data, SearchResultDataBean::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalSearchResultDataBean)
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