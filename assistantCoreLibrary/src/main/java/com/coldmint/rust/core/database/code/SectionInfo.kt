package com.coldmint.rust.core.database.code

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * @author Cold Mint
 * @date 2022/1/19 11:55
 */
@Entity(tableName = "section")
data class SectionInfo(
    @PrimaryKey val code: String = "",
    @ColumnInfo(name = "translate") val translate: String = "",
    @ColumnInfo(name = "need_name") val needName: Boolean = false,
    @ColumnInfo(name = "is_visible") val isVisible: Boolean = true,
    @ColumnInfo(name = "is_available") val isAvailable: Boolean = true
) {
    @Ignore
    constructor() : this("")
}
