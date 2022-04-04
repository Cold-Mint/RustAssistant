package com.coldmint.rust.core.database.file

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * @author Cold Mint
 * @date 2022/1/16 15:51
 */
@Dao
interface ValueDao {

    @Query("SELECT * FROM value ORDER BY keyWord DESC")
    fun getAll(): List<ValueTable>

    @Insert
    fun insert(valueTable: ValueTable)

    @Update
    fun update(valueTable: ValueTable)

    @Query("SELECT * FROM value WHERE keyWord LIKE '%'||:keyWord||'%'  AND type = :type LIMIT :limitNum")
    fun searchValueByKey(keyWord: String, type: String, limitNum: Int = 10): List<ValueTable>?

    @Query("SELECT * FROM value WHERE keyWord = :keyWord LIMIT 1")
    fun findValueByKey(keyWord: String): ValueTable?

    @Query("DELETE FROM value")
    fun clearTable()
}