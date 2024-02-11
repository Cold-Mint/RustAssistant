package com.coldmint.rust.pro

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.debug.LogCatObserver
import com.muqing.muqing.wj
import com.coldmint.rust.pro.tool.AppSettings
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.hjq.language.MultiLanguages
import com.youth.banner.BuildConfig


class RustApplication : Application() {

    companion object {
        private lateinit var instanceObject: RustApplication

        /**
         * 获取实例对象
         */
        fun getInstance(): RustApplication {
            return instanceObject
        }
    }

    override fun onCreate() {
        super.onCreate()
        wj(this)


        instanceObject = this
        AppSettings.initAppSettings(this)
        //动态颜色
        val options = DynamicColorsOptions.Builder()
            .setPrecondition { _, _ ->
                AppSettings
                    .getValue(
                        AppSettings.Setting.DynamicColor,
                        DynamicColors.isDynamicColorAvailable()
                    )
            }.build()
        DynamicColors.applyToActivitiesIfAvailable(this, options)
        LogCat.attachObserver(object : LogCatObserver {
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

        MultiLanguages.init(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            @SuppressLint("SourceLockedOrientationActivity")
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
//                android:screenOrientation="portrait"
                //全局强制横屏
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(MultiLanguages.attach(base))
    }


}