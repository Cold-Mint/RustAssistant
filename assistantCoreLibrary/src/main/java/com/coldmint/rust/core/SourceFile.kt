package com.coldmint.rust.core

import android.content.Context
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.interfaces.LineParserEvent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.turret.TurretManager
import java.io.File
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.regex.Pattern

/**
 * 源文件类
 * @property file File 文件
 * @property modclass ModClass?
 * @property text String
 * @property allSection Array<String>
 * @constructor
 */
class SourceFile(text: String) {
    //获取模组文件
    lateinit var file: File
    private var modclass: ModClass? = null

    /**
     * 获取文本内容（在内存中读取）
     *
     * @return 文本内容
     *///设置文件内容
    var text: String


    init {
        if (text.startsWith(Environment.getExternalStorageDirectory().absolutePath)) {
            throw RuntimeException("引用的内容可能是文件路径。${text}")
        } else {
            this.text = text
        }
    }

    /**
     * 供文件管理器使用
     *
     * @param file 文件
     */
    constructor(file: File) : this(FileOperator.readFile(file) ?: "") {
        this.file = file
    }

    /**
     * 一般的构造方法
     *
     * @param file     文件
     * @param modclass 模组类
     */
    constructor(file: File, modclass: ModClass) : this(FileOperator.readFile(file) ?: "") {
        this.file = file
        this.modclass = modclass
    }

    /**
     * 去非法字符 \r
     */
    fun removeIllegalCharacters() {
        text = text.replace("\r", "")
    }

    /**
     * 获取炮塔管理器
     */
    fun getTurretManager(): TurretManager {
        return TurretManager(this)
    }


    /**
     * 设置模组类
     *
     *
     * 若未设置模组类，则搜索资源文件方法永远返回null
     *
     * @param modclass
     */
    fun setModclass(modclass: ModClass?) {
        this.modclass = modclass
    }

    /**
     * 获取所有节
     *
     * @return 节数组(不带符号 " [] “)
     */
    val allSection: Array<String>
        get() {
            val matcher = Pattern.compile("\\[.+\\]").matcher(
                text
            )
            val strings = ArrayList<String>()
            while (matcher.find()) {
                val s = matcher.group()
                val length = s.length
                strings.add(s.substring(1, length - 1))
            }
            return strings.toTypedArray()
        }

    /**
     * 搜索资源文件
     *
     * @param value       值
     * @param checkExists 检查是否存在
     * @return 文件数组，未设置模组类返回null，无匹配内容返回null
     */
    fun findResourceFiles(value: String?, checkExists: Boolean): Array<File>? {
        var value = value
        if (modclass == null || !this::file.isInitialized) {
            return null
        }
        val none = "NONE"
        val auto = "AUTO"
        val shared = "SHARED"
        val root = "ROOT"
        return if (value == null || value == none || value == auto || value.startsWith(shared)) {
            null
        } else {
            val result: ArrayList<File> = ArrayList()
            if (value.startsWith(root)) {
                val modpath = modclass!!.modFile.absolutePath + "/"
                val start_num = value.lastIndexOf(root)
                val path = modpath + value.substring(start_num + root.length)
                val target = File(path)
                if (checkExists) {
                    if (target.exists()) {
                        result.add(target)
                    }
                } else {
                    result.add(target)
                }
            } else if (value.contains(",")) {
                val lineParser = LineParser(value)
                lineParser.needTrim = true
                lineParser.symbol = ","
                lineParser.analyse(object : LineParserEvent {
                    override fun processingData(
                        lineNum: Int,
                        lineData: String,
                        isEnd: Boolean
                    ): Boolean {
                        val oneFile = findResourceFiles(lineData, false)
                        if (oneFile != null && oneFile.isNotEmpty()) {
                            val target = oneFile[0]
                            if (checkExists) {
                                if (target.exists()) {
                                    result.add(target)
                                }
                            } else {
                                result.add(target)
                            }
                        }
                        return true
                    }
                })
            } else {
                val directory = FileOperator.getSuperDirectory(
                    file
                )
                value = "${directory}/${value}"
                val target = File(value)
                if (checkExists) {
                    if (target.exists()) {
                        result.add(target)
                    }
                } else {
                    result.add(target)
                }
            }
            if (result.size > 0) {
                result.toTypedArray()
            } else {
                null
            }
        }
    }

    /**
     * 搜索资源文件返回null或者空数组。
     *
     * @param key         键
     * @param checkExists 检查是否存在
     * @return 文件数组，未设置模组类返回null，无匹配内容返回null
     */
    fun findResourceFilesFromKey(key: String, checkExists: Boolean): Array<File>? {
        if (modclass == null) {
            return null
        }
        val value = readValue(key)
        return findResourceFiles(value, checkExists)
    }

    /**
     * 在节内搜索资源文件
     *
     * @param key         键
     * @param section     节
     * @param checkExists 检查是否存在
     * @return 文件数组，无匹配内容返回null,未设置模组类返回null
     */
    fun findResourceFilesFromSection(
        key: String,
        section: String,
        checkExists: Boolean
    ): Array<File>? {
        if (modclass == null) {
            return null
        }
        val value = readValueFromSection(key, section)
        return findResourceFiles(value, checkExists)
    }

    /**
     * 获取单位名称
     *
     * @return 单位名称
     */
    fun getName(language: String?): String {
        val display_name = readValue("displayText", language, readValue("displayText"))
        val finalFile = file
        return if (display_name == null) {
            val readName = readValue("name")
            return readName ?: FileOperator.getPrefixName(finalFile)
        } else {
            display_name
        }
    }

    /**
     * 获取单位描述
     *
     * @return 单位描述
     */
    fun getDescribe(language: String?, defaultValue: String?): String? {
        return readValue(
            "displayDescription",
            language,
            readValue("displayDescription", defaultValue)
        )
    }

    /**
     * 读取值
     *
     * @param key 键
     * @return 成功返回值，失败返回null
     */
    fun readValue(key: String): String? {
        var key = key
        key = "\n${key}:"
        val info = "\n${text}\n"
        //contains 检查是否包含
        val startNum = info.indexOf(key)
        if (startNum > 0) {
            //substring 截取字符串（起点，终点）
            //indexOf 搜索字符串的位置
            //trim 删除首尾空
            val symbolNum = info.indexOf(":", startNum)
            val endNum = info.indexOf("\n", symbolNum)
            return info.substring(symbolNum + 1, endNum).trim { it <= ' ' }
        }
        return null
    }

    /**
     * 读取值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 成功返回值，失败返回默认值
     */
    fun readValue(key: String, defaultValue: String?): String? {
        val result = readValue(key)
        return result ?: defaultValue
    }

    /**
     * 读取值
     *
     * @param key          键
     * @param language     语言
     * @param defaultValue 默认值
     * @return 成功返回值，失败返回默认值
     */
    fun readValue(key: String?, language: String?, defaultValue: String?): String? {
        val builder = StringBuilder()
        builder.append(key)
        builder.append("_")
        builder.append(language)
        return readValue(builder.toString(), defaultValue)
    }

    /**
     * 在某个节里读取值
     *
     * @param key     键
     * @param section 节名（不带符号）
     * @return 失败返回null，成功返回值
     */
    fun readValueFromSection(key: String, section: String): String? {
        var key = key
        var info = readSection(section)
        if (info != null) {
            if (!info.startsWith("\n")) {
                info = "\n${info}"
            }
            key = "\n$key:"
            val startNum = info.indexOf(key)
            if (startNum > -1) {
                val symbolNum = info.indexOf(":", startNum)
                val endNum = info.indexOf("\n", symbolNum)
                val result: String = if (endNum > -1) {
                    info.substring(symbolNum + 1, endNum).trim { it <= ' ' }
                } else {
                    info.substring(symbolNum + 1).trim { it <= ' ' }
                }
                return result
            }
        }
        return null
    }

    /**
     * 读取节数据
     *
     * @param section 节名（不带符号）
     * @return 成功返回节内容, 失败返回null
     */
    fun readSection(section: String): String? {
        var section = section
        section = "\n[$section]\n"
        val info = "\n${text}\n["
        val startNum = info.indexOf(section)
        if (startNum > -1) {
            val endNum = info.indexOf("\n[", startNum + section.length)
            return if (endNum > 1) {
                info.substring(startNum + section.length, endNum).trim { it <= ' ' }
            } else {
                info.substring(startNum + section.length).trim { it <= ' ' }
            }
        }
        return null
    }

    /**
     * 包含键
     *
     * @param key 键
     * @return 是否存在
     */
    fun containKey(key: String): Boolean {
        var key = key
        key = "\n${key}:"
        val info = "\n${text}\n"
        //contains 检查是否包含
        return info.contains(key)
    }

    /**
     * 某节内是否包含某键
     *
     * @param key     键
     * @param section 节
     * @return 是否包含
     */
    fun containKeyFromSection(key: String, section: String): Boolean {
        val info = "\n${readSection(section)}\n"
        return info.contains("\n$key:")
    }

    /**
     * 是否包含节
     *
     * @param section   节名，不带符号
     * @param isAllName 是否为完整节名
     * @return 是否包含
     */
    fun containSection(section: String, isAllName: Boolean): Boolean {
        var section = section
        section = if (isAllName) {
            "\n[$section]\n"
        } else {
            "\n[${section}_"
        }
        val info = "\n${text}"
        return info.contains(section)
    }

    /**
     * 保存数据到文件，如果文件被指名则保存至文件，若未指名则永远返回false
     * @return Boolean
     */
    fun save(): Boolean {
        return if (this::file.isInitialized) {
            FileOperator.writeFile(
                file,
                text
            )
        } else {
            false
        }
    }

    /**
     * 写值
     *
     * @param key   键
     * @param value 值
     * @return 写入结果，键不存在返回假
     */
    fun writeValue(key: String, value: String?): Boolean {
        var keyCode = key
        keyCode = "\n${keyCode}:"
        val info = "\n${text}\n"
        val startnum = info.indexOf(keyCode)
        if (startnum > -1) {
            val symbolnum = info.indexOf(":", startnum)
            val endnum = info.indexOf("\n", symbolnum)
            val stringBuilder = StringBuilder()
            stringBuilder.append(info.substring(1, symbolnum + 1))
            stringBuilder.append(value)
            stringBuilder.append(info.substring(endnum, info.length - 1))
            text = stringBuilder.toString()
            return true
        }
        return false
    }

    /**
     * 写值或者添加值
     * 值存在修改其值，不存在添加一份
     *
     * @param key     键
     * @param value   值
     * @param section 节
     */
    fun writeValueOrAddKey(key: String, value: String, section: String) {
        //先尝试改
        val modify = writeValueFromSection(key, value, section)
        if (!modify) {
            //读取节数据
            val sectionData = readSection(section)
            if (sectionData == null) {
                text = "${text}\n[${section}]\n${key}:${value}"
            } else {
                val newSectionData = "${sectionData}\n${key}:${value}"
                text = text.replaceFirst(sectionData.toRegex(), newSectionData)
            }
        }
    }

    /**
     * 在节内修改值
     * @param key String 键
     * @param value String 值
     * @param section String 节
     * @return Boolean 是否修改成功
     */
    fun writeValueFromSection(key: String, value: String, section: String): Boolean {
        var key = key
        var section = section
        key = "\n$key:"
        section = "\n[$section]\n"
        val info: String
        var hasSymbol = true
        if (text.startsWith("\n")) {
            info = "\n$text\n["
        } else {
            info = "\n\n$text\n["
            hasSymbol = false
        }
        val sectionstartnum = info.indexOf(section)
        if (sectionstartnum > -1) {
            val sectionendnum = info.indexOf("\n[", sectionstartnum + section.length)
            val result =
                info.substring(sectionstartnum + section.length, sectionendnum).trim()
            val sinfo = "\n${result}\n"
            if (sinfo.contains(key)) {
                val startnum = sinfo.indexOf(key)
                val symbolnum = sinfo.indexOf(":", startnum)
                val endnum = sinfo.indexOf("\n", symbolnum)
                val stringBuilder = StringBuilder()
                stringBuilder.append(info.substring(1, sectionstartnum))
                stringBuilder.append(section)
                stringBuilder.append(sinfo.substring(1, symbolnum + 1))
                stringBuilder.append(value)
                stringBuilder.append(sinfo.substring(endnum, sinfo.length - 1))
                stringBuilder.append(info.substring(sectionendnum, info.length - 2))
                text = if (hasSymbol) {
                    stringBuilder.toString()
                } else {
                    stringBuilder.substring(1)
                }
                return true
            }
        }
        return false
    }

    /**
     * 获取单位图标
     * @return Drawable?
     */
    fun getIcon(): Drawable? {
        var mainIcon: Drawable? = null
        val baseImages = findResourceFilesFromSection("image", "graphics", true)
        if (baseImages != null && baseImages.isNotEmpty()) {
            val file = baseImages[0]
            mainIcon = Drawable.createFromPath(file.absolutePath)
        }
        return mainIcon
    }

    companion object {
        /**
         * 获取节绝对名
         *
         * @param name 节名（不带符号“[]"）
         * @return 例如“炮塔_a”返回a,不含"_"返回传入值
         */
        fun getAbsoluteSectionName(name: String): String {
            val index = name.lastIndexOf("_")
            return if (index > -1) {
                name.substring(index + 1)
            } else {
                name
            }
        }

        /**
         * 获取节类型名
         *
         * @param name 节名（不带符号"[]"）
         * @return 例如"炮塔_a"返回"炮塔",不含"_"返回传入值
         */
        fun getSectionType(name: String): String {
            val index = name.lastIndexOf("_")
            return if (index > -1) {
                name.substring(0, index)
            } else {
                name
            }
        }
    }
}