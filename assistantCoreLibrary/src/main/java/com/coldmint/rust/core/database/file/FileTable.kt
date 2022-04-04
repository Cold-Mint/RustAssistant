package com.coldmint.rust.core.database.file

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 文件信息表
 * ColumnInfo列信息
 */
@Entity(tableName = "file")
data class FileTable(
    @PrimaryKey val path: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "md5") val md5: String,
    @ColumnInfo(name = "type") val type: String
)
