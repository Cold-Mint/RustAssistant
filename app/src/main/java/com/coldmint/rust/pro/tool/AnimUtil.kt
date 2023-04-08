package com.coldmint.rust.pro.tool

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils

object AnimUtil {

    /**
     * 播放动画资产文件
     */
    fun doAnim(
        context: Context,
        animRes: Int,
        views: List<View>,
        func: (views: List<View>) -> Unit
    ) {
        val animation = AnimationUtils.loadAnimation(context, animRes)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                views.forEach {
                    it.clearAnimation()
                }
                func.invoke(views)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })
        views.forEach {
            it.startAnimation(animation)
        }
    }
}