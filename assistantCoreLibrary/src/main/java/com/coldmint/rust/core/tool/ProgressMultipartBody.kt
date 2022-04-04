package com.coldmint.rust.core.tool

import com.coldmint.rust.core.interfaces.ProgressListener
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.*

/**
 * 带有进度的MultipartBody代理类
 * @author Cold Mint
 * @date 2021/12/8 15:38
 */
class ProgressMultipartBody(val requestBody: RequestBody) : RequestBody() {
    private var currentLength: Long = 0
    private var listener: ProgressListener? = null

    fun setProgressListener(progressListener: ProgressListener) {
        listener = progressListener
    }

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun writeTo(sink: BufferedSink) {
        val totalLength = contentLength()
        val finalListener = listener
        val forwardingSink = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                // 每次写都会来这里
                currentLength += byteCount;
                finalListener?.onProgress(totalLength, currentLength)
                super.write(source, byteCount);
            }
        }
        val bufferedSink = forwardingSink.buffer();
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    override fun contentLength(): Long {
        return requestBody.contentLength()
    }
}