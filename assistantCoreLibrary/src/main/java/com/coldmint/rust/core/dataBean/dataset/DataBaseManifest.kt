package com.coldmint.rust.core.dataBean.dataset

data class DataBaseManifest(
    val id: String,
    val author: String,
    val describe: String,
    val name: String,
    val versionNumber: Int,
    val versionName: String,
    val tables: Tables
) {
    data class Tables(
        val chain_inspection: String,
        val code: String,
        val game_version: String,
        val section: String,
        val value_type: String
    )
}