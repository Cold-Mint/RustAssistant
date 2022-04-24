package com.coldmint.rust.core.dataBean.dataset

import com.google.gson.annotations.SerializedName

data class DataBaseManifest(
    @SerializedName("author")
    val author: String,
    @SerializedName("describe")
    val describe: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("tables")
    val tables: Tables,
    @SerializedName("updateLog")
    val updateLog: List<String>,
    @SerializedName("versionName")
    val versionName: String,
    @SerializedName("versionNumber")
    val versionNumber: Int
) {
    data class Tables(
        @SerializedName("chain_inspection")
        val chainInspection: String,
        @SerializedName("code")
        val code: String,
        @SerializedName("game_version")
        val gameVersion: String,
        @SerializedName("section")
        val section: String,
        @SerializedName("value_type")
        val valueType: String
    )
}