package com.coldmint.rust.core.turret

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.coldmint.rust.core.R
import com.coldmint.rust.core.debug.LogCat

/**
 * 炮塔视图
 * @constructor
 */
class TurretView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    private var debugKey = "炮塔视图"
    private lateinit var turretData: TurretData
    private var bitmapW: Int = 0
    private var bitmapH: Int = 0
    private var turretSketchpadView: TurretSketchpadView? = null
    private var coordinateChangeListener: ((CoordinateData, TurretData) -> Unit)? = null

    //是否可以拖动
    private var canDrag = false

    /**
     * 设置坐标监听器
     * @param coordinateChangeListener Function2<CoordinateData, TurretData, Unit>?
     */
    fun setCoordinateChangeListener(coordinateChangeListener: ((CoordinateData, TurretData) -> Unit)?) {
        this.coordinateChangeListener = coordinateChangeListener
    }

    /**
     * 设置是否可用拖到
     * @param canDrag Boolean
     */
    fun setCanDrag(canDrag: Boolean) {
        LogCat.d(debugKey, "${turretData.name} 可拖动状态${canDrag}")
        this.canDrag = canDrag
        if (canDrag) {
            val and = turretSketchpadView?.toAndroidCoordinate(turretData.gameCoordinateData)
            if (and != null) {
                turretSketchpadView?.setKeyCoordinate(and)
            } else {
                LogCat.e(debugKey, "可拖动状态,辅助线定位失败。")
            }
        }
    }


    /**
     * 设置游戏坐标
     * @param coordinateData CoordinateData
     */
    fun setGameCoordinateData(coordinateData: CoordinateData) {
        val android = turretSketchpadView?.toAndroidCoordinate(coordinateData)
        if (android != null) {
            turretSketchpadView?.setKeyCoordinate(android)
        }
        turretData.gameCoordinateData = coordinateData
        invalidate()
    }

    /**
     * 设置炮塔数据
     * @param turretData TurretData
     */
    fun setTurretData(turretData: TurretData) {
        this.turretData = turretData
        this.debugKey = "炮塔视图_" + turretData.name
    }

    /**
     * 设置画板
     * @param sketchpadView TurretSketchpadView
     */
    fun setTurretSketchpadView(sketchpadView: TurretSketchpadView) {
        this.turretSketchpadView = sketchpadView
    }


    /**
     * 获取炮塔数据（若未初始化返回null）
     * @return TurretData?
     */
    fun getTurretData(): TurretData? {
        if (this::turretData.isInitialized) {
            return turretData
        }
        return null
    }

    /**
     * 设置炮塔坐标数据
     * @param coordinateData
     */
    fun setTurretGameCoordinateData(coordinateData: CoordinateData) {
        if (this::turretData.isInitialized) {
            turretData.gameCoordinateData = coordinateData
        }
    }


    /**
     * 当出现触摸事件
     * @param event MotionEvent
     * @return Boolean 返回true已被处理
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    return canDrag
                }
                MotionEvent.ACTION_MOVE -> {
                    LogCat.d(debugKey, "收到移动${turretData.name} 可拖动状态${canDrag}")
                    if (canDrag) {
                        val and = CoordinateData(event.x.toInt(), event.y.toInt())
                        val gameCoordinateData = turretSketchpadView!!.toGameCoordinate(and)
                        setTurretGameCoordinateData(gameCoordinateData)
                        turretSketchpadView!!.setKeyCoordinate(and)
                        coordinateChangeListener?.invoke(gameCoordinateData, turretData)
                        invalidate()
                        return true
                    }
                }

            }
        }
        return false
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (this::turretData.isInitialized) {
            if (turretSketchpadView == null) {
                LogCat.e(debugKey, "未绑定画板，停止绘制。")
                return
            }
            val paint = Paint()
            var bitmap: Bitmap? = if (turretData.imageFile != null) {
                BitmapFactory.decodeFile(turretData.imageFile!!.absolutePath)
            } else {
                null
            }
            if (bitmap == null) {
                LogCat.e(debugKey, "无法加载炮塔图像。")
                return
            } else {
                if (turretData.scaleValue != 1f) {
                    bitmap = TurretSketchpadView.scaleBitmap(
                        bitmap, turretData.scaleValue
                    )
                }
                val androidCoordinate =
                    turretSketchpadView!!.toAndroidCoordinate(turretData.gameCoordinateData)
                bitmapW = bitmap!!.width
                bitmapH = bitmap!!.height
                canvas?.drawBitmap(
                    bitmap,
                    (androidCoordinate.x - bitmapW / 2).toFloat(),
                    (androidCoordinate.y - bitmapH / 2).toFloat(),
                    paint
                )
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        } else {
            LogCat.e(debugKey, "未设置炮塔数据，停止绘制。")
        }
    }
}