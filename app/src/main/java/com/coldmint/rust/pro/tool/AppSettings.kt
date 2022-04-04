package com.coldmint.rust.pro.tool

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.coldmint.rust.pro.R
import kotlin.Throws
import android.os.Environment
import android.util.Log
import java.lang.NullPointerException
import java.util.*

/**
 * 程序设置类
 * Program setup class
 */
class AppSettings private constructor(val mContext: Context) {


    companion object {
        private var instance: AppSettings? = null
        fun getInstance(context: Context): AppSettings {
            if (instance == null) {
                synchronized(AppSettings::class.java)
                {
                    if (instance == null) {
                        instance = AppSettings(context.applicationContext)
                    }
                }
            }
            return instance!!
        }

        @JvmField
        val dataRootDirectory =
            Environment.getExternalStorageDirectory().absolutePath + "/rustAssistant"
        val Locale_Russia = Locale("RU", "ru", "")
    }

    private val mFileName: String = mContext.packageName + "_preferences"
    private val sharedPreferences: SharedPreferences =
        mContext.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    enum class Setting {
        DatabaseDirectory, DatabasePath, TemplateDirectory, AppLanguage, DeveloperMode, CustomSymbol, AutoCreateNomedia, OnlyLoadConantLanguageTemple, NightMode, GamePackage, KeepRwmodFile, EnableRecoveryStation, RecoveryStationFileSaveDays, RecoveryStationFolder, IndependentFolder, SetGameStorage, PackDirectory, IdentifiersPromptNumber, UserName, UseJetBrainsMonoFont, AppID, Account, PassWord, ExpirationTime, CheckBetaUpdate, UpdateData, ShareTip, AgreePolicy, EnglishEditingMode, NightModeFollowSystem, UseMobileNetwork, MapFolder, ModFolder, UseTheCommunityAsTheLaunchPage, AutoSave,ServerAddress
    }


    private val map: HashMap<Setting, String> = HashMap<Setting, String>()

    /**
     * 是否为开发者模式设置
     * Whether to set to developer mode
     *
     * @param setting 设置类型 Set the type
     * @return 是否为开发者模式设置 Whether to set to developer mode
     */
    fun isDevelopersModeSetting(setting: Setting?): Boolean {
        val Name = map[Setting.DeveloperMode] as String?
        if (sharedPreferences.contains(Name)) {
            val developeer_mode = sharedPreferences.getBoolean(Name, false)
            if (!developeer_mode) {
                return when (setting) {
                    Setting.DatabaseDirectory, Setting.TemplateDirectory -> true
                    else -> false
                }
            }
        }
        return false
    }

    /**
     * 初始化设置，仅第一次调用有效。
     * Initialization Settings, valid only for the first call.
     *
     * @param setting 设置信息 Set up information
     * @param value   初始值 initial value
     * @return 初始化状态 Initialization state
     */
    fun <T> initSetting(setting: Setting?, value: T): Boolean {
        val Name = map[setting] as String?
        if (!sharedPreferences.contains(Name)) {
            if (value is String) {
                editor.putString(Name, value as String)
            } else if (value is Boolean) {
                editor.putBoolean(Name, value as Boolean)
            } else if (value is Int) {
                editor.putInt(Name, value as Int)
            } else if (value is Float) {
                editor.putFloat(Name, value as Float)
            } else if (value is Long) {
                editor.putLong(Name, value as Long)
            } else {
                return false
            }
            return editor.commit()
        }
        return false
    }

    /**
     * 设置语言
     *
     * @param language 语言
     */
    fun setLanguage(language: String) {
        val locale = toLocaleValue(language)
        val resources = mContext.resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        // 应用用户选择语言
        config.locale = locale
        resources.updateConfiguration(config, dm)
        Locale.setDefault(locale)
    }


    /**
     * 转换字符串语言
     * 将字符串转换为Locale的枚举类型，未知类型转换为英文。
     * 字符串可为以下值：
     * zh简体中文
     * zh_TW繁体中文
     * ja日语
     * 其他 英文
     *
     * @param language 语言（字符串）
     * @return Locale的枚举对象
     */
    private fun toLocaleValue(language: String): Locale {
        return when (language) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "zh_TW" -> Locale.TRADITIONAL_CHINESE
            "ja" -> Locale.JAPANESE
            "ru" -> Locale_Russia
            else -> Locale.ENGLISH
        }
    }

    /**
     * 设置字符串值
     *
     * @param setting 设置类型
     * @param value   值
     * @return 设置状态
     */
    fun <T> setValue(setting: Setting?, value: T): Boolean {
        val name = map[setting] as String?
        return if (sharedPreferences.contains(name)) {
            when (value) {
                is String -> {
                    editor.putString(name, value as String)
                }
                is Boolean -> {
                    editor.putBoolean(name, value as Boolean)
                }
                is Int -> {
                    editor.putInt(name, value as Int)
                }
                is Float -> {
                    editor.putFloat(name, value as Float)
                }
                is Long -> {
                    editor.putLong(name, value as Long)
                }
                else -> {
                    return false
                }
            }
            editor.commit()
        } else {
            false
        }
    }

    /**
     * 强制写入设置
     * 此方法不考虑是否初始化设置。
     *
     * @param setting 设置
     * @param value   值
     * @return 是否成功
     */
    fun <T> forceSetValue(setting: Setting?, value: T): Boolean {
        val name = map[setting]
        when (value) {
            is String -> {
                editor.putString(name, value as String)
            }
            is Boolean -> {
                editor.putBoolean(name, value as Boolean)
            }
            is Int -> {
                editor.putInt(name, value as Int)
            }
            is Float -> {
                editor.putFloat(name, value as Float)
            }
            is Long -> {
                editor.putLong(name, value as Long)
            }
            else -> {
                return false
            }
        }
        return editor.commit()
    }

    /**
     * 读取字符串值
     * 若读取失败返回默认值
     *
     * @param setting      设置类型
     * @param defaultValue 默认值
     * @return 字符串值
     * @throws NullPointerException 抛出空指针异常
     */
    @Throws(NullPointerException::class)
    fun <T> getValue(setting: Setting, defaultValue: T): T {
        if (isDevelopersModeSetting(setting)) {
            return defaultValue
        }
        if (defaultValue == null) {
            throw NullPointerException()
        }
        val Name = map[setting]
        val resultValue: T = if (defaultValue is String) {
            sharedPreferences.getString(Name, defaultValue as String) as T
        } else if (defaultValue is Boolean) {
            java.lang.Boolean.valueOf(
                sharedPreferences.getBoolean(
                    Name,
                    defaultValue as Boolean
                )
            ) as T
        } else if (defaultValue is Int) {
            Integer.valueOf(sharedPreferences.getInt(Name, defaultValue as Int)) as T
        } else if (defaultValue is Float) {
            java.lang.Float.valueOf(sharedPreferences.getFloat(Name, defaultValue as Float)) as T
        } else if (defaultValue is Long) {
            java.lang.Long.valueOf(sharedPreferences.getLong(Name, defaultValue as Long)) as T
        } else {
            defaultValue
        }
        return resultValue
    }


    /**
     * 构造AppSettings类
     * Construct the AppSettings class
     */
    init {
        map[Setting.DatabasePath] = mContext.getString(R.string.setting_database_path)
        map[Setting.AppLanguage] =
            mContext.getString(R.string.setting_app_language)
        map[Setting.DatabaseDirectory] = mContext.getString(R.string.setting_database_directory)
        map[Setting.DeveloperMode] = mContext.getString(R.string.setting_developer_mode)
        map[Setting.CustomSymbol] =
            mContext.getString(R.string.setting_custom_symbol)
        map[Setting.TemplateDirectory] =
            mContext.getString(R.string.setting_template_directory)
        map[Setting.AutoCreateNomedia] =
            mContext.getString(R.string.setting_auto_create_nomedia)
        map[Setting.OnlyLoadConantLanguageTemple] =
            mContext.getString(R.string.setting_only_load_conant_language_temple)
        map[Setting.NightMode] = mContext.getString(R.string.setting_night_mode)
        map[Setting.GamePackage] = mContext.getString(R.string.setting_game_package)
        map[Setting.KeepRwmodFile] = mContext.getString(R.string.setting_keep_rwmod_file)
        map[Setting.EnableRecoveryStation] =
            mContext.getString(R.string.setting_enable_recovery_station)
        map[Setting.RecoveryStationFileSaveDays] =
            mContext.getString(R.string.setting_recovery_station_file_save_days)
        map[Setting.RecoveryStationFolder] =
            mContext.getString(R.string.setting_recovery_station_folder)
        map[Setting.IndependentFolder] = mContext.getString(R.string.setting_independent_folder)
        map[Setting.PackDirectory] =
            mContext.getString(R.string.setting_pack_directory)
        map[Setting.IdentifiersPromptNumber] =
            mContext.getString(R.string.setting_identifiers_prompt_number)
        map[Setting.UserName] =
            mContext.getString(R.string.setting_user_name)
        map[Setting.UseJetBrainsMonoFont] =
            mContext.getString(R.string.setting_use_jetBrains_mono_font)
        map[Setting.CheckBetaUpdate] = mContext.getString(R.string.setting_check_beta_update)
        map[Setting.EnglishEditingMode] = mContext.getString(R.string.setting_english_editing_mode)
        map[Setting.NightModeFollowSystem] =
            mContext.getString(R.string.setting_night_mode_follow_system)
        map[Setting.UseMobileNetwork] = mContext.getString(R.string.setting_use_mobile_network)
        map[Setting.MapFolder] = mContext.getString(R.string.setting_map_folder)
        map[Setting.ModFolder] = mContext.getString(R.string.setting_mod_folder)
        map[Setting.UseTheCommunityAsTheLaunchPage] =
            mContext.getString(R.string.setting_use_the_community_as_the_launch_page)
        map[Setting.AutoSave] = mContext.getString(R.string.setting_auto_save)
        map[Setting.ServerAddress] = mContext.getString(R.string.setting_server_address)
        //仅保存不可显示
        map[Setting.SetGameStorage] = "SetGameStorage"
        map[Setting.AppID] = "AppId"
        map[Setting.Account] = "Account"
        map[Setting.PassWord] = "PassWord"
        map[Setting.ExpirationTime] = "ExpirationTime"
        map[Setting.UpdateData] = "UpdateData"
        map[Setting.ShareTip] = "ShareTip"
        map[Setting.AgreePolicy] = "AgreePolicy"
    }
}