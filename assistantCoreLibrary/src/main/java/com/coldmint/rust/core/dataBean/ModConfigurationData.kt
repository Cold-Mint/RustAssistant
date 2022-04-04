package com.coldmint.rust.core.dataBean

/**
 * @author Cold Mint
 * @date 2021/10/20 19:04
 */
data class ModConfigurationData(
    var updateType: String,
    var updateTitle: String,
    var updateLink: String,
    var sourceFileFilteringRule: String = ".+\\.ini|.+\\.template",
    var garbageFileFilteringRule: String = ".+\\.bak",
    var buildDate: String,
    var modId: String? = null
)