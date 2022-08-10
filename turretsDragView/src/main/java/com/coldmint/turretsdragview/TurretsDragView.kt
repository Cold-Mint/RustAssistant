package com.coldmint.turretsdragview

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View

/**
 * 炮塔拖拽视图
 */
class TurretsDragView(context: Context) : View(context) {

    val key = "炮塔拖拽视图"

    //主要位图变量
    private var baseBitmap: Bitmap? = null

    //画布
    private var canvas: Canvas? = null

    //画笔
    private val paint by lazy {
        Paint()
    }

    //路径与位图之间的映射
    val map = HashMap<String, Bitmap>()


    /**
     * 获取位图
     * 优先从映射里读取。
     * @param path String
     * @return Bitmap
     */
    fun getBitmap(path: String): Bitmap {
        var bitmap = map[path]
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeFile(path)
            map[path] = bitmap
        }
        return bitmap!!
    }

    /**
     * 设置主体图像
     */
    fun setBaseImage(path: String) {
        baseBitmap = getBitmap(path)
    }

    /**
     * 炮塔列表数据
     */
    private val turretList by lazy {
        ArrayList<TurretData>()
    }

    /**
     * 当测量视图
     * @param widthMeasureSpec Int
     * @param heightMeasureSpec Int
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    /**
     * 当绘制视图
     * @param canvas Canvas
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas
        if (baseBitmap == null) {
            Log.e(key, "未设置炮塔主体图像")
            throw NullPointerException("未设置炮塔主体图像")
        }else{
            val matrix = Matrix()
            canvas!!.drawBitmap(baseBitmap!!,matrix, paint)
        }
    }
}