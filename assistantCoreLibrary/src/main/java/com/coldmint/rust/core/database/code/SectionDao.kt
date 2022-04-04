package com.coldmint.rust.core.database.code

import androidx.room.*

/**
 * @author Cold Mint
 * @date 2022/1/19 16:35
 */
@Dao
interface SectionDao {


    /**
     * 获取所有
     * @return List<SectionInfo>
     */
    @Query("SELECT * FROM section")
    fun getAll(): List<SectionInfo>

    /**
     * 通过代码获取节信息
     * @param code String翻译
     * @param isAvailable Boolean 是否可用（默认为真）
     * @return SectionInfo?
     */
    @Query("SELECT * FROM section WHERE code =:code AND is_available = :isAvailable LIMIT 1 ")
    fun findSectionInfoByCode(code: String, isAvailable: Boolean = true): SectionInfo?

    @Query("SELECT * FROM section WHERE code =:code LIMIT 1 ")
    fun findSectionInfoByCodeNotCheckAvailability(code: String): SectionInfo?

    /**
     * 通过翻译获取节信息
     * @param translate String翻译
     * @param isAvailable Boolean 是否可用（默认为真）
     * @return SectionInfo?
     */
    @Query("SELECT * FROM section WHERE translate =:translate AND is_available =:isAvailable  LIMIT 1 ")
    fun findSectionInfoByTranslate(translate: String, isAvailable: Boolean = true): SectionInfo?

    @Query("SELECT * FROM section WHERE translate LIKE '%'||:key||'%' AND is_available =:isAvailable  LIMIT :limitNum ")
    fun searchSectionInfoByTranslate(
        key: String,
        isAvailable: Boolean = true,
        limitNum: Int
    ): List<SectionInfo>?

    @Query("SELECT * FROM section WHERE code LIKE '%'||:key||'%' AND is_available =:isAvailable  LIMIT :limitNum ")
    fun searchSectionInfoByCode(
        key: String,
        isAvailable: Boolean = true,
        limitNum: Int
    ): List<SectionInfo>?

    /**
     * 插入代码信息
     * @param sectionInfo
     */
    @Insert
    fun insertAll(sectionInfo: List<SectionInfo>)

    /**
     * 插入代码信息
     * @param sectionInfo
     */
    @Insert
    fun insert(sectionInfo: SectionInfo)

    @Update
    fun update(sectionInfo: SectionInfo)

    /**
     * 清理代码表
     */
    @Query("DELETE FROM section")
    fun clearTable()

    @Delete
    fun delete(it: SectionInfo)
}