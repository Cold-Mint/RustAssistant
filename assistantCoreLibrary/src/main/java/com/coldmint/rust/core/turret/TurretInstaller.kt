package com.coldmint.rust.core.turret

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.Drawable.createFromPath
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.coldmint.rust.core.R
import com.coldmint.rust.core.SourceFile

/**
 * 炮塔安装器
 * 此类用于安装炮塔，将炮塔安放到视图上
 */
object TurretInstaller {

    /**
     * 安装全部炮塔到视图
     * @param frameLayout FrameLayout
     * @param sourceFile SourceFile
     */
    fun installerAllTurret(
        viewGroup: ViewGroup,
        sourceFile: SourceFile,
        coordinateChangeListener: (CoordinateData) -> Unit
    ) {
        sourceFile.getTurretManager().turretList.forEach {
            installerTurret(viewGroup, it,coordinateChangeListener)
        }
    }

    /**
     * 安装单个炮塔到视图
     * @param frameLayout FrameLayout
     * @param sourceFile SourceFile
     */
    fun installerTurret(viewGroup: ViewGroup, turretData: TurretData,        coordinateChangeListener: (CoordinateData) -> Unit
    ) {
        val turretView = TurretView(viewGroup.context)
        turretView.setTurretData(turretData)
        turretView.setOnTouchListener { view, motionEvent ->
            turretView.setTurretX(motionEvent.x.toInt())
            turretView.setTurretY(motionEvent.y.toInt())
            coordinateChangeListener.invoke(CoordinateData(motionEvent.x.toInt(),motionEvent.y.toInt()))
            turretView.invalidate()
            true
        }
        viewGroup.addView(turretView)
    }

}