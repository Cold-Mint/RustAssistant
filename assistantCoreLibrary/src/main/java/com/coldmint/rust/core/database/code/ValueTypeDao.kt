package com.coldmint.rust.core.database.code

import androidx.room.*

/**
 * @author Cold Mint
 * @date 2022/1/19 16:43
 */
@Dao
interface ValueTypeDao {
    /**
     * 插入代码信息
     * @param sectionInfo
     */
    @Insert
    fun insertAll(valueTypeInfo: List<ValueTypeInfo>)

    /**
     * 通过type查找类型
     */
    @Query("SELECT * FROM value_type WHERE type = :type LIMIT 1")
    fun findTypeByType(type: String): ValueTypeInfo?


    /**
     * 获取所有
     * @return List<ValueTypeInfo>
     */
    @Query("SELECT * FROM value_type")
    fun getAll(): List<ValueTypeInfo>


    /**
     * 清理代码表
     */
    @Query("DELETE FROM value_type")
    fun clearTable()

    @Insert
    fun insert(it: ValueTypeInfo)

    @Update
    fun update(it: ValueTypeInfo)

    @Delete
    fun delete(it: ValueTypeInfo)
}