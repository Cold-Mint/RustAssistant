package com.coldmint.rust.pro.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.CompressionManager
import com.coldmint.rust.core.DataSet
import com.coldmint.rust.core.dataBean.LoginRequestData
import com.coldmint.rust.core.dataBean.user.ActivationInfo
import com.coldmint.rust.core.dataBean.user.SocialInfoData
import com.coldmint.rust.core.dataBean.user.UserData
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.UnzipListener
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.User
import com.coldmint.rust.pro.*
import com.coldmint.rust.pro.base.BaseAndroidViewModel
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.BookmarkManager
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.zip.ZipEntry


/**
 * 启动事件初始化视图模型
 * @constructor
 */
class StartViewModel(application: Application) : BaseAndroidViewModel(application) {

    /**
     * 签名错误的LiveData
     */
    val signatureErrorLiveData by lazy {
        MutableLiveData<Boolean>(false)
    }

    /**
     * 用户数据的LiveData
     */
    val userLiveData by lazy {
        MutableLiveData<ActivationInfo>()
    }

    /**
     * 是否需要登录的LiveData
     */
    val needLoginLiveData by lazy {
        MutableLiveData<Boolean>(false)
    }

    /**
     * 表示此应用是否已激活
     */
    val isActivationLiveData by lazy {
        MutableLiveData(false)
    }


    /**
     * 用户信息验证错误LiveData
     */
    val verifyErrorMsgLiveData by lazy {
        MutableLiveData("")
    }

    /**
     * 数据集消息
     */
    val dataSetMsgLiveData by lazy {
        MutableLiveData("")
    }

    private val context: Context by lazy {
        getApplication()
    }

    private val appSettings by lazy {
        AppSettings.getInstance(context)
    }

    /**
     * 初始化全部数据
     */
    fun initAllData() {
        //初始化语言
        initLanguage()
        //加载夜间模式设置
        loadNightMode()
        //检查签名是否错误
        if (checkSignature()) {
            return
        }
        //初始化设置
        initSetting()
        //初始化资源
        initRes()
        //初始化书签
        initBookmark()
        //验证用户信息
        verifyingUserInfo()
    }


    /**
     * 验证用户信息
     */
    fun verifyingUserInfo() {
        val status = appSettings.getValue(AppSettings.Setting.LoginStatus, false)
        if (!status) {
            needLoginLiveData.value = true
            return
        }
        //验证登录
        val token = appSettings.getValue(AppSettings.Setting.Token, "")
        if (token.isBlank()) {
            needLoginLiveData.value = true
        } else {
            User.getUserActivationInfo(token, object : ApiCallBack<ActivationInfo> {
                override fun onFailure(e: Exception) {
                    val localTime = appSettings.getValue(
                        AppSettings.Setting.ExpirationTime,
                        0.toLong()
                    )
                    if (localTime == (-2).toLong()) {
                        isActivationLiveData.value = true
                    } else {
                        val nowTime = System.currentTimeMillis()
                        //本地时间大于当前时间 激活
                        isActivationLiveData.value = localTime > nowTime
                    }
                }


                override fun onResponse(activationInfo: ActivationInfo) {
                    if (activationInfo.code == ServerConfiguration.Success_Code) {
                        userLiveData.value = activationInfo
                        //更新本地激活时间
                        val expirationTime = activationInfo.data.expirationTime
                        val time = ServerConfiguration.toLongTime(expirationTime)
                        appSettings.forceSetValue(
                            AppSettings.Setting.ExpirationTime,
                            time
                        )
                        isActivationLiveData.value = activationInfo.data.activation
                    } else {
//                        用户登录失败
                        verifyErrorMsgLiveData.value = activationInfo.message
                        Log.d("验证失败", activationInfo.message)
                    }
                }
            })
        }
    }


    /**
     * 初始化资源
     */
    private fun initRes() {
        val defaultDatabase = File(
            appSettings.getValue(
                AppSettings.Setting.DatabaseDirectory,
                context.filesDir.absolutePath + "/database/"
            ) + "official"
        )
        appSettings.initSetting(
            AppSettings.Setting.DatabasePath,
            defaultDatabase.absolutePath
        )
        val cacheFile = File(context.cacheDir.toString() + "/System/DataBase.rdb")
        val cacheFolder = File(context.cacheDir.toString() + "/System/official")
        FileOperator.outputResourceFile(context, "dataBase.rdb", cacheFile)
        try {
            val updateDataSet: (File) -> Unit = {
                CompressionManager.instance.unzip(
                    cacheFile,
                    it,
                    object : UnzipListener {
                        override fun whenUnzipFile(zipEntry: ZipEntry, file: File): Boolean {
                            return true
                        }

                        override fun whenUnzipFolder(zipEntry: ZipEntry, folder: File): Boolean {
                            return true
                        }

                        override fun whenUnzipComplete(result: Boolean) {
                            var codeDataBase = CodeDataBase.getInstance(context)

                            codeDataBase.loadDataSet(
                                DataSet(defaultDatabase),
                                CodeDataBase.ReadMode.Copy
                            )
                        }

                    })
            }

            if (defaultDatabase.exists()) {
                updateDataSet.invoke(cacheFolder)
                val oldDataSet = DataSet(defaultDatabase)
                val newDataSet = DataSet(cacheFolder)
                val update = oldDataSet.update(newDataSet)
                if (update) {
                    dataSetMsgLiveData.value = context.getString(R.string.dataset_update_ok)
                }
            } else {
                updateDataSet.invoke(defaultDatabase)
            }
            val defaultValues = File(context.filesDir.absolutePath + "/values.json")
            if (!defaultValues.exists()) {
                FileOperator.outputResourceFile(
                    context,
                    "defaultValueType.json",
                    defaultValues
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        importingTemplatePackage("ling.rp")
        importingTemplatePackage("baseTemplate.rp")
    }


    /**
     * 导入内置的模板包文件
     * @param fileName String
     */
    private fun importingTemplatePackage(fileName: String) {
        val defaultTemplate = File(
            appSettings.getValue(
                AppSettings.Setting.TemplateDirectory,
                context.filesDir.absolutePath + "/template/"
            ).toString() + FileOperator.getPrefixName(fileName)
        )
        if (!defaultTemplate.exists()) {
            val cacheFile = File(context.cacheDir.toString() + "/System/" + fileName)
            if (!cacheFile.exists()) {
                FileOperator.outputResourceFile(context, fileName, cacheFile)
            }
            CompressionManager.instance.unzip(
                cacheFile,
                defaultTemplate,
                object : UnzipListener {
                    override fun whenUnzipFile(zipEntry: ZipEntry, file: File): Boolean {
                        return true
                    }

                    override fun whenUnzipFolder(zipEntry: ZipEntry, folder: File): Boolean {
                        return true
                    }


                    override fun whenUnzipComplete(result: Boolean) {
                        FileOperator.delete_files(cacheFile)
                    }
                })
        }
    }

    //初始化书签
    private fun initBookmark() {
        val bookmarkManager = BookmarkManager(context)
        if (!bookmarkManager.load()) {
            bookmarkManager.addBookmark(
                Environment.getExternalStorageDirectory().absolutePath + "/rustAssistant",
                context.getString(R.string.app_name)
            )
            bookmarkManager.addBookmark(
                Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units",
                context.getString(R.string.unit_directory)
            )
            bookmarkManager.save()
        }
    }


    /**
     * 检查签名是否有错误
     */
    private fun checkSignature(): Boolean {
        val key = "签名检查"
        val sign = AppOperator.getSignature(context)
        if (BuildConfig.DEBUG) {
            //是测试模式
            if (GlobalMethod.DEBUG_SIGN != sign) {
                Log.e(key, "测试打包，签名检查错误" + sign + "不是合法的签名。")
                signatureErrorLiveData.value = true
            } else {
                Log.d(key, "测试打包，签名合法。")
                signatureErrorLiveData.value = false
            }
        } else {
            if (GlobalMethod.RELEASE_SIGN != sign) {
                signatureErrorLiveData.value = true
                Log.e(key, "正式打包，签名检查错误" + sign + "不是合法的签名。")
            } else {
                Log.d(key, "正式打包，签名合法。")
                signatureErrorLiveData.value = false
            }
        }
        return signatureErrorLiveData.value ?: true
    }

    /**
     * 初始化语言设置
     */
    private fun initLanguage() {
        val defaultLanguage = Locale.getDefault().language
        val language = appSettings.getValue(AppSettings.Setting.AppLanguage, defaultLanguage)
        if (language != defaultLanguage) {
            appSettings.setLanguage(language)
        }
    }

    /**
     * 初始化设置
     */
    private fun initSetting() {
        appSettings.initSetting(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
        appSettings.initSetting(AppSettings.Setting.DeveloperMode, false)
        appSettings.initSetting(
            AppSettings.Setting.DatabaseDirectory,
            context.filesDir.absolutePath + "/database/"
        )
        appSettings.initSetting(
            AppSettings.Setting.TemplateDirectory,
            context.filesDir.absolutePath + "/template/"
        )
        appSettings.initSetting(AppSettings.Setting.CustomSymbol, "[],:='*_$%@#{}()")
        appSettings.initSetting(AppSettings.Setting.AutoCreateNomedia, true)
        appSettings.initSetting(AppSettings.Setting.OnlyLoadConantLanguageTemple, true)
        appSettings.initSetting(AppSettings.Setting.NightMode, isNightMode())
        appSettings.initSetting(
            AppSettings.Setting.GamePackage,
            GlobalMethod.DEFAULT_GAME_PACKAGE
        )
        appSettings.initSetting(AppSettings.Setting.KeepRwmodFile, true)
        appSettings.initSetting(AppSettings.Setting.RecoveryStationFileSaveDays, 7)
        appSettings.initSetting(AppSettings.Setting.EnableRecoveryStation, true)
        appSettings.initSetting(AppSettings.Setting.UseMobileNetwork, false)
        appSettings.initSetting(
            AppSettings.Setting.RecoveryStationFolder,
            context.filesDir.absolutePath + "/backup/"
        )
        appSettings.initSetting(
            AppSettings.Setting.PackDirectory,
            AppSettings.dataRootDirectory + "/bin/"
        )
        appSettings.initSetting(AppSettings.Setting.IndependentFolder, true)
        appSettings.initSetting(AppSettings.Setting.IdentifiersPromptNumber, 40)
        appSettings.initSetting(AppSettings.Setting.UseJetBrainsMonoFont, true)
        appSettings.initSetting(AppSettings.Setting.AppID, UUID.randomUUID().toString())
        appSettings.initSetting(AppSettings.Setting.CheckBetaUpdate, false)
        appSettings.initSetting(AppSettings.Setting.SetGameStorage, false)
        appSettings.initSetting(AppSettings.Setting.ShareTip, true)
        appSettings.initSetting(AppSettings.Setting.EnglishEditingMode, false)
        appSettings.initSetting(AppSettings.Setting.NightModeFollowSystem, true)
        appSettings.initSetting(AppSettings.Setting.UseTheCommunityAsTheLaunchPage, true)
        appSettings.initSetting(
            AppSettings.Setting.ServerAddress,
            ServerConfiguration.defaultIp
        )
        ServerConfiguration.website =
            appSettings.getValue(
                AppSettings.Setting.ServerAddress,
                ServerConfiguration.defaultIp
            )
        appSettings.initSetting(
            AppSettings.Setting.MapFolder,
            Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/maps/"
        )
        appSettings.initSetting(
            AppSettings.Setting.ModFolder,
            Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units/"
        )
        appSettings.initSetting(AppSettings.Setting.AutoSave, true)
        appSettings.initSetting(AppSettings.Setting.AgreePolicy, false)
        appSettings.initSetting(AppSettings.Setting.LoginStatus, false)
        //如果启用动态颜色
        appSettings.initSetting(
            AppSettings.Setting.DynamicColor,
            DynamicColors.isDynamicColorAvailable()
        )
    }

    /**
     * 加载夜间设置
     */
    private fun loadNightMode() {
        val followSystem = appSettings.getValue(AppSettings.Setting.NightModeFollowSystem, true)
        if (followSystem) {
            //如果跟随系统(更新本地设置与系统同步)
            appSettings.setValue(AppSettings.Setting.NightMode, isNightMode())
        } else {
            val night = appSettings.getValue(AppSettings.Setting.NightMode, false)
            if (night) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    /**
     * 是否为深色模式
     * @return Boolean
     */
    private fun isNightMode(): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

}