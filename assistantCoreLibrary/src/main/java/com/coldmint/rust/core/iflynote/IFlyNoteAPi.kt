package com.coldmint.rust.core.iflynote

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.coldmint.rust.core.dataBean.iflynote.DocData
import com.coldmint.rust.core.dataBean.iflynote.NoteData
import com.google.gson.Gson
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.web.ServerConfiguration
import kotlin.Throws
import okhttp3.*
import java.io.IOException

/**
 * 讯飞语记 api
 */
@Deprecated("已停用，因为可能后台被ban")
class IFlyNoteAPi(private val url: String) {
    /**
     * 获取类型
     *
     * @return
     */
    var type: String? = null

    /**
     * 是否可用
     *
     * @return
     */
    var isAvailable = false
    private var apiLink: String
    private var docId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var delayed = 300
    private val gson = Gson()

    /**
     * 设置延迟
     *
     * @param delayed 延迟
     */
    fun setDelayed(delayed: Int) {
        this.delayed = delayed
    }

    /**
     * 获取网页源代码
     * 接口泛型
     * 如果类型为note 则返回[NoteData]封装类
     * 如果是类型是doc（新版） 则返回[DocData] 封装类
     * 其他类型返回源码
     *
     * @param apiCallBack api接口
     */
    fun getCode(apiCallBack: ApiCallBack<Any>) {
        if (!isAvailable) {
            apiCallBack.onFailure(IOException())
            return
        }
        val okHttpClient = ServerConfiguration.initOkHttpClient()
        val request: Request = Request.Builder().url(apiLink).get().build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.postDelayed({ apiCallBack.onFailure(e) }, delayed.toLong())
            }

            @Throws(Exception::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val code = response.body?.string()
                    if (code == null) {
                        handler.postDelayed({
                            apiCallBack.onFailure(NullPointerException("code变量为空"))
                        }, delayed.toLong())
                    } else {
                        when (type) {
                            "note" -> {
                                val noteData = gson.fromJson(code, NoteData::class.java)
                                handler.postDelayed(
                                    { apiCallBack.onResponse(noteData) },
                                    delayed.toLong()
                                )
                            }
                            "doc" -> {
                                val docData = gson.fromJson(code, DocData::class.java)
                                handler.postDelayed(
                                    { apiCallBack.onResponse(docData) },
                                    delayed.toLong()
                                )
                            }
                            else -> {
                                handler.postDelayed({
                                    apiCallBack.onResponse(code)
                                }, delayed.toLong())
                            }
                        }
                    }
                } catch (e: Exception) {
                    handler.postDelayed({
                        apiCallBack.onFailure(e)
                    }, delayed.toLong())
                }
            }
        })
    }

    init {
        //https://iflynote.com/h/s/doc/7bV1LgxjYl8GhOet
        val head = "iflynote.com/h/s/"
        val tail = "/"
        val start = url.indexOf(head)
        val end = url.indexOf(tail, start + head.length)
        if (start > -1 && end > -1) {
            isAvailable = true
            type = url.substring(start + head.length, end)
            docId = url.substring(end + tail.length)
            apiLink = "https://api.iflynote.com/notes/share/$type/shareFileDetail?fid=$docId"
        } else {
            isAvailable = false
            apiLink = ""
            docId = null
            type = null
        }
    }
}