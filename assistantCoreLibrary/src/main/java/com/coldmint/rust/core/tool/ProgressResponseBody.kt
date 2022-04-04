package com.coldmint.rust.core.tool

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.File

class ProgressResponseBody(
    //真正的ResponseBody
    private val responseBody: ResponseBody?,
    //回调接口
    private val progressListener: ResponseProgressListener
) : ResponseBody() {
    //读取响应体时的缓冲区
    private lateinit var bufferedSource: BufferedSource

    //响应体的总大小
    override fun contentLength(): Long {
        return responseBody!!.contentLength()
    }

    //contentType，这里没用到，但是都要重载
    override fun contentType(): MediaType? {
        return responseBody?.contentType()
    }

    /*上面bufferedSource的注释说到读取响应体时需要缓冲区
    就是在这里通过Okio.buffer(Source)获取的
    但是这样在读取响应体时，接口方法就没有调用
    解决方法是利用Okio提供的ForwardingSource可以用来包装Source
    有兴趣的可以看一下下面的ForwardingSource部分源码
    */
    override fun source(): BufferedSource {
        if (!this::bufferedSource.isInitialized)
            bufferedSource = source(responseBody!!.source()).buffer()
        return bufferedSource
    }

    //构建ForwardingSource
    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            private var totalBytesRead = 0L
            override fun read(sink: Buffer, byteCount: Long): Long {
                //读缓冲区的数据，得到读了多少字节
                val bytesRead = super.read(sink, byteCount)
                if (bytesRead != -1L)
                    totalBytesRead += bytesRead
                //接口回调
                progressListener.update(
                    totalBytesRead,
                    responseBody!!.contentLength(),
                    bytesRead == -1L
                )
                return bytesRead
            }
        }
    }

    //回调接口
    interface ResponseProgressListener {
        //参数名应该都写的很清楚是什么了
        fun update(bytesRead: Long, contentLength: Long, done: Boolean)

        fun downloadFail(exception: Exception?)

        fun downloadSuccess()
    }
}