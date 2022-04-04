package com.coldmint.rust.pro.tool

import android.content.Context
import com.coldmint.rust.core.tool.FileOperator.writeFile
import com.coldmint.rust.core.tool.FileOperator.readFile
import com.coldmint.rust.pro.interfaces.BookmarkListener
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.interfaces.LineParserEvent
import com.coldmint.rust.pro.databean.Bookmark
import com.coldmint.rust.pro.tool.BookmarkManager
import java.io.File
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.HashMap

/**
 * 构建书签
 *
 * @param context     上下文环境
 * @param bookSigning 书签名（主标签-默认为书签文件名）
 */
class BookmarkManager(val context: Context, private val bookSigning: String = DefaultSigning) {
    //key文件路径,value书签名
    private val hashMap = HashMap<String, String>()
    private val configurationFile: File
    private val valueSymbol = " | v:"
    private val keySymbol = "k:"

    /**
     * 添加书签
     *
     * @param path 书签路径
     * @param name 书签名
     * @return 若书签已存在返回false，反之返回true。
     */
    fun addBookmark(path: String, name: String): Boolean {
        return if (hashMap.containsKey(path)) {
            false
        } else {
            hashMap[path] = name
            true
        }
    }

    /**
     * 添加书签
     *
     * @param bookmark 书签
     * @return 若书签已存在返回false，反之返回true。
     */
    fun addBookmark(bookmark: Bookmark): Boolean {
        return addBookmark(bookmark.path, bookmark.name)
    }

    /**
     * 获取书签数量
     *
     * @return 书签数量
     */
    val size: Int
        get() = hashMap.size

    /**
     * 在列表内构建
     */
    fun fromList(bookmarkListener: BookmarkListener) {
        if (hashMap.size <= 0) {
            return
        }
        val entrySet: Set<Map.Entry<String, String>> = hashMap.entries
        val iterator = entrySet.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            bookmarkListener.find(entity.key, entity.value)
        }
    }

    /**
     * 获取书签列表
     *
     * @return 书签集合，失败返回null
     */
    fun list(): ArrayList<Bookmark>? {
        if (hashMap.size <= 0) {
            return null
        }
        val bookmarkArrayList = ArrayList<Bookmark>()
        val entrySet: Set<Map.Entry<String, String?>> = hashMap.entries
        val iterator = entrySet.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            bookmarkArrayList.add(Bookmark(entity.key, entity.value!!))
        }
        return if (bookmarkArrayList.size > 0) {
            bookmarkArrayList
        } else {
            null
        }
    }

    /**
     * 移除书签
     * 如果没有映射，则返回false。反之返回true
     *
     * @param path 文件路径
     * @return 是否移除成功
     */
    fun removeBookmark(path: String): Boolean {
        val string = hashMap.remove(path)
        return string != null
    }

    /**
     * 移除书签
     *
     * @param bookmark 书签
     * @return 是否移除成功
     */
    fun removeBookmark(bookmark: Bookmark): Boolean {
        return removeBookmark(bookmark.path)
    }

    /**
     * 替换书签
     *
     * @param oldBookmark 旧书签
     * @param newBookmark 新书签
     * @return 是否替换成功
     */
    fun replaceBookmark(oldBookmark: Bookmark, newBookmark: Bookmark): Boolean {
        return if (removeBookmark(oldBookmark)) {
            addBookmark(newBookmark)
        } else {
            false
        }
    }

    /**
     * 保存书签
     * 将书签保存到配置文件内
     *
     * @return 是否保存成功
     */
    fun save(): Boolean {
        if (hashMap.size <= 0) {
            return false
        }
        val stringBuilder = StringBuilder()
        val entrySet: Set<Map.Entry<String, String?>> = hashMap.entries
        val iterator = entrySet.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            stringBuilder.append(keySymbol)
            stringBuilder.append(entity.key)
            stringBuilder.append(valueSymbol)
            stringBuilder.append(entity.value)
            stringBuilder.append('\n')
        }
        return writeFile(configurationFile, stringBuilder.toString())
    }

    /**
     * 读取书签方法
     * 将配置文件里的内容
     *
     * @return
     */
    fun load(): Boolean {
        if (!configurationFile.exists()) {
            return false
        }
        hashMap.clear()
        val lineParser = LineParser(readFile(configurationFile))
        lineParser.analyse(object : LineParserEvent {
            override fun processingData(lineNum: Int, lineData: String, isEnd: Boolean): Boolean {
                val valueIndex = lineData.indexOf(valueSymbol)
                if (valueIndex > -1) {
                    val key = lineData.substring(keySymbol.length, valueIndex)
                    val value = lineData.substring(valueIndex + valueSymbol.length)
                    hashMap[key] = value
                }
                return true
            }
        })
        return true
    }

    /**
     * 是否包含某个文件
     *
     * @param file 文件
     * @return 是否包含
     */
    operator fun contains(file: File): Boolean {
        return hashMap.containsKey(file.absolutePath)
    }

    /**
     * 是否包含某个文件
     *
     * @param path 文件路径
     * @return 是否包含
     */
    operator fun contains(path: String): Boolean {
        return hashMap.containsKey(path)
    }

    /**
     * 是否包含某个书签
     *
     * @param bookmark 书签
     * @return 是否包含
     */
    operator fun contains(bookmark: Bookmark): Boolean {
        return hashMap.containsKey(bookmark.path)
    }

    companion object {
        var DefaultSigning = "Default"
    }


    init {
        val bookmarkFolder = File(context.filesDir.absolutePath + "/bookmarks/")
        if (!bookmarkFolder.exists()) {
            bookmarkFolder.mkdirs()
        }
        configurationFile = File(bookmarkFolder.absolutePath + "/" + this.bookSigning + ".ini")
    }
}