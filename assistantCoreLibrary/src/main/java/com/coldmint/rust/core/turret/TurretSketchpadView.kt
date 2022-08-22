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


    companion object {
        /**
         * 缩放图像
         * @param bitmap Bitmap
         * @param size Float
         * @return Bitmap
         */
        fun scaleBitmap(bitmap: Bitmap, size: Float): Bitmap {
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
    }


    private val debug = "炮塔画板"
    private var mainImagePath: String? = null

    //计算中心点
    private var centreX = -1
    private var centreY = -1

    //计算绘制点  取图像中点坐标(图像尺寸*缩放比例/2)
    private var startX = 0
    private var startY = 0
    private var endX = 0
    private var endY = 0

    //单元格宽度
    private var cellSize = 10

    //关键坐标(用于绘制关键线)
    private var keyCoordinate: CoordinateData? = null


    /**
     * 是否建议显示坐标系
     * 如果单元格宽度大于10那么显示
     * @return Boolean
     */
    fun isSuggestedDisplayCoordinateSystem(): Boolean {
        return cellSize > 10
    }

    //是否绘制关键线
    var drawAuxiliaryLine = true
        set(value) {
            field = value
            invalidate()
        }

    //设置是否绘制坐标
    var drawCoordinate = true
        set(value) {
            field = value
            invalidate()
        }


    //设置坐标颜色
    var coordinateColor = Color.RED
        set(value) {
            field = value
            invalidate()
        }

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
            "转换游戏坐标，安卓坐标${androidCoordinateData} 游戏坐标${game}"
        )
        return game
    }

    /**
     * 转换为安卓坐标
     * @param gameCoordinateData CoordinateData
     * @return CoordinateData
     */
    fun toAndroidCoordinate(gameCoordinateData: CoordinateData): CoordinateData {
        val x = (gameCoordinateData.x * cellSize) + centreX
        val y = (gameCoordinateData.y * cellSize) + centreY
        val androidCoordinateData = CoordinateData(x, y)
        Log.d(
            debug,
            "转换安卓坐标，中心点${centreX} ${centreY}安卓坐标${androidCoordinateData} 游戏坐标${gameCoordinateData}"
        )
        return androidCoordinateData
    }

    /**
     * 设置关键坐标
     * @param coordinateData CoordinateData
     */
    fun setKeyCoordinate(coordinateData: CoordinateData) {
        if (centreX != -1 && centreY != -1) {
            keyCoordinate = coordinateData
            Log.d(debug, "已设置关键坐标${coordinateData}。")
            invalidate()
        } else {
            Log.e(debug, "设置关键坐标失败。")
        }

    }


    /**
     * 绘制辅助线
     * @param androidCoordinateData CoordinateData
     */
    private fun drawAuxiliaryLines(canvas: Canvas, androidCoordinateData: CoordinateData) {
        if (!drawAuxiliaryLine) {
            return
        }
        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        //横线
        canvas.drawLine(
            startX.toFloat(),
            androidCoordinateData.y.toFloat(),
            endX.toFloat(),
            androidCoordinateData.y.toFloat(),
            paint
        )

        //竖线
        canvas.drawLine(
            androidCoordinateData.x.toFloat(),
            startY.toFloat(),
            androidCoordinateData.x.toFloat(),
            endY.toFloat(),
            paint
        )
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
            paint.color = coordinateColor
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
     * 绘制主体图像
     */
    private fun drawImage(canvas: Canvas, bitmap: Bitmap) {
        val paint = Paint()
        val temBitmap = scaleBitmap(bitmap, cellSize.toFloat())
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
            //标记关键坐标
            if (keyCoordinate != null) {
                drawAuxiliaryLines(canvas, keyCoordinate!!)
            }
            if (!bitmap!!.isRecycled) {
                bitmap.recycle()
            }
        }
    }


}