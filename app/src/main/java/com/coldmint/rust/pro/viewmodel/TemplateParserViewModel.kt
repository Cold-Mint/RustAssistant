package com.coldmint.rust.pro.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.coldmint.rust.core.dataBean.InputParserDataBean
import com.coldmint.rust.core.dataBean.IntroducingDataBean
import com.coldmint.rust.core.dataBean.ListParserDataBean
import com.coldmint.rust.core.dataBean.template.Template
import com.coldmint.rust.core.interfaces.TemplateParser
import com.coldmint.rust.core.templateParser.InputParser
import com.coldmint.rust.core.templateParser.IntroducingParser
import com.coldmint.rust.core.templateParser.ListParser
import com.coldmint.rust.pro.base.BaseViewModel
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class TemplateParserViewModel : BaseViewModel() {

    //json数据
    private var jsonData: JSONObject? = null
    private var template: Template? = null

    /**
     * 设置模板数据
     * @param template Template
     */
    fun setTemplate(template: Template) {
        this.template = template
        this.jsonData = template.getJson()
    }


    /**
     * 获取源代码
     * @return String
     */
    fun getCode(): String {
        return jsonData?.getString("data") ?: ""
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
     * 获取模板解析器列表
     * @param context Context
     * @return ArrayList<TemplateParser>
     */
    fun getTemplateParserList(context: Context): ArrayList<TemplateParser> {
        val gson = Gson()
        val arrayList = ArrayList<TemplateParser>()
        val jsonArray = jsonData?.getJSONArray("action")
        if (jsonArray == null) {
            Log.e("获取模板解析器", "此模板没有action，无法读取。")
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
    }

}