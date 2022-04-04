package com.coldmint.rust.core

import com.coldmint.rust.core.dataBean.TemplateDataBean
import com.coldmint.rust.core.dataBean.template.TemplateInfo
import com.coldmint.rust.core.tool.FileOperator
import com.google.gson.Gson
import java.io.File

/**
 * 模板类
 */
class TemplatePackage(val directest: File) {
    /**
     * 获取文件目录
     *
     * @return
     */
    val infoFile: File by lazy {
        File(directest.absolutePath + "/" + INFONAME)
    }

    val gson by lazy {
        Gson()
    }

    /**
     * 是模板包
     *
     * @return
     */
    val isTemplate: Boolean
        get() = if (directest.isDirectory && infoFile.exists()) {
            infoFile.exists()
        } else false


    /**
     * 获取清单信息文件
     * @return TemplateInfo
     */
    fun getInfo(): TemplateInfo? {
        val data = FileOperator.readFile(infoFile) ?: return null
        return gson.fromJson(data, TemplateInfo::class.java)
    }

    /**
     * 获取文件对象
     * 将相对路径转换为文件对象
     * @param name String
     * @return File
     */
    fun getFile(name: String): File {
        return File(directest.absolutePath + name)
    }

    /**
     * 获取模板名称
     * @return String
     */
    fun getName(): String {
        val info = getInfo()
        return info?.name ?: directest.name
    }

    /**
     * 创建清单文件
     * @param templateInfo TemplateInfo 清单信息
     * @param file File? 文件
     * @return Boolean 是否写出成功
     */
    fun create(templateInfo: TemplateInfo, file: File? = null): Boolean {
        if (!directest.exists()) {
            directest.mkdirs()
        }
        val data = gson.toJson(templateInfo)
        val useFile = file ?: infoFile
        return FileOperator.writeFile(useFile, data)
    }

    companion object {
        const val INFONAME = "template_info.txt"

        /**
         * 获取绝对文件名（不包含“_”）
         * @param file File 文件
         * @return String
         */
        fun getAbsoluteFileName(file: File? = null, filename: String? = null): String {
            val name = if (file != null) {
                FileOperator.getPrefixName(file)
            } else filename ?: "default"
            val index = name.lastIndexOf("_")
            return if (index > -1) {
                name.substring(0, index)
            } else {
                name
            }
        }

    }

}