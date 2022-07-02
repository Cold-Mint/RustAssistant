package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.AppUpdateData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class AppUpdate {

    /**
     * 激活账号
     */
    fun getUpdate(apiCallBack: ApiCallBack<AppUpdateData>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/appUpdate.php?action=getUpdate")
                .get().build()
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
                    val finalAppUpdateData =
                        gson.fromJson(data, AppUpdateData::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalAppUpdateData)
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