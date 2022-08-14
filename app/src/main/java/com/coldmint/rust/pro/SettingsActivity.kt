package com.coldmint.rust.pro

import com.coldmint.rust.pro.base.BaseActivity
import android.os.Bundle
import com.coldmint.rust.pro.tool.AppSettings
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.bumptech.glide.Glide
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.databinding.ActivitySettingsBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.color.DynamicColors
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val manager = preferenceManager
            val listPreference =
                manager.findPreference<Preference>(getString(R.string.setting_app_language)) as ListPreference?
            listPreference!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    val oldLanguage =
                        AppSettings.getValue(
                            AppSettings.Setting.AppLanguage,
                            Locale.getDefault().language
                        )
                    val newLanguage = newValue.toString()
                    if (oldLanguage != newLanguage) {
                        val restart = AppSettings.setLanguage(newValue.toString())
                        if (restart) {
                            requireActivity().recreate()
                        }
                    }
                    true
                }

            val english_editing_mode =
                manager.findPreference<SwitchPreference>(requireContext().getString(R.string.setting_english_editing_mode))

            val dynamicColor =
                manager.findPreference<SwitchPreference>(requireContext().getString(R.string.setting_dynamic_color))
            if (!DynamicColors.isDynamicColorAvailable()) {
                //动态颜色不可用
                dynamicColor?.summary = getString(R.string.dynamic_color_disabled)
                dynamicColor?.isEnabled = false
            }

            val errorInfo =
                manager.findPreference<PreferenceScreen>(requireContext().getString(R.string.setting_see_error_info))
            errorInfo!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, ErrorInfoActivity::class.java))
                true
            }

            val game = manager.findPreference<PreferenceScreen>("set_game_pack")
            game!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(context, ApplicationListActivity::class.java))
                true
            }
            val valueTypeManager =
                manager.findPreference<PreferenceScreen>("value_type_manager")
            valueTypeManager!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent(context, ValueTypeActivity::class.java)
                    startActivity(intent)
                    true
                }
            val openRecoveryStation =
                manager.findPreference<PreferenceScreen>("open_recovery_station")
            openRecoveryStation!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    startActivity(Intent(context, RecyclingStationActivity::class.java))
                    true
                }

            val clearCache =
                manager.findPreference<PreferenceScreen>(getString(R.string.setting_clear_cache))
            clearCache!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val listData = listOf<String>(
                        getString(R.string.history_cache),
                        getString(R.string.code_cache),
                        getString(R.string.glide_cache)
                    )
                    MaterialDialog(requireContext()).show {
                        title(R.string.clear_cache).listItemsMultiChoice(items = listData)
                        { dialog, indices, items ->
                            val handler = Handler(Looper.getMainLooper())
                            thread {
                                if (items.isNotEmpty()) {
                                    for (item in items) {
                                        when (item) {
                                            getString(R.string.glide_cache) -> {
                                                Glide.get(requireContext()).clearDiskCache()
                                                handler.post {
                                                    Glide.get(requireContext()).clearMemory()
                                                }
                                            }
                                            getString(R.string.code_cache) -> {
                                                FileOperator.delete_files(requireContext().codeCacheDir)
                                                FileOperator.delete_files(requireContext().cacheDir)
                                            }
                                            getString(R.string.history_cache) -> {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                    FileOperator.delete_files(File(requireActivity().applicationContext.dataDir.absolutePath + "/databases"))
                                                } else {
                                                    FileOperator.delete_files(
                                                        File(
                                                            FileOperator.getSuperDirectory(
                                                                requireContext().cacheDir
                                                            ) + "/databases"
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                    }
                                    handler.post {
                                        Toast.makeText(
                                            requireContext(),
                                            R.string.clear_cache_complete,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }.positiveButton(R.string.dialog_ok).negativeButton(R.string.dialog_cancel)
                    }
                    true
                }

            val nightMode: SwitchPreference? =
                manager.findPreference<SwitchPreference>(getString(R.string.setting_night_mode))
            nightMode?.setOnPreferenceChangeListener { preference, newValue ->
                    val booleanValue = newValue as Boolean
                    if (booleanValue) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                true
            }


            val nightModeFollowSystem: SwitchPreference? =
                manager.findPreference<SwitchPreference>(getString(R.string.setting_night_mode_follow_system))
            nightModeFollowSystem?.setOnPreferenceChangeListener { preference, newValue ->
                if (nightMode != null) {
                    val useValue = newValue as Boolean
                    nightMode.isEnabled = !useValue
                    return@setOnPreferenceChangeListener true
                } else {
                    return@setOnPreferenceChangeListener false
                }
            }
            if (nightMode != null && nightModeFollowSystem != null) {
                nightMode.isEnabled = !nightModeFollowSystem.isChecked
            }
            if (!GlobalMethod.isActive) {
                val editGroup = manager.findPreference<PreferenceCategory>("editGroup")
                if (editGroup != null) {
                    editGroup.isVisible = false
                }
                val gamePackGroup = manager.findPreference<PreferenceCategory>("gamePackGroup")
                if (gamePackGroup != null) {
                    gamePackGroup.isVisible = false
                }

                val modGroup = manager.findPreference<PreferenceCategory>("modGroup")
                if (modGroup != null) {
                    modGroup.isVisible = false
                }

                val templateGroup = manager.findPreference<PreferenceCategory>("templateGroup")
                if (templateGroup != null) {
                    templateGroup.isVisible = false
                }
                val developerModeGroup =
                    manager.findPreference<PreferenceCategory>("developerModeGroup")
                if (developerModeGroup != null) {
                    developerModeGroup.isVisible = false
                }

                val useCommunity =
                    manager.findPreference<SwitchPreference>(getString(R.string.setting_use_the_community_as_the_launch_page))
                if (useCommunity != null) {
                    useCommunity.isVisible = false
                }

                val mapGroup = manager.findPreference<PreferenceCategory>("mapGroup")
                if (mapGroup != null) {
                    mapGroup.isVisible = false
                }
            }

        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getString(R.string.set_up)
            setReturnButton()
            val settingsFragment = SettingsFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit()
        }
    }

}