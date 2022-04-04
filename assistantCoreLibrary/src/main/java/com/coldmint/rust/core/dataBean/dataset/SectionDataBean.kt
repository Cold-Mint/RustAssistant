package com.coldmint.rust.core.dataBean.dataset

import com.coldmint.rust.core.database.code.SectionInfo

data class SectionDataBean(
    val `data`: List<SectionInfo>,
    val name: String
)