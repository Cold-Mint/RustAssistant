package com.coldmint.rust.pro.viewmodel

import android.util.Log
import com.coldmint.rust.core.dataBean.template.WebTemplateData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.base.BaseViewModel
import com.coldmint.rust.pro.databean.CodeData
import com.coldmint.rust.pro.tool.AppSettings
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 模板制作器ViewModel
 */
class TemplateMakerViewModel : BaseViewModel() {


    private var name: String? = null

    /**
     * json数据
     */
    private var json: JSONObject? = null


    private var path: String? = null

    /**
     * 是否为本地模板
     */
    private var isLocal = true


    /**
     * 设置是否为本地模板
     * @param local Boolean
     */
    fun isLocal(local: Boolean) {
        this.isLocal = local
    }


    /**
     * 设置名称
     * @param name String
     */
    fun setName(name: String) {
        this.name = name
    }


    fun getName(): String {
        return name!!
    }

    /**
     * 如果是本地模板设置模板路径(json格式推断为模板，其他格式推断为源代码)，远程模板提供模板id
     * @param path String
     */
    fun setPath(path: String) {
        this.path = path
    }

    /**
     * 获取Json数据
     * @return JSONObject
     */
    fun getJsonData(): JSONObject {
        return json!!
    }


    /**
     * 获取代码数据
     */
    fun getCodeData(func: (JSONArray?, MutableList<CodeData>) -> Unit) {
        val key = "获取代码数据"
        if (isLocal) {
            //如果是本地，那么加载文件
            val file = File(path)
            if (file.exists()) {
                val data = FileOperator.readFile(file)
                if (data == null) {
                    Log.e(key, "无法读取文件 ${file.absolutePath}。")
                    return
                }
                val type = FileOperator.getFileType(file)
                if (type == "json") {
                    json = JSONObject(data)
                    val code = json!!.getString("data")
                    val jsonArray = json!!.getJSONArray("action")
                    parsingSourceCode(code, jsonArray, func)
                    Log.d(key, "已读取 ${file.absolutePath} 为本地模板文件。")
                } else {
                    json = JSONObject()
                    json!!.put("data", data)
                    json!!.put(
                        "language",
                        AppSettings.getValue(AppSettings.Setting.AppLanguage, "ALL")
                    )

                    parsingSourceCode(data, null, func)
                    Log.d(key, "已读取 ${file.absolutePath} 为源文件。")
                }
            } else {
                Log.e(key, "目标文件不存在 ${file.absolutePath}")
            }
        } else {
            TemplatePhp.instance.getTemplate(path ?: "", object : ApiCallBack<WebTemplateData> {
                override fun onResponse(t: WebTemplateData) {
                    if (t.code == ServerConfiguration.Success_Code) {
                        json = JSONObject(t.data.content)
                        val code = json!!.getString("data")
                        val jsonArray = json!!.getJSONArray("action")
                        parsingSourceCode(code, jsonArray, func)
                        Log.d(key, "已加载远程模板 ${path} 。")
                    } else {
                        Log.e(key, "远程模板响应: ${t.message}")
                    }
                }

                override fun onFailure(e: Exception) {
                    Log.e(key, "远程模板不存在 ${path}")
                }

            })
        }
    }

    /**
     * 解析源代码
     * @param data String 代码
     * @param func ArrayList<CodeData> 解析结果
     */
    private fun parsingSourceCode(
        data: String,
        jsonArray: JSONArray?,
        func: (JSONArray?, MutableList<CodeData>) -> Unit
    ) {
        val lineParser = LineParser(data)
        var section = ""
        val codeData: MutableList<CodeData> = ArrayList()
        lineParser.analyse { lineNum, lineData, isEnd ->
            if (lineData.startsWith("[") && lineData.endsWith("]")) {
                section = lineData.substring(1, lineData.length - 1)
            }
            codeData.add(CodeData(lineData, section))
            true
        }
        func.invoke(jsonArray, codeData)
    }

}