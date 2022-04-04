package com.coldmint.rust.core.tool

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import okhttp3.*
import okio.IOException
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.concurrent.thread

/**
 * @author Cold Mint
 * @date 2021/12/17 9:56
 * 文件下载器
 */
class FileLoader private constructor(var url: String, var path: String) {

    companion object {
        private var instantiate: FileLoader? = null

        fun getInstantiate(url: String, path: String): FileLoader {
            if (instantiate == null) {
                synchronized(FileLoader::class.java) {
                    if (instantiate == null) {
                        instantiate = FileLoader(url, path)
                    }
                }
            }
            instantiate!!.url = url
            instantiate!!.path = path
            return instantiate!!
        }
    }

    /**
     * 下载文件
     */
    fun download(downloadListener: ProgressResponseBody.ResponseProgressListener) {
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor {
                val response = it.proceed(it.request())
                //换成ProgressResponseBody
                response.newBuilder()
                    .body(ProgressResponseBody(response.body, downloadListener))
                    .build()
            }
            .build()
        val handler = Handler(Looper.getMainLooper())
        val request = Request.Builder().url(url).build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { downloadListener.downloadFail(e) }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body
                if (body != null) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                    val writerOk = writeFile(file, body)
                    if (writerOk) {
                        handler.post { downloadListener.downloadSuccess() }
                    } else {
                        handler.post { downloadListener.downloadFail(IOException()) }
                    }
                } else {
                    handler.post {
                        downloadListener.downloadFail(NullPointerException())
                    }
                }
            }

        })

    }

    /**
     * 写出文件
     * @param file File
     * @param body ResponseBody
     */
    private fun writeFile(file: File, body: ResponseBody): Boolean {
        val outputStream = BufferedOutputStream(FileOutputStream(file))
        val inputStream = BufferedInputStream(body.byteStream())
        var hasError = false
        try {
            var len = 0
            val byteArray = ByteArray(1024)
            len = inputStream.read(byteArray)
            while (len > -1) {
                outputStream.write(byteArray, 0, len)
                len = inputStream.read(byteArray)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            hasError = true
        }
        outputStream.close()
        inputStream.close()
        return !hasError
    }

}