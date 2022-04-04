package com.coldmint.rust.core

import com.coldmint.rust.core.tool.FileOperator
import java.io.File
import java.text.SimpleDateFormat

/**
 * 地图类
 */
class MapClass(val file: File) {

    /**
     * 次构造方法
     * @param string String
     * @constructor
     */
    constructor(string: String) : this(File(string))

    /**
     * 获取图标文件
     * 存在图标则返回文件，不存在图标则返回null
     */
    fun getIconFile(): File? {
        val iconPath =
            FileOperator.getSuperDirectory(file) + "/" + FileOperator.getPrefixName(file) + "_map.png"
        val iconFile = File(iconPath)
        return if (iconFile.exists()) {
            iconFile
        } else {
            null
        }
    }

    /**
     * 获取模组最后修改时间
     *
     * @return 修改时间字符串
     */
    val lastModificationTime: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return formatter.format(file.lastModified())
        }

    /**
     * 获取地图名称
     * @return String
     */
    fun getName(): String {
//        val head = "<property name=\"introText\" value=\""
//        val end = "\"/>"
//        val info = getInfo()
//        if (info != null) {
//            val index = info.indexOf(head)
//            if (index > -1) {
//                val endIndex = info.indexOf(end, index)
//                if (endIndex > -1) {
//                    return info.substring(index, endIndex)
//                }
//            }
//        }
        return FileOperator.getPrefixName(file)
    }


    /**
     * 获取地图信息
     * @return String?
     */
    fun getInfo(): String? {
        return FileOperator.readFile(file)
    }

    /**
     * 删除方法（包括附加图标）
     */
    fun delete(): Boolean {
        val icon = getIconFile()
        val iconDel = icon?.delete() ?: true
        val thisDel = file.delete()
        return iconDel && thisDel
    }

}