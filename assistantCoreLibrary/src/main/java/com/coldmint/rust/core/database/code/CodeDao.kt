package com.coldmint.rust.core.database.code

import androidx.room.*
import com.coldmint.rust.core.database.file.FileTable

/**
 * @author Cold Mint
 * @date 2022/1/19 11:36
 */
@Dao
interface CodeDao {

    /**
     * 获取所有
     */
    @Query("SELECT * FROM code")
    fun getAll(): List<CodeInfo>


    /**
     * 通过节查找代码
     * @param section String
     * @param limitNum String
     * @return List<CodeInfo>?
     */
    @Query("SELECT * FROM code WHERE section LIKE '%'||:section||'%' OR section='all' ")
    fun findCodeBySection(section: String): List<CodeInfo>?


    /**
     * 查找代码通过关键字
     * @param key String
     * @param section String
     * @param limitNum Int
     * @return List<CodeInfo>?
     */
    @Query("SELECT * FROM code WHERE translate LIKE '%'||:key||'%' AND (section=:section OR section='all') LIMIT :limitNum")
    fun findCodeByKeyInSection(key: String, section: String, limitNum: Int): List<CodeInfo>?

    /**
     * 在类型内通过翻译查找代码
     * @param key String
     * @param type String
     * @param limitNum Int
     * @return List<CodeInfo>?
     */
    @Query("SELECT * FROM code WHERE translate LIKE '%'||:key||'%' AND type=:type LIMIT :limitNum")
    fun findCodeByTranslateInType(key: String, type: String, limitNum: Int): List<CodeInfo>?
    /**
     * 在类型内通过Code查找代码
     * @param key String
     * @param type String
     * @param limitNum Int
     * @return List<CodeInfo>?
     */
    @Query("SELECT * FROM code WHERE code LIKE '%'||:key||'%' AND type=:type LIMIT :limitNum")
    fun findCodeByCodeInType(key: String, type: String, limitNum: Int): List<CodeInfo>?

    /**
     * 搜索代码
     * @param key String 翻译关键字
     * @param section String 包含的节
     * @param limitNum Int 限制数量
     * @return List<CodeInfo>?
     */
    @Query("SELECT * FROM code WHERE translate LIKE '%'||:key||'%' AND (section LIKE '%'||:section||'%' OR section='all') LIMIT :limitNum")
    fun findCodeByKeyFromSection(key: String, section: String, limitNum: Int): List<CodeInfo>?

    /**
     * 搜索代码通过英文
     * @param key String 翻译关键字
     * @param section String 包含的节
     * @param limitNum Int 限制数量
     * @return List<CodeInfo>?
     */
    @Query("SELECT * FROM code WHERE code LIKE '%'||:key||'%' AND (section LIKE '%'||:section||'%' OR section='all') LIMIT :limitNum")
    fun findCodeByEnglishCodeKeyFromSection(key: String, section: String, limitNum: Int): List<CodeInfo>?

    /**
     * 通过代码或者翻译在节内查找代码
     * @param key String
     * @param section String
     * @return List<CodeInfo>?
     */
    @Query("SELECT * FROM code WHERE (translate LIKE '%'||:key||'%' OR code LIKE '%'||:key||'%') AND section LIKE '%'||:section||'%' ")
    fun findCodeByCodeOrTranslateFromSection(key: String, section: String): List<CodeInfo>?

    /**
     * 查找代码通过代码
     * @param code String
     * @return CodeInfo?
     */
    @Query("SELECT * FROM code WHERE code = :code LIMIT 1")
    fun findCodeByCode(code: String): CodeInfo?

    /**
     * 查找代码通过翻译
     * @param translate String
     * @return CodeInfo?
     */
    @Query("SELECT * FROM code WHERE translate = :translate LIMIT 1")
    fun findCodeByTranslate(translate: String): CodeInfo?

    /**
     * 插入代码信息
     * @param codeInfo
     */
    @Insert
    fun insertAll(codeInfo: List<CodeInfo>)

    /**
     * 插入代码信息
     * @param codeInfo
     */
    @Insert
    fun insert(codeInfo: CodeInfo)

    /**
     * 更新
     * @param codeInfo CodeInfo
     */
    @Update
    fun update(codeInfo: CodeInfo)

    /**
     * 清理代码表
     */
    @Query("DELETE FROM code")
    fun clearTable()

    /**
     * 删除
     * @param codeInfo CodeInfo
     */
    @Delete
    fun delete(codeInfo: CodeInfo)

}