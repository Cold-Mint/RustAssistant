package com.coldmint.rust.pro.databean

import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.tool.AppSettings
import com.google.gson.Gson
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

/**
 * 程序错误信息
 * @property time String
 * @property allErrorDetails String
 * @property describe String?
 * @constructor
 */
data class ErrorInfo(
    private val nowTime: Long = System.currentTimeMillis(),
    val time: String = ServerConfiguration.toStringTime(nowTime),
    val id: String = UUID.randomUUID().toString(),
    var allErrorDetails: String = "",
    var activityLog: String? = null,
    var autoSave: Boolean = true,
) {


    /**
     * 保存ErrorInfo
     * @return Boolean
     */
    fun save(): Boolean {
        val filePath = StringBuilder()
        filePath.append(AppSettings.dataRootDirectory)
        filePath.append("/carsh/")
        val folderFormatter = SimpleDateFormat("yyyy-MM-dd")
        val folderName = folderFormatter.format(nowTime)
        filePath.append(folderName)
        val folder = File(filePath.toString())
        if (!folder.exists()) {
            folder.mkdirs()
        }
        filePath.append("/")
        val dateFormat = SimpleDateFormat("HH-mm-ss")
        val fileName = dateFormat.format(nowTime)
        filePath.append(fileName)
        filePath.append(".log")
        val gson = Gson()
        val logFile = File(filePath.toString())
        return FileOperator.writeFile(logFile, gson.toJson(this))
    }

}