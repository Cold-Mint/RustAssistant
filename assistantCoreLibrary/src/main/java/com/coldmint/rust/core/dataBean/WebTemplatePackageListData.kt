package com.coldmint.rust.core.dataBean


import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.core.web.TemplatePhp
import com.google.gson.annotations.SerializedName

data class WebTemplatePackageListData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: MutableList<Data>,
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
        @SerializedName("templateNumber")
        val templateNumber: String,
        @SerializedName("versionName")
        val versionName: String,
        @SerializedName("versionNumber")
        val versionNumber: String,
        @SerializedName("subscribe")
        var subscribe:Boolean = false
    ):TemplatePackage{
        override fun getPathORId(): String {
            return id
        }

        override fun getName(): String {
            return name1
        }

        override fun getDescription(): String {
            return describe
        }

        override fun delete(token: String, func: (Boolean) -> Unit) {

        }

        override fun isLocal(): Boolean {
            return false
        }

    }
}