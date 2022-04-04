package com.coldmint.rust.core.database.code

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Cold Mint
 * @date 2022/1/19 12:11
 */
@Entity(tableName = "chain_inspection")
data class ChainInspection(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "list") val list: String
)
