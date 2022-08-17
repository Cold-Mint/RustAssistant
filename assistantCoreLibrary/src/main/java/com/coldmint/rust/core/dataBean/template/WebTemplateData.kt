package com.coldmint.rust.core.dataBean.template


import com.google.gson.annotations.SerializedName
import org.json.JSONObject

/**
 * 网络模板详情
 * @property code Int
 * @property `data` Data
 * @property message String
 * @constructor
 */
data class WebTemplateData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("content")
        val content: String,
        @SerializedName("createTime")
        val createTime: String,
        @SerializedName("deleted")
        val deleted: String,
        @SerializedName("developer")
        val developer: String,
        @SerializedName("id")
        val id: String,
        @SerializedName("modificationTime")
        val modificationTime: String,
        @SerializedName("packageId")
        val packageId: String,
        @SerializedName("title")
        val title: String,
    ) : Template {
        private lateinit var jsonObject: JSONObject

        override fun getJson(): JSONObject {
            if (!this::jsonObject.isInitialized) {
                jsonObject = JSONObject(content)
            }
            return jsonObject
        }

        override fun getName(language: String): String {
            return if (getJson().has("name_$language")) {
                return getJson().getString("name_$language")
            } else {
                return getJson().getString("name")
            }
        }

        override fun getIcon(): Any? {
            return null
        }

        override fun isLocal(): Boolean {
            return false
        }

        override fun getLink(): String {
            return id
        }

    }
}