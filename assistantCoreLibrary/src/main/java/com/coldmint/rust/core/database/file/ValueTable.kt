package com.coldmint.rust.core.database.file

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Cold Mint
 * @date 2022/1/16 15:46
 */
//"value",
//                "\"keyWord\" TEXT,\"type\" TEXT,\"from\" TEXT,PRIMARY KEY (\"keyWord\")
@Entity(tableName = "value")
data class ValueTable(
    @PrimaryKey val keyWord: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "from")
    val from: String
)