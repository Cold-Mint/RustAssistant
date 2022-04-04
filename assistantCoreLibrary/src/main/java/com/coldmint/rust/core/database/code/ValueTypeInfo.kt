package com.coldmint.rust.core.database.code

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Cold Mint
 * @date 2022/1/19 11:59
 */
@Entity(tableName = "value_type")
data class ValueTypeInfo(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "rule") val rule: String,
    @ColumnInfo(name = "external") val external: String = "",
    @ColumnInfo(name = "offset") val offset: String,
    @ColumnInfo(name = "list") val list: String,
    @ColumnInfo(name = "tag") val tag: String,
    @ColumnInfo(name = "describe") val describe: String
)