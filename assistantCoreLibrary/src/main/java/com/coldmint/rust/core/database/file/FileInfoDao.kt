package com.coldmint.rust.core.database.file

import androidx.room.*

/**
 * @author Cold Mint
 * @date 2022/1/14 13:27
 */
@Dao
interface FileInfoDao {

    /**
     * 查找所有文件
     * @return List<FileTable>
     */
    @Query("SELECT * FROM file")
    fun getAll(): List<FileTable>

    @Query("SELECT * FROM file WHERE type=:fileType LIMIT :limitNum")
    fun findFileInfoByType(fileType: String, limitNum: Int): List<FileTable>?

    //LIKE '%'||:name||'%'
    @Query("SELECT * FROM file WHERE file_name LIKE '%'||:name||'%' AND type=:fileType LIMIT :limitNum")
    fun searchFileInfoByNameAndType(name: String, fileType: String, limitNum: Int): List<FileTable>?


    @Query("SELECT * FROM file WHERE path=:newPath LIMIT 1")
    fun findFileInfoByPath(newPath: String): FileTable?

    @Query("SELECT * FROM file WHERE file_name LIKE '%'||:name||'%' LIMIT :limitNum")
    fun searchFileInfoByName(name: String, limitNum: Int): List<FileTable>?

    /**
     * 插入文件信息
     * @param fileInfo FileTable
     */
    @Insert
    fun insert(fileInfo: FileTable)

    /**
     * 更新数据
     * @param fileInfo List<HistoryRecord>
     */
    @Update
    fun update(fileInfo: FileTable)


    /**
     * 插入文件信息
     * @param fileInfo FileTable
     */
    @Insert
    @Deprecated("不建议使用")
    fun insertAll(fileInfo: List<FileTable>)

    /**
     * 删除文件信息
     * @param fileInfo FileTable
     */
    @Delete
    fun delete(fileInfo: FileTable)

    @Query("DELETE FROM file")
    fun clearTable()

}