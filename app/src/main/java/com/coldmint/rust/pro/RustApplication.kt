package com.coldmint.rust.pro

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.debug.LogCatObserver
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.CompletionItemConverter
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.hjq.language.MultiLanguages
import com.youth.banner.BuildConfig
import java.util.*

class RustApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        AppSettings.initAppSettings(this)
        //动态颜色
        val options = DynamicColorsOptions.Builder()
            .setPrecondition { activity, theme ->
                AppSettings
                    .getValue(
                        AppSettings.Setting.DynamicColor,
                        DynamicColors.isDynamicColorAvailable()
                    )
            }.build()
        DynamicColors.applyToActivitiesIfAvailable(this, options)
        LogCat.attachObserver(object : LogCatObserver{
            override fun onReceiveLog(msg: String) {
                Firebase.crashlytics.log(msg)
            }
        })
        LogCat.setEnable(!BuildConfig.DEBUG)
        //程序崩溃
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

        MultiLanguages.init(this);

    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(MultiLanguages.attach(base))
    }


}