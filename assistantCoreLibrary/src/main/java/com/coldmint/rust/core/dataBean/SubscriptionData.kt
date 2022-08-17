package com.coldmint.rust.core.dataBean


import android.util.Log
import com.coldmint.rust.core.dataBean.template.Template
import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.TemplatePhp
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

/**
 * 订阅信息
 * @property code Int
 * @property `data` List<Data>
 * @property message String
 * @constructor
 */
data class SubscriptionData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("appVersionName")
        val appVersionName: String,
        @SerializedName("appVersionNumber")
        val appVersionNumber: String,
        @SerializedName("createTime")
        val createTime: String,
        @SerializedName("describe")
        val describe: String,
        @SerializedName("developer")
        val developer: String,
        @SerializedName("id")
        val id: String,
        @SerializedName("modificationTime")
        val modificationTime: String,
        @SerializedName("name")
        val name1: String,
        @SerializedName("public")
        val `public`: String,
        @SerializedName("subscriptionNumber")
        val subscriptionNumber: String,
        @SerializedName("templateList")
        val templateList: List<Template>,
        @SerializedName("templateNumber")
        val templateNumber: String,
        @SerializedName("versionName")
        val versionName: String,
        @SerializedName("versionNumber")
        val versionNumber: String
    ) : TemplatePackage {
        data class Template(
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
            val title: String
        ) : com.coldmint.rust.core.dataBean.template.Template {

            private val jsonObject: JSONObject by lazy {
                JSONObject(content)
            }

            override fun getJson(): JSONObject {
                return jsonObject
            }

            override fun getName(language: String): String {
                return title
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

        override fun getName(): String {
            return name1
        }

        override fun getDescription(): String {
            return describe
        }

        override fun delete(token: String, func: (Boolean) -> Unit) {
            TemplatePhp.instance.deleteSubscription(token, id, object : ApiCallBack<ApiResponse> {
                override fun onResponse(t: ApiResponse) {
                    if (t.code == ServerConfiguration.Success_Code) {
                        Log.d("取消订阅", "成功。")
                        func.invoke(true)
                    } else {
                        Log.e("取消订阅", t.message)
                        func.invoke(false)
                    }
                }

                override fun onFailure(e: Exception) {
                    e.printStackTrace()
                    Log.e("取消订阅", e.toString())
                    func.invoke(false)
                }

            })
        }

        override fun isLocal(): Boolean {
            return false
        }
    }
}