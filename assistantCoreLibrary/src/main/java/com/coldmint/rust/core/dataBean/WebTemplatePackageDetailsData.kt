package com.coldmint.rust.core.dataBean


import com.google.gson.annotations.SerializedName

data class WebTemplatePackageDetailsData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("packageData")
        val packageData: PackageData,
        @SerializedName("templateList")
        val templateList: List<Template>
    ) {
        data class PackageData(
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
            val name: String,
            @SerializedName("public")
            val `public`: String,
            @SerializedName("subscriptionNumber")
            val subscriptionNumber: String,
            @SerializedName("templateNumber")
            val templateNumber: String,
            @SerializedName("versionName")
            val versionName: String,
            @SerializedName("versionNumber")
            val versionNumber: String
        )

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
        )
    }
}