package com.coldmint.rust.core.dataBean.webTemplate


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
        @SerializedName("downloadNumber")
        val downloadNumber: Int,
        @SerializedName("templateNumber")
        val templateNumber: Int,
        @SerializedName("id")
        val id: String,
        @SerializedName("modificationTime")
        val modificationTime: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("public")
        val `public`: Boolean,
        @SerializedName("versionName")
        val versionName: String,
        @SerializedName("versionNumber")
        val versionNumber: String,
        @SerializedName("subscribe")
        val subscribe: Boolean
    )
}