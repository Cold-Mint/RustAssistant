package com.coldmint.rust.pro

import android.app.Application
import android.content.Context
import android.util.Log
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.coldmint.rust.pro.tool.AppSettings
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class RustApplication : Application() {

//    companion object {
//        val appId = "61853bf9e0f9bb492b4f7eba"
//        val channel = "Umeng"
//    }

    override fun onCreate() {
        super.onCreate()
        //动态颜色
        val options = DynamicColorsOptions.Builder().setPrecondition { activity, theme ->
            AppSettings.getInstance(this)
                .getValue(AppSettings.Setting.DynamicColor, DynamicColors.isDynamicColorAvailable())
        }.build()
        DynamicColors.applyToActivitiesIfAvailable(this, options)
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) //default: true
            .showErrorDetails(true) //default: true
            .showRestartButton(true) //default: true
            .logErrorOnRestart(true) //default: true
            .trackActivities(false) //default: false
            .minTimeBetweenCrashesMs(2000) //default: 3000
            //                .errorDrawable(R.drawable.ic_custom_drawable) //default: bug image
            .restartActivity(MainActivity::class.java) //default: null (your app's launch activity)
            .errorActivity(ErrorActivity::class.java) //default: null (default error activity)
            .apply()
    }


}