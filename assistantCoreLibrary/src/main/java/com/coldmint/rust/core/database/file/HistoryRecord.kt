package com.coldmint.rust.core.database.file

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Cold Mint
 * @date 2022/1/14 15:13
 */
@Entity(tableName = "history")
data class HistoryRecord(
    @PrimaryKey val path: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "time") val time: String
)
