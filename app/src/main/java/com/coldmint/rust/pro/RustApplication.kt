package com.coldmint.rust.pro

import android.app.Application
import android.content.Context
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class RustApplication : Application() {

//    companion object {
//        val appId = "61853bf9e0f9bb492b4f7eba"
//        val channel = "Umeng"
//    }

    override fun onCreate() {
        super.onCreate()
//        UMConfigure.preInit(this, appId, channel)
//        DynamicColors.applyToActivitiesIfAvailable(this)
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