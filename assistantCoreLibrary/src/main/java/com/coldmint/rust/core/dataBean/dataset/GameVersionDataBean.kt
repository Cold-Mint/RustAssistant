package com.coldmint.rust.core.dataBean.dataset

import com.coldmint.rust.core.database.code.Version

data class GameVersionDataBean(
    val `data`: List<Version>,
    val name: String
)