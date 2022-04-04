package com.coldmint.rust.core.database.code

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Cold Mint
 * @date 2022/1/19 12:08
 */
@Entity(tableName = "game_version")
data class Version(
    @PrimaryKey val versionName: String,
    @ColumnInfo(name = "version_number") val versionNumber: Int,
)