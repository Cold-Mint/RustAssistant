package com.coldmint.rust.core.database.code

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Cold Mint
 * @date 2022/1/19 11:37
 */
@Entity(tableName = "code")
data class CodeInfo(
    @PrimaryKey val code: String,
    @ColumnInfo(name = "translate") val translate: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "demo") val demo: String = "",
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "add_version") val addVersion: Int,
    @ColumnInfo(name = "remove_version") val removeVersion: Int = 1,
    @ColumnInfo(name = "section") val section: String
)