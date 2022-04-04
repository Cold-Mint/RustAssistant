package com.coldmint.rust.core.dataBean.dataset

import com.coldmint.rust.core.database.code.ChainInspection

data class ChainInspectionDataBean(
    val `data`: List<ChainInspection>,
    val name: String
)