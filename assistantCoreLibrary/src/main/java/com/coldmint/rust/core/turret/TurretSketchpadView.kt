package com.coldmint.rust.core.turret

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.coldmint.rust.core.R

/**
 * 炮塔画板
 * @constructor
 */
class TurretSketchpadView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {


    private val debug = "炮塔画板"
    private var mainImagePath: String? = null

    //计算中心点
    private var centreX = 0
    private var centreY = 0

    //计算绘制点  取图像中点坐标(图像尺寸*缩放比例/2)
    private var startX = 0
    private var startY = 0
    private var endX = 0
    private var endY = 0

    //单元格宽度
    private var cellSize = 10


    //设置是否绘制坐标
    var drawCoordinate = true

    init {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.turretSketchpadView)
        //读取自定义属性
        drawCoordinate = typedArray.getBoolean(R.styleable.turretSketchpadView_drawCoordinate, true)
        typedArray.recycle()
    }

    /**
     * 设置主要图像
     * @param path String
     */
    fun setImage(path: String) {
        mainImagePath = path
        invalidate()
    }


    /**
     * 转换为游戏坐标
     * @param androidCoordinateData CoordinateData
     * @return CoordinateData
     */
    fun toGameCoordinate(androidCoordinateData: CoordinateData): CoordinateData {
        val x = (androidCoordinateData.x - centreX) / cellSize
        val y = (androidCoordinateData.y - centreY) / cellSize
        val game = CoordinateData(x, y)
        Log.d(
            debug,
            "转换坐标，安卓坐标${androidCoordinateData} 游戏坐标${game}"
        )
        return game
    }

    /**
     * 计算坐标
     */
    private fun calculateCoordinate(bitmap: Bitmap) {
        //图像
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height
        //计算单元格尺寸
        val cellX = width / imageWidth
        val cellY = height / imageHeight
        cellSize = if (cellX < cellY) {
            cellX
        } else {
            cellY
        }
        Log.d(
            debug,
            "计算尺寸\n单元格宽:${cellX} 使用视图宽度${width}除以图像宽度${imageWidth}\n单元格高:${cellY} 使用视图宽度${height}除以图像宽度${imageHeight}\n采用${cellSize}作为视图单元格尺寸"
        )
        centreX = width / 2
        centreY = height / 2
        //计算绘制点  取图像中点坐标(图像尺寸*缩放比例/2)
        startX = centreX - imageWidth / 2 * cellSize
        startY = centreY - imageHeight / 2 * cellSize
        endX = startX + imageWidth * cellSize
        endY = startY + imageHeight * cellSize
    }


    /**
     * 绘制坐标
     * @param canvas Canvas
     * @param imageHeight Int
     * @param imageWidth Int
     */
    private fun drawCoordinate(canvas: Canvas) {
        if (drawCoordinate) {
            //如果需要绘制
            val paint = Paint()
            paint.color = Color.RED
            //开启描边
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            //开始绘制
            for (y in startY..endY step cellSize) {
                //Y改变，x不变
                canvas.drawLine(startX.toFloat(), y.toFloat(), endX.toFloat(), y.toFloat(), paint)
            }
            for (x in startX..endX step cellSize) {
                canvas.drawLine(x.toFloat(), startY.toFloat(), x.toFloat(), endY.toFloat(), paint)
            }
//            canvas.drawCircle(centreX.toFloat(), centreY.toFloat(), 30f, paint)
        }
    }

    /**
     * 缩放图像
     * @param bitmap Bitmap
     * @param size Float
     * @return Bitmap
     */
    fun scaleBitmap(bitmap: Bitmap, size: Float = cellSize.toFloat()): Bitmap {
        //创建新的图像背景
        val matrix = Matrix()
        matrix.setScale(size, size)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.getWidth(),
            bitmap.getHeight(),
            matrix,
            true
        )
    }

    /**
     * 绘制主体图像
     */
    private fun drawImage(canvas: Canvas, bitmap: Bitmap) {
        val paint = Paint()
        val temBitmap = scaleBitmap(bitmap)
        canvas.drawBitmap(temBitmap, startX.toFloat(), startY.toFloat(), paint)
        temBitmap.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mainImagePath != null && canvas != null) {
            val bitmap = BitmapFactory.decodeFile(mainImagePath)
            //计算坐标
            calculateCoordinate(bitmap)
            //设置图像
            drawImage(canvas, bitmap)
            //设置坐标系（如果需要）
            drawCoordinate(canvas)
            if (!bitmap!!.isRecycled) {
                bitmap.recycle()
            }
        }
    }


}