package com.coldmint.rust.pro.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.dataBean.InputParserDataBean
import com.coldmint.rust.core.dataBean.IntroducingDataBean
import com.coldmint.rust.core.dataBean.ListParserDataBean
import com.coldmint.rust.core.dataBean.template.Template
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.interfaces.TemplateParser
import com.coldmint.rust.core.templateParser.InputParser
import com.coldmint.rust.core.templateParser.IntroducingParser
import com.coldmint.rust.core.templateParser.ListParser
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.base.BaseViewModel
import com.coldmint.rust.pro.tool.AppSettings
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TemplateParserViewModel : BaseViewModel() {

    //json数据
    private var jsonData: JSONObject? = null
    private var template: Template? = null
    private lateinit var arrayList: ArrayList<TemplateParser>

    //文件输出目录
    private var outPutPath: String? = null


    //创建目录
    private var createDirectory: String? = null

    /**
     * 设置模板数据
     * @param template Template
     */
    fun setTemplate(template: Template) {
        this.template = template
        this.jsonData = template.getJson()
    }


    /**
     * 设置创建目录
     * @param path String
     */
    fun setCreateDirectory(path: String) {
        createDirectory = path
    }


    /**
     * 获取源代码
     * @return String
     */
    fun getCode(): String {
        try {
            return jsonData?.getString("data") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }


    /**
     * 获取模板名称
     * @param language String
     * @return String
     */
    fun getTemplateName(language: String): String {
        return template?.getName(language) ?: ""
    }


    /**
     * 构建文件
     * @param context Context
     * @param fileName String
     * @return Boolean
     */
    fun buildFile(context: Context, fileName: String): Boolean {
        if (createDirectory == null) {
            LogCat.e("构建文件", "没有设置创建目录。")
            return false
        }
        val index = fileName.lastIndexOf('.')
        val independentFolder = AppSettings.getValue(AppSettings.Setting.IndependentFolder, true)
        val createPath = if (independentFolder) {
            val folderName =
                if (index > -1) {
                    fileName.substring(0 until index)
                } else {
                    fileName
                }
            "$createDirectory/$folderName"
        } else {
            createDirectory
        }
        LogCat.d("构建文件", "是否需要独立创建文件夹${independentFolder} 文件夹目录${createPath}")
        if (independentFolder) {
            val folder = File(createPath)
            if (folder.exists()) {
                LogCat.e("构建文件", "创建目录${createPath}已存在。")
                return false
            }
            folder.mkdirs()
        }
        val path = File(
            createPath + "/" + if (index > -1) {
                fileName
            } else {
                "$fileName.ini"
            }
        )
        if (path.exists()) {
            LogCat.e("构建文件", "目标文件${path}已存在。")
            return false
        }
        outPutPath = path.absolutePath
        return FileOperator.writeFile(path, generatingCode(context))
    }

    /**
     * 获取文件输出目录（当构建文件完毕后，返回有效值。否则返回null）
     * @return String?
     */
    fun getOutputPath(): String? {
        return outPutPath
    }

    /**
     * 生成代码
     * @return String
     */
    private fun generatingCode(context: Context): String {
        val staticCode = getCode()
        //如果为空，那么返回空
        if (staticCode.isBlank()) {
            LogCat.e("获取模板解析器", "静态代码为空。")
            return staticCode
        }

        val sourceFile = SourceFile(staticCode)
        val parserList = getTemplateParserList(context)
        if (parserList.isEmpty()) {
            LogCat.w("生成代码", "此模板没有解析器，返回静态代码。")
            return staticCode
        } else {
            parserList.forEach {
                if (it.needParse) {
                    val input = it.getInput()
                    if (input.isBlank()) {
                        LogCat.w("生成代码", "模板${it.code}输入为空，跳过处理。")
                    } else {
                        LogCat.d("生成代码", "已将${it.code}的值设置为${input}。")
                        val section = it.section
                        if (section == null) {
                            sourceFile.writeValue(it.code, input)
                        } else {
                            sourceFile.writeValueFromSection(it.code, input, section)
                        }
                    }
                } else {
                    LogCat.d("生成代码", "模板${it.code}无需处理。")
                }
            }
            return sourceFile.text
        }
    }


    /**
     * 获取模板解析器列表
     * @param context Context
     * @return ArrayList<TemplateParser>
     */
    fun getTemplateParserList(context: Context): ArrayList<TemplateParser> {
        if (this::arrayList.isInitialized) {
            LogCat.d("获取模板解析器", "已经被调用了一次，返回成员变量")
            return arrayList
        }
        arrayList = ArrayList()
        val gson = Gson()
        try {
            val jsonArray = jsonData?.getJSONArray("action")
            if (jsonArray == null) {
                LogCat.e("获取模板解析器", "此模板没有action，无法读取。")
                return arrayList
            } else {
                val len = jsonArray.length()
                for (i in 0 until len) {
                    val templateParserData = jsonArray.getJSONObject(i)
                    when (templateParserData.getString("type")) {
                        "input" -> {
                            arrayList.add(
                                InputParser(
                                    context,
                                    gson.fromJson(
                                        templateParserData.toString(4),
                                        InputParserDataBean::class.java
                                    )
                                )
                            )
                        }
                        "comment" -> {
                            arrayList.add(
                                IntroducingParser(
                                    context, gson.fromJson(
                                        templateParserData.toString(4),
                                        IntroducingDataBean::class.java
                                    )
                                )
                            )
                        }
                        "valueSelector" -> {
                            arrayList.add(
                                ListParser(
                                    context, gson.fromJson(
                                        templateParserData.toString(4),
                                        ListParserDataBean::class.java
                                    )
                                )
                            )
                        }
                    }
                }
                return arrayList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogCat.e("获取模板解析器", "解析action出错$e。")
            return arrayList
        }

    }

}