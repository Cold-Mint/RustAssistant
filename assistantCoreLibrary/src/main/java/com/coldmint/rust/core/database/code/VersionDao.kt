package com.coldmint.rust.core.database.code

import androidx.room.*

/**
 * @author Cold Mint
 * @date 2022/1/20 10:58
 */
@Dao
interface VersionDao {


    /**
     * 获取所有
     * @return List<Version>
     */
    @Query("SELECT * FROM game_version")
    fun getAll(): List<Version>

    /**
     * 插入代码信息
     * @param sectionInfo
     */
    @Insert
    fun insertAll(version: List<Version>)

    @Query("SELECT * FROM game_version WHERE versionName = :name LIMIT 1")
    fun findVersionByVersionName(name: String): Version?


    @Update
    fun update(version: Version)

    @Insert
    fun insert(version: Version)

    @Delete
    fun delete(version: Version)

    /**
     * 清理代码表
     */
    @Query("DELETE FROM game_version")
    fun clearTable()
}