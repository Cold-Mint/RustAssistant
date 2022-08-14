package com.coldmint.rust.core.dataBean.template

import com.coldmint.rust.core.tool.FileOperator
import org.json.JSONObject
import java.io.File

/**
 * 本地模板文件
 * @property file File
 * @constructor
 */
class LocalTemplateFile(val file: File) : Template {


    private val jsonObject: JSONObject by lazy {
        JSONObject(FileOperator.readFile(file) ?: "")
    }

    override fun getJson(): JSONObject {
        return jsonObject
    }

    override fun getName(language: String): String {
        return if (jsonObject.has("name_$language")) {
            return jsonObject.getString("name_$language")
        } else {
            return jsonObject.getString("name")
        }
    }

    override fun getIcon(): Any? {
        return null
    }

    override fun isLocal(): Boolean {
        return true
    }

    override fun getLink(): String {
        return file.absolutePath
    }
}