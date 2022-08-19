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
import android.view.View
import com.coldmint.rust.core.R

/**
 * 炮塔视图
 * @constructor
 */
class TurretView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    private val debugKey = "炮塔视图"
    private lateinit var turretData: TurretData
    private var bitmapW: Int = 0
    private var bitmapH: Int = 0


    /**
     * 设置炮塔数据
     * @param turretData TurretData
     */
    fun setTurretData(turretData: TurretData) {
        this.turretData = turretData
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
     * 设置炮塔X
     * @param x Int
     */
    fun setTurretX(x: Int) {
        if (this::turretData.isInitialized) {
            turretData.x = x
        }
    }

    /**
     * 设置炮塔X
     * @param y Int
     */
    fun setTurretY(y: Int) {
        if (this::turretData.isInitialized) {
            turretData.y = y
        }
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (this::turretData.isInitialized) {
            val paint = Paint()
            val bitmap = if (turretData.imageFile == null) {
                BitmapFactory.decodeResource(this.resources, R.drawable.image)
            } else {
                BitmapFactory.decodeFile(turretData.imageFile!!.absolutePath)
            }
            bitmapW = bitmap.width
            bitmapH = bitmap.height
            canvas?.drawBitmap(
                bitmap,
                (turretData.x - bitmapW / 2).toFloat(),
                (turretData.y - bitmapH / 2).toFloat(),
                paint
            )
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        } else {
            Log.e(debugKey, "未设置炮塔数据，停止绘制。")
        }
    }
}