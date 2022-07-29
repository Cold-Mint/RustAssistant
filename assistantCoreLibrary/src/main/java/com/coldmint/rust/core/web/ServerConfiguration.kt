package com.coldmint.rust.core.web

import android.os.Environment
import android.util.Log
import com.coldmint.rust.core.tool.FileOperator
import okhttp3.*
import java.io.File
import java.io.IOException
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 服务器配置
 */
object ServerConfiguration {

    //正式:http://39.105.229.249/
    //本地:http://10.0.2.2/

    //电脑查 ip [ipconfig]
    private const val testIp = "http://10.0.2.2/rust-assistant-backstage/"
    const val defaultIp = "http://39.105.229.249/"
    var website = defaultIp
    const val Success_Code = 0
    const val Error_Code = 1
    const val ForeverTime = "forever"


    /**
     * 将整数转换为字符串，加上"万"
     * @param num Int
     * @return String
     */
    fun numberToString(num: Int): String {
        return if (num > 10000) {
            "${num / 10000}.${(num % 10000) / 1000}万"
        } else {
            num.toString()
        }
    }

    /**
     * 将服务器响应的时间字符串转化为时间戳
     * @param string 时间字符串
     * @return 时间戳，转换失败返回-1，永久用户返回-2
     */
    fun toLongTime(string: String): Long {
        if (string == ForeverTime) {
            return -2
        } else {
            val form = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            try {
                val date = form.parse(string)
                return date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return -1
        }
    }

    /**
     * 将Long表示的时间转换为String表示
     * @param longTime Long
     * @return String
     */
    fun toStringTime(longTime: Long): String {
        return when (longTime) {
            (-2).toLong() -> {
                ForeverTime
            }
            (0).toLong() -> {
                "#"
            }
            else -> {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                formatter.format(longTime)
            }
        }
    }

//    fun toTime(stringTime: String): String {
//        //记录的时间
//        val num = toLongTime(stringTime)
//        //如果不是永久且计算成功
//        if (num >= 0) {
//            //现在的时间
//            val now = System.currentTimeMillis()
//            if (num > now) {
//                //在现在之后
//            } else {
//                if (num == now) {
//                    //现在
//                    return "刚刚"
//                } else {
//                    //在现在之前
//                    val difference = now - num
//
//                    return
//                }
//            }
//        }
//        return stringTime
//    }
//
//    fun time(longTime: Long){
//        val year = longTime % 31536000000
//    }


    /**
     * 获取okHttpClient对象
     * @return OkHttpClient
     */
    fun initOkHttpClient(): OkHttpClient {
        //TimeUnit.MILLISECONDS 毫秒
        val builder = OkHttpClient.Builder()
        builder.proxy(Proxy.NO_PROXY)
        builder.proxySelector(object : ProxySelector() {
            override fun select(uri: URI?): MutableList<Proxy> {
                return Collections.singletonList(Proxy.NO_PROXY)
            }

            override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {

            }

        })

        return builder.build()
    }


    /**
     * 获取真实的文件链接
     * 将服务器上的相对路径转换为可识别的绝对路径，若输入路径为绝对路径(以http或https开头)则不做处理
     * 将../替换为网站根路径
     * @param string String
     */
    fun getRealLink(
        string: String
    ): String {
        return if (string.isBlank()) {
            //为空抛出异常
            throw NullPointerException("String不能为空")
        } else if (string.startsWith("http://") || string.startsWith("https://")) {
            //如果说直链
            Log.d("真实路径组合", "直链" + string)
            string
        } else if (string.startsWith(Environment.getExternalStorageDirectory().absolutePath) || string.startsWith(
                "/data/"
            )
        ) {
            //如果是文件路径
            Log.d("真实路径组合", "是文件路径" + string)
            string
        } else {
            //如果开头包含../上级目录
            val key = "../"
            val start = string.indexOf(key)
            val result = if (start > -1) {
                //开头是../
                "${website}${string.substring(start + key.length)}"
            } else {
                val key2 = "/"
                if (string.startsWith(key2)) {
                    "${website}${string.substring(key2.length, string.length)}"
                } else {
                    "${website}/${string}"
                }
            }
            Log.d("真实路径组合", result)
            result
        }
    }


    /**
     * 转换为服务器上的相对路径
     * @param string String
     * @return String
     */
    fun toRelativePath(string: String, sourceFileDirectory: String = "php/"): String {
        var path = FileOperator.getRelativePath(string, website) ?: string
        if (!path.startsWith(sourceFileDirectory) && !string.startsWith(Environment.getExternalStorageDirectory().absolutePath) && !string.startsWith(
                "/data/"
            )
        ) {
            if (string.startsWith("http://") || string.startsWith("https://")) {
                return string
            } else {
                //如果不是源文件目录的子目录话，设置为上级目录
                path = "../" + path
            }
        }
        return path
    }

    /**
     * 是否可以转换成文件
     * @param link String 链接
     */
    fun canConvertedToFile(link: String): Boolean {
        return link.startsWith(Environment.getExternalStorageDirectory().absolutePath) || link.startsWith(
            "/data/"
        )
    }


    /**
     * 是否为事件挂载(本地处理)
     * @param data String 数据
     * @return Boolean
     */
    fun isEvent(data: String): Boolean {
        return data.startsWith("@event:")
    }

}