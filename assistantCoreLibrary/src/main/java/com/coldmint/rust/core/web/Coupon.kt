package com.coldmint.rust.core.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.CouponListDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

/**
 * 优惠券类
 * @author Cold Mint
 * @date 2022/1/10 20:17
 */
@Deprecated("已废弃")
class Coupon {

    companion object {
        val instance: Coupon by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Coupon()
        }
    }

    /**
     * 获取可用的折扣券列表
     * @param account String
     * @param apiCallBack ApiCallBack<CouponListDataBean>
     */
    fun list(account: String, apiCallBack: ApiCallBack<CouponListDataBean>) {
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val requestBodyBuilder: FormBody.Builder =
            FormBody.Builder().add("account", account)
        val requestBody = requestBodyBuilder.build()
        val request =
            Request.Builder()
                .url(ServerConfiguration.website + "php/coupons.php?action=list")
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
                    val finalCouponListDataBean = gson.fromJson(data, CouponListDataBean::class.java)
                    handler.post {
                        apiCallBack.onResponse(finalCouponListDataBean)
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