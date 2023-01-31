package com.coldmint.rust.core.tool

import android.R
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.*
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import java.io.File
import java.security.MessageDigest
import java.util.*


/**
 * 程序交互类
 *
 * @author Cold Mint
 * @date 2021/10/21 17:35
 */
object AppOperator {

    /**
     * 网络类型枚举
     */
    enum class NetWorkType {
        NetWorkType_Wifi, NetWorkType_Moble, NetWorkType_None
    }

    /**
     * 获取网络类型
     * @param context Context 上下文环境
     * @return NetWorkType 网络类型
     */
    fun getNetworkType(context: Context): NetWorkType {
        val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT < 23) {
                val info = connectivityManager.activeNetworkInfo
                if (info != null) {
                    if (info.type == ConnectivityManager.TYPE_WIFI) {
                        return NetWorkType.NetWorkType_Wifi
                    } else if (info.type == ConnectivityManager.TYPE_MOBILE) {
                        return NetWorkType.NetWorkType_Moble
                    }
                }
            } else {
                val network = connectivityManager.activeNetwork
                if (network != null) {
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    if (networkCapabilities != null) {
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return NetWorkType.NetWorkType_Wifi
                        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return NetWorkType.NetWorkType_Moble
                        }
                    }
                }
            }
        }
        return NetWorkType.NetWorkType_None
    }

    /**
     * 获取程序版本号
     * @param context Context 上下文环境
     * @param packagename String? 包名（为空返回自身版本号）
     * @return Int
     */
    fun getAppVersionNum(context: Context, packageName: String? = null): Int {
        return try {
            val packageManager = context.packageManager
            val packageInfo = if (packageName == null) {
                packageManager.getPackageInfo(context.packageName, 0)
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    /*打开某个应用的系统设置（此方法返回Intent）*/
    fun openSettings(packagename: String): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.parse("package:$packagename")
        return intent
    }

    /**
     * 使用浏览器访问网页
     *
     * @param context 上下文环境
     * @param uri     网页链接
     */
    fun useBrowserAccessWebPage(context: Context, uri: String?): Boolean {
        return try {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            val contentUrl = Uri.parse(uri)
            intent.data = contentUrl
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 检查代理网络（Vpn是否打开）
     * @return Boolean
     */
    fun checkWifiProxy(context: Context): Boolean {
        val IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
        val proxyAddress: String?
        val proxyPort: Int?
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost")
            val portStr = System.getProperty("http.proxyPort")
            proxyPort = Integer.parseInt(portStr ?: "-1")
        } else {
            proxyAddress = android.net.Proxy.getHost(context)
            proxyPort = android.net.Proxy.getPort(context)
        }
//        Log.i("cxmyDev","proxyAddress : ${proxyAddress}, prot : ${proxyPort}")
        return !TextUtils.isEmpty(proxyAddress) && proxyPort != -1
    }


    /**
     * 打开App
     *
     * @param context     上下文环境
     * @param packageName 包名
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun openApp(context: Context, packageName: String) {
        try {
            var mainAct: String? = null
            val pkgMag = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
            @SuppressLint("WrongConstant") val list =
                pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES)
            for (i in list.indices) {
                val info = list[i]
                if (info.activityInfo.packageName == packageName) {
                    mainAct = info.activityInfo.name
                    break
                }
            }
            if (mainAct!!.isEmpty()) {
                return
            }
            intent.component = ComponentName(packageName, mainAct)
            context.startActivity(intent)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    /**
     * 通知系统相册更新
     * @param context Context
     * @param file File
     */
    @Deprecated("不建议使用这个，可能无效，或引发程序奔溃。")
    fun updateTheAlbum(context: Context, file: File, description: String? = null): Boolean {
        try {
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                file.absolutePath,
                file.name,
                description
            )
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * 应用是否安装
     *
     * @param context     上下文环境
     * @param packageName 包名
     * @return 是否安装
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_UNINSTALLED_PACKAGES
            )
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 打开qq群卡片
     *
     * @param context     上下文环境
     * @param groupNumber 群号
     * @return 是否跳转成功
     */
    fun openQQGroupCard(context: Context, groupNumber: Int): Boolean {
        val intent = Intent()
        val uri =
            Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=${groupNumber}&card_type=group&source=qrcode")
        intent.data = uri
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 向其他App分享内容
     * @param context Context
     * @param title String
     * @param content String
     */
    fun shareText(context: Context, title: String, content: String) {
        DebugHelper.printLog("分享文本", content, "分享")
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = "text/plain"
        context.startActivity(Intent.createChooser(sendIntent, title))
    }


    /**
     * 获取应用签名（MD5）
     * @param context Context 上下文环境
     * @param packageName String 包名(可选，默认为自身)
     * @return String? 包名
     */
    fun getSignature(context: Context, packageName: String = context.packageName): String? {
        try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo
                signingInfo.apkContentsSigners
            } else {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                packageInfo.signatures
            }
            return if (signatures != null && signatures.isNotEmpty()) {
                val sign = signatures[0]
                encryptionMD5(sign.toByteArray()).lowercase(Locale.getDefault())
            } else {
                null
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 转换MD5签名
     * @param byteStr ByteArray
     * @return String
     */
    private fun encryptionMD5(byteStr: ByteArray): String {
        val messageDigest: MessageDigest
        val md5StrBuff = StringBuffer()
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(byteStr)
            val byteArray = messageDigest.digest()
            for (aByteArray in byteArray) {
                if (Integer.toHexString(0xFF and aByteArray.toInt()).length == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF and aByteArray.toInt()))
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF and aByteArray.toInt()))
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return md5StrBuff.toString()
    }


}