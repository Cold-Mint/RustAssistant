package com.coldmint.rust.core.dataBean.template

data class TemplateInfo(
    val appVersionNum: Int,
    val description: String,
    val developer: String,
    @Deprecated(
        "不要直接读取模板名称，请使用getName方法",
        ReplaceWith("getName()", "com.coldmint.rust.core"), DeprecationLevel.WARNING
    )
    val name: String,
    val update: String,
    val versionName: String,
    val versionNum: Int
)