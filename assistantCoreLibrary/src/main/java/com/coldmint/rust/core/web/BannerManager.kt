package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import com.coldmint.rust.core.dataBean.BannerItemDataBean
import com.coldmint.rust.core.dataBean.PlanDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

/**
 * 轮播图管理器
 */
@Deprecated("已废弃")
class BannerManager private constructor() {
    companion object {
        val instance: BannerManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BannerManager()
        }
    }

    /**
     * 获取BannerItems
     * @param apiCallBack ApiCallBack<BannerItemDataBean>
     */
    fun getItems(apiCallBack: ApiCallBack<BannerItemDataBean>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBody: FormBody = FormBody.Builder().build()
        val request = Request.Builder()
            .url(ServerConfiguration.website + "php/banner.php?action=getItems")
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
                    val finalBannerItemDataBean =
                        gson.fromJson(data, BannerItemDataBean::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalBannerItemDataBean)
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