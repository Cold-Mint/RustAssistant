package com.coldmint.rust.pro.tool

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.coldmint.rust.core.dataBean.mod.WebModUpdateLogData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.permissionx.guolindev.PermissionX
import jp.wasabeef.glide.transformations.*
import jp.wasabeef.glide.transformations.internal.Utils

/*全局方法类*/
object GlobalMethod {

    /**
     * 是否为激活状态
     */
    var isActive = false
    const val DEFAULT_GAME_PACKAGE = "com.corrodinggames.rts"
    const val DEBUG_SIGN = "963dfd616924b27f9247a35e45bc130a"
    const val RELEASE_SIGN = "5320b24894fe7ed449842a81a2dfceda"
    const val Event_LOGOUT = "logOut"
    const val Event_LOGIN = "logIn"


    /**
     * 转dp
     * @param i Int
     * @return Int
     */
    fun dp2px(i: Int): Int {
        return (Resources.getSystem().displayMetrics.density * i + 0.5f).toInt()
    }


    /**
     * 获取主题色
     *
     * @param context 上下文环境
     * @param resId   资源id
     * @return 成功返回值，失败返回-1
     */
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


    /**
     * 获取Glide请求设置
     * @return RequestOptions
     */
    fun getRequestOptions(circleCrop: Boolean = false, grayscale: Boolean = false): RequestOptions {
        //变换列表
        val transformations = ArrayList<BitmapTransformation>()
        if (circleCrop) {
            transformations.add(CropCircleTransformation())
        }
        if (grayscale) {
            transformations.add(GrayscaleTransformation())
        }
        //请求设置
        val requestOptions = if (transformations.isNotEmpty()) {
            val multi = MultiTransformation<Bitmap>(
                transformations
            )
            RequestOptions.bitmapTransform(multi)
        } else {
            RequestOptions()
        }
        requestOptions.placeholder(R.drawable.image)
            .error(R.drawable.image_not_supported)
        return requestOptions
    }


    /**
     * 创建PopMenu
     */
    fun createPopMenu(view: View): PopupMenu {
        val context = view.context;
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            PopupMenu(
                context,
                view,
                Gravity.NO_GRAVITY,
                0, R.style.Widget_Material3_PopupMenu
            )
        } else {
            PopupMenu(context, view)
        }
    }

    /**
     * int颜色值转String
     * @param color Int
     * @param useARGB Boolean
     * @return String
     */
    fun colorToString(color: Int, useARGB: Boolean = true): String {
        val builder = StringBuilder()
        builder.append('#')
        if (useARGB) {
            builder.append(convertDigital(Color.alpha(color)))
        }
        builder.append(convertDigital(Color.red(color)))
        builder.append(convertDigital(Color.green(color)))
        builder.append(convertDigital(Color.blue(color)))
        return builder.toString()
    }

    /**
     * 展示颜色选择对话框
     * @param context Context
     * @param func Function1<String, Unit>
     */
    fun showColorPickerDialog(
        context: Context,
        useARGB: Boolean = false, func: ((String) -> Unit)
    ) {
        ColorPickerDialogBuilder
            .with(context).showAlphaSlider(useARGB)
            .setTitle(context.getString(R.string.choose_color))
            .initialColor(Color.WHITE)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .setOnColorSelectedListener {
                //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
            }
            .setPositiveButton(R.string.dialog_ok) { dialog, selectedColor, allColors ->
                func.invoke(colorToString(selectedColor, useARGB))
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, which -> }
            .build()
            .show()
    }

    /**
     * 转换为16进制
     *
     * @param num 十进制整数
     * @return 16进制数
     */
    private fun convertDigital(num: Int): String {
        return if (num > 255) {
            "FF"
        } else {
            val builder = java.lang.StringBuilder()
            val result = num / 16
            val remainder = num % 16
            when (result) {
                10 -> builder.append('A')
                11 -> builder.append('B')
                12 -> builder.append('C')
                13 -> builder.append('D')
                14 -> builder.append('E')
                15 -> builder.append('F')
                else -> builder.append(result)
            }
            when (remainder) {
                10 -> builder.append('A')
                11 -> builder.append('B')
                12 -> builder.append('C')
                13 -> builder.append('D')
                14 -> builder.append('E')
                15 -> builder.append('F')
                else -> builder.append(remainder)
            }
            builder.toString()
        }
    }


    /**
     * 显示更新日志
     * @param context Context
     * @param modId String
     */
    fun showUpdateLog(context: Context, modId: String) {
        WebMod.instance.getUpdateRecord(modId, object : ApiCallBack<WebModUpdateLogData> {
            override fun onResponse(t: WebModUpdateLogData) {
                try {
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
                            MaterialAlertDialogBuilder(context).setTitle(title)
                                .setMessage(stringBuilder.toString()).setCancelable(false)
                                .setPositiveButton(R.string.dialog_ok) { i, i2 ->
                                }.show()
                        } else {
                            Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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