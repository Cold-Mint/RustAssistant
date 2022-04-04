package com.coldmint.rust.core.database.code

import androidx.room.*

/**
 * @author Cold Mint
 * @date 2022/1/20 14:40
 */
@Dao
interface ChainInspectionDao {

    /**
     * 插入链式检查信息
     * @param chainInspection
     */
    @Insert
    fun insertAll(chainInspection: List<ChainInspection>)

    @Query("SELECT * FROM chain_inspection WHERE id = :id LIMIT 1")
    fun findChainInspectionById(id: String): ChainInspection?

    /**
     * 清理表
     */
    @Query("DELETE FROM chain_inspection")
    fun clearTable()

    @Insert
    fun insert(it: ChainInspection)

    @Update
    fun update(it: ChainInspection)

    @Delete
    fun delete(it: ChainInspection)
}