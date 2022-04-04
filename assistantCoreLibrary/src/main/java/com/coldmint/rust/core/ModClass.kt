package com.coldmint.rust.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.coldmint.rust.core.tool.FileOperator
import java.io.File
import java.text.SimpleDateFormat

/**
 * 模组类
 *
 * @author Cold Mint
 */
class ModClass(file: File) {
    //清单文件名只能为小写，修复功能会搜索他。
    val INFOFILENAME = "mod-info.txt"
    private val configurationFileName = "build.json"

    companion object {
        /**
         * 是否为模组
         * @param file File 文件
         * @return Boolean 布尔值
         */
        fun isMod(file: File): Boolean {
            return if (file.isDirectory) {
                true
            } else {
                val type = FileOperator.getFileType(file)
                type == "rwmod" || type == "zip"
            }
        }
    }

    /**
     * 获取模组文件
     *
     * @return 模组文件对象
     */
    val modFile: File = file
    private var mInfoFile: SourceFile? = null

    /**
     * 获取模组名称
     *
     * @return 模组名称
     */
    lateinit var modName: String
    private var upDateTime: Long = 0

    /**
     * 是否含有信息文件(mod-info.txt)
     *
     * @return 信息文件状态
     */
    fun hasInfo(): Boolean {
        return mInfoFile!!.file.exists() ?: false
    }

    /**
     * 创建媒体屏蔽文件(.nomedia)
     *
     * @param context 上下文环境
     */
    fun createNomeidaFile(context: Context?): Boolean {
        if (modFile.isDirectory) {
            val file = File(modFile.absolutePath + "/.nomedia")
            if (!file.exists()) {
                return FileOperator.writeFile(file, "")
            }
        }
        return false
    }

    /**
     * 读取清单文件(mod-info.txt)的值
     *
     * @param key 键
     * @return 键对应的值, 不包含键返回null
     */
    fun readValueFromInfo(key: String): String? {
        val thisInfo = mInfoFile
        return thisInfo?.readValue(key)
    }

    /**
     * 获取模组配置管理器
     * 模组包返回null
     * 模组文件夹返回配置文件对象
     *
     * @return
     */
    val modConfigurationManager: ModConfigurationManager?
        get() = if (modFile.isDirectory) {
            val configurationFilePath =
                File(modFile.absolutePath + "/" + configurationFileName)
            ModConfigurationManager(configurationFilePath)
        } else {
            null
        }

    /**
     * 在源文件里写值
     *
     * @param key   键
     * @param value 值
     */
    fun writeValueToInfo(key: String, value: String, section: String) {
        mInfoFile!!.writeValueOrAddKey(key, value, section)
    }

    /**
     * 读取清单文件(mod-info.txt)的值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 键对应的值
     */
    fun readValueFromInfo(key: String, defaultValue: String): String {
        val result = readValueFromInfo(key)
        return result ?: defaultValue
    }

    /**
     * 在信息节内读取值
     *
     * @param key     键
     * @param section 节
     * @return 失败返回null，成功返回值
     */
    fun readValueFromInfoSection(key: String, section: String): String? {
        return mInfoFile?.readValueFromSection(key, section)
    }

    /**
     * 读取在清单文件(mod-info.txt)内读取资源
     *
     * @param key 键
     * @return 值所指向的文件对象 文件不存在或者键不存在 返回null
     */
    fun readResourceFromInfo(key: String): File? {
        val file_name = readValueFromInfo(key)
        return if (file_name != null) {
            val target = File(modFile.absolutePath + "/" + file_name)
            if (target.exists()) {
                target
            } else {
                null
            }
        } else {
            null
        }
    }

    /**
     * 获取模组图标，若图标不存在返回null
     *
     * @return 位图对象
     */
    val modIcon: Bitmap?
        get() {
            val icon_file = readResourceFromInfo("thumbnail")
            return if (icon_file != null && icon_file.exists()) {
                BitmapFactory.decodeFile(readResourceFromInfo("thumbnail")!!.absolutePath)
            } else {
                null
            }
        }

    /**
     * 获取模组最后修改时间
     *
     * @return 修改时间字符串
     */
    val lastModificationTime: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return formatter.format(upDateTime)
        }

    /**
     * 获取清单文件
     *
     * @return 清单文件对象
     */
    val infoFile: File
        get() = mInfoFile!!.file

    /**
     * 获取模组信息（读取清单文件信息）,读取失败返回null
     *
     * @return 清单文件的内容
     */
    val info: String
        get() = mInfoFile!!.text

    /**
     * 模组类的构造方法
     *
     * @param file 模组文件
     */
    init {
        if (file.exists()) {
            if (file.isDirectory) {
                val infoFile = File(file.absolutePath + "/" + INFOFILENAME)
                mInfoFile = SourceFile(infoFile, this)
                mInfoFile?.removeIllegalCharacters()
                modName = if (infoFile.exists()) {
                    readValueFromInfo("title", file.name)
                } else {
                    file.name
                }
            } else {
                modName = file.name
            }
            upDateTime = file.lastModified()
        }
    }
}