package com.coldmint.rust.core.turret

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.Drawable.createFromPath
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import com.coldmint.rust.core.R
import com.coldmint.rust.core.SourceFile

/**
 * 炮塔安装器
 * 此类用于安装炮塔，将炮塔安放到视图上
 */
class TurretInstaller(
    val parentView: ViewGroup,
    val turretManager: TurretManager
) {


    private var scale: Int = 1


    /**
     * 设置缩放比例
     */
    fun setScale(value: Int) {
        scale = value
    }

    /**
     * 构造方法（使用源文件构造）
     * @param parentView View
     * @param sourceFile SourceFile
     * @constructor
     */
    constructor(parentView: ViewGroup, sourceFile: SourceFile) : this(
        parentView,
        sourceFile.getTurretManager()
    )

    /**
     * 安装所有炮塔到视图容器内
     */
    @SuppressLint("ObjectAnimatorBinding")
    fun installAllTurrets() {
        val turretList = turretManager.turretList
        val size = turretList.size
        if (size > 0) {
            turretList.forEach {
                val imageView = ImageView(parentView.context)
                val imageFile = it.imageFile
                if (imageFile == null) {
                    setNoneImage(imageView)
                } else {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    if (bitmap == null) {
                        setNoneImage(imageView)
                    } else {
                        imageView.setImageBitmap(bitmap)
                        val layoutParams =
                            ViewGroup.LayoutParams(bitmap.width * scale, bitmap.height * scale)
                        imageView.layoutParams = layoutParams
                    }
                }
                imageView.setOnTouchListener { view, motionEvent ->
                    if (view == null || motionEvent == null) {
                        false
                    }
                    val x = motionEvent.x
                    val y = motionEvent.y
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
//                            val setDown = AnimatorSet()
//                            setDown.playTogether(
//                                ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.5f),
//                                ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.5f),
//                                ObjectAnimator.ofFloat(this, "alpha", 1f, 0.5f)
//                            )
//                            setDown.start()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            view.setX(x + view.getLeft() + view.getTranslationX() - view.getWidth() / 2);
                            view.setY(y + view.getTop() + view.getTranslationY() - view.getHeight() / 2);
                        }
                        MotionEvent.ACTION_UP -> {
//                            val setUp = AnimatorSet()
//                            setUp.playTogether(
//                                ObjectAnimator.ofFloat(this, "scaleX", 1.5f, 1f),
//                                ObjectAnimator.ofFloat(this, "scaleY", 1.5f, 1f),
//                                ObjectAnimator.ofFloat(this, "alpha", 0.5f, 1f)
//                            );
//                            setUp.start()
                        }
                        else -> {
                            false
                        }
                    }
                    true
                }
                parentView.addView(imageView)
            }
        }
    }


    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 设置空白图像(不受缩放限制)
     * @param imageView ImageView 图片框
     * @param size Int 尺寸
     */
    private fun setNoneImage(
        imageView: ImageView,
        size: Int = dip2px(parentView.context, 30.toFloat())
    ) {
        imageView.setImageResource(R.drawable.image)
        //设置默认图像大小
        val layoutParams =
            ViewGroup.LayoutParams(size, size)
        imageView.layoutParams = layoutParams
    }

}