package com.coldmint.rust.core.dataBean.dataset

import com.coldmint.rust.core.database.code.ValueTypeInfo

data class ValueTypeDataBean(
    val `data`: List<ValueTypeInfo>,
    val name: String,
)