package com.coldmint.rust.pro.viewmodel

import android.os.Environment
import android.util.Log
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.pro.base.BaseViewModel
import com.coldmint.rust.pro.tool.AppSettings
import java.io.File

/**
 * 模组ViewModel
 */
class ModViewModel : BaseViewModel() {

    /**
     * 加载模组
     */
    fun loadMod(): ArrayList<ModClass>? {
        val key = "加载模组列表"
        val modFolder = File(
            AppSettings.getValue(
                AppSettings.Setting.ModFolder,
                Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units/"
            )
        )
        if (modFolder.exists()) {
            if (!modFolder.isDirectory) {
                Log.e(key, "模组文件夹${modFolder}，不是文件夹。")
                return null
            }
            val fileList = modFolder.listFiles()
            if (fileList.isNullOrEmpty()) {
                Log.e(key, "模组文件夹${modFolder}，为空。")
                return null
            }

            val arrayList = ArrayList<ModClass>()
            fileList.forEach {
                val isMod = ModClass.isMod(it)
                if (isMod) {
                    arrayList.add(ModClass(it))
                    Log.d(key, "已添加${it.absolutePath}。")
                } else {
                    Log.e(key, "${it.absolutePath} 不是模组。")
                }
            }
            return if (arrayList.isEmpty()){
                return null
            }else{
                arrayList
            }
        } else {
            Log.e(key, "模组文件夹${modFolder}不存在。")
            return null
        }
    }

}