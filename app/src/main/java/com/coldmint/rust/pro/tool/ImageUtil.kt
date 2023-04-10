package com.coldmint.rust.pro.tool

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.coldmint.rust.core.web.ServerConfiguration

/**
 * 图像工具
 */
object ImageUtil {

    /**
     * Drawable转Bitmap
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        //声明将要创建的bitmap
        var bitmap: Bitmap? = null;
        //获取图片宽度
        val width = drawable.getIntrinsicWidth();
        //获取图片高度
        val height = drawable.getIntrinsicHeight();
        //图片位深，PixelFormat.OPAQUE代表没有透明度，RGB_565就是没有透明度的位深，否则就用ARGB_8888。详细见下面图片编码知识。
        val config: Bitmap.Config = if (drawable.getOpacity() != PixelFormat.OPAQUE) {
            Bitmap.Config.ARGB_8888
        } else {
            Bitmap.Config.RGB_565
        }
        //创建一个空的Bitmap
        bitmap = Bitmap.createBitmap(width, height, config)
        //在bitmap上创建一个画布
        val canvas = Canvas(bitmap)
        //设置画布的范围
        drawable.setBounds(0, 0, width, height);
        //将drawable绘制在canvas上
        drawable.draw(canvas);
        return bitmap;
    }


    /**
     * 设置请求回调
     */
    fun getRequestListener(func: (Palette?) -> Unit): RequestListener<Drawable> {
        return object :
            RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                if (resource == null) {
                    return false
                }
                var bitmap = drawableToBitmap(resource)
                Palette.from(bitmap).generate {
                    func(it)
                }
                return false
            }

        }
    }

}