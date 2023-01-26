package com.coldmint.rust.core

import com.google.gson.Gson
import com.coldmint.rust.core.dataBean.ModConfigurationData
import com.coldmint.rust.core.tool.FileOperator
import java.io.File

/**
 * 模组配置管理器
 * @author Cold Mint
 * @date 2021/10/20 19:18
 */
class ModConfigurationManager(file: File) {
    private val gson: Gson = Gson()

    /**
     * 获取配置文件对象
     *
     * @return 配置文件对象
     */
    val file: File = file

    /**
     * 从文件内构建配置对象
     *
     * @return 成功返回模组配置对象，失败返回null
     */
    fun readData(): ModConfigurationData? {
        if (!file.exists()) {
            return null
        }
        val code = FileOperator.readFile(file)
        return if (code == null) {
            null
        } else {
            try {
                gson.fromJson(code, ModConfigurationData::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 保存配置对象到某个文件
     *
     * @param modConfigurationData 配置对象
     * @return 是否保存成功
     */
    fun saveData(modConfigurationData: ModConfigurationData?): Boolean {
        val data = gson.toJson(modConfigurationData, ModConfigurationData::class.java)
        return FileOperator.writeFile(file, data)
    }

    companion object {
        var webLinkType = "webLink"
        var qqGroupType = "qqGroup"
    }

}