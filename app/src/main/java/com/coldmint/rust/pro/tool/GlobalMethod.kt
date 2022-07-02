package com.coldmint.rust.pro.tool

import android.Manifest
import android.app.Activity
import android.os.Environment
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.content.Intent
import android.util.TypedValue
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.R
import android.widget.TextView
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.google.android.material.snackbar.Snackbar
import android.graphics.drawable.Drawable
import android.content.res.ColorStateList
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.rust.core.dataBean.mod.WebModUpdateLogData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.MainActivity
import com.permissionx.guolindev.PermissionX
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/*全局方法类*/
object GlobalMethod {

    /**
     * 是否为激活状态
     */
    var isActive = false
    const val DEFAULT_GAME_PACKAGE = "com.corrodinggames.rts"
    const val DEBUG_SIGN = "963dfd616924b27f9247a35e45bc130a"
    const val RELEASE_SIGN = "5320b24894fe7ed449842a81a2dfceda"


    /**
     * 获取Glide请求设置
     * @return RequestOptions
     */
    fun getRequestOptions(circleCrop: Boolean = false): RequestOptions {
        val requestOptions = if (circleCrop) {
            RequestOptions.circleCropTransform()
        } else {
            RequestOptions().placeholder(R.drawable.image).error(R.drawable.image_not_supported)
        }
        return requestOptions
    }

    /**
     * 显示更新日志
     * @param context Context
     * @param modId String
     */
    fun showUpdateLog(context: Context, modId: String) {
        WebMod.instance.getUpdateRecord(modId, object : ApiCallBack<WebModUpdateLogData> {
            override fun onResponse(t: WebModUpdateLogData) {
                if (t.code == ServerConfiguration.Success_Code) {
                    val data = t.data
                    if (data != null && data.isNotEmpty()) {
                        val stringBuilder = StringBuilder()
                        data.forEach {
                            stringBuilder.append(it.versionName)
                            stringBuilder.append("\n")
                            stringBuilder.append(it.updateLog)
                            stringBuilder.append("\n")
                            stringBuilder.append(it.time)
                            stringBuilder.append("\n\n------\n\n")
                        }
                        val title =
                            context.getString(R.string.update_record) + "(" + data.size + ")"
                        MaterialDialog(context, BottomSheet()).show {
                            title(text = title).message(text = stringBuilder.toString())
                                .cancelable(false)
                                .positiveButton(R.string.dialog_ok)
                        }
                    } else {
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.network_error),
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    /**
     * 请求权限方法
     * @param activity FragmentActivity 活动
     * @param requestCompleted Function0<Unit> 当获取权限完成的函数
     */
    fun requestStoragePermissions(activity: FragmentActivity, requestCompleted: (Boolean) -> Unit) {
        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            listOf(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            )
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        PermissionX.init(activity).permissions(
            list
        ).onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                deniedList,
                activity.getString(R.string.dialog_title),
                activity.getString(R.string.dialog_confirm)
            )
        }
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    activity.getString(R.string.dialog_title),
                    activity.getString(R.string.dialog_confirm)
                )
            }
            .request { allGranted, grantedList, deniedList ->
                requestCompleted.invoke(allGranted)
            }
    }

    /**
     * 获取主题色
     *
     * @param context 上下文环境
     * @param resId   资源id
     * @return 成功返回值，失败返回-1
     */
    @Deprecated("废弃")
    fun getThemeColor(context: Context, resId: Int): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(resId, typedValue, true)) {
            typedValue.data
        } else {
            -1
        }
    }

    /**
     * 获取主要色
     *
     * @param context 上下文环境
     * @return 整数
     */
    @Deprecated("废弃")
    fun getColorPrimary(context: Context): Int {
        return getThemeColor(context, R.attr.colorPrimary)
    }

    /**
     * 获取暗色主要色
     *
     * @param context 上下文环境
     * @return 整数
     */
    fun getDarkColorPrimary(context: Context): Int {
        return getThemeColor(context, R.attr.colorPrimaryDark)
    }

    //设置删除线
    fun addDeleteLine(vararg textViews: TextView?) {
        for (textView in textViews) {
            if (textView != null) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
    }

    //移除删除线
    fun removeDeleteLine(vararg textViews: TextView?) {
        for (textView in textViews) {
            if (textView != null) {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    /**
     * 复制文本
     *
     * @param context  上下文环境
     * @param text     文本
     * @param showView 展示的视图（设置为null则不展示提示）
     */
    fun copyText(context: Context, text: String, showView: View? = null) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(text, text)
        clipboardManager.setPrimaryClip(clipData)
        if (showView != null) {
            Snackbar.make(
                showView,
                String.format(context.getText(R.string.copy_complete).toString(), text),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    //打开指定app
    //图像着色
    fun tintDrawable(drawable: Drawable?, colors: ColorStateList?): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTintList(wrappedDrawable, colors)
        return wrappedDrawable
    }
}