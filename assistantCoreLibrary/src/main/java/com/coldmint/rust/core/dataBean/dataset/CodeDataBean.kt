package com.coldmint.rust.core.dataBean.dataset

import com.coldmint.rust.core.database.code.CodeInfo

data class CodeDataBean(
    val `data`: List<CodeInfo>,
    val name: String
)