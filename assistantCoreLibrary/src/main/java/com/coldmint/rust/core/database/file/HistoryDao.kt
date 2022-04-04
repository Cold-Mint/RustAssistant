package com.coldmint.rust.core.database.file

import androidx.room.*

/**
 * @author Cold Mint
 * @date 2022/1/14 15:15
 */
@Dao
interface HistoryDao {

    /**
     * 查找所有历史记录
     * @return List<FileTable>
     */
    @Query("SELECT * FROM history ORDER BY time DESC")
    fun getAll(): List<HistoryRecord>

    @Query("SELECT * FROM history WHERE path=:newPath LIMIT 1")
    fun findHistoryByPath(newPath: String): HistoryRecord?

    /**
     * 插入一条历史记录
     * @param historyRecord 历史记录
     */
    @Insert
    fun insert(historyRecord: HistoryRecord)

    /**
     * 更新数据
     * @param historyRecord HistoryRecord
     */
    @Update
    fun update(historyRecord: HistoryRecord)

    /**
     * 删除记录
     * @param historyRecord HistoryRecord
     */
    @Delete
    fun delete(historyRecord: HistoryRecord)

}