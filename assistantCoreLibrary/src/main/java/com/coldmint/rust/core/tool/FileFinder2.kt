package com.coldmint.rust.core.tool

import android.util.Log
import com.coldmint.rust.core.interfaces.FileFinderInterface
import com.coldmint.rust.core.interfaces.FileFinderListener
import java.io.File

/**
 * FileFinder2 文件搜索器
 * 基于FileTree构建
 */
class FileFinder2 : FileFinderInterface {
    //设置根目录
    private val root: File

    //是否检查空文件夹
    var isDetectingEmptyFolder = false

    private var fileFinderListener: FileFinderListener? = null

    /**
     * 查找规则
     */
    var findRule = ".+"

    /**
     * 是否为搜索模式
     */
    var findMode = false

    /**
     * 启用Re正则搜索
     */
    var asRe = false

    //找到的文件对象
    private var targetFile: File? = null

    constructor(root: File) {
        this.root = root
    }

    /**
     * 获取找到的文件对象
     * @return File?
     */
    fun getTargetFile(): File? {
        return targetFile
    }

    /**
     * 开始搜索
     * 必须设置[FileFinder2.findMode]为真，才会按条件过滤
     * 使用[FileFinder2.asRe]设置是否为正则扫描模式
     * 使用[FileFinder2.findRule]设置正则表达式，或文件关键字
     * @return Boolean
     */
    override fun onStart(): Boolean {
        val treeWalk = root.walk()
        var result = true
        val finalFindListener: FileFinderListener =
            fileFinderListener ?: throw NullPointerException("未设置监听器")
        val fileList:
                Sequence<File> = if (findMode) {
            treeWalk.filter {
                if (asRe) {
                    it.name.matches(Regex(findRule))
                } else {
                    it.name.contains(findRule)
                }
            }
        } else {
            treeWalk
        }
        fileList.forEach {
            if (!result) {
                return@forEach
            }
            if (it.isDirectory) {
                if (it.list().isNotEmpty()) {
                    result = finalFindListener.whenFindFolder(it)
                } else {
                    if (isDetectingEmptyFolder) {
                        result = finalFindListener.whenFindFile(it)
                    }
                }
            } else {
                result = finalFindListener.whenFindFile(it)
            }
        }
        return result
    }


    override fun setFinderListener(finderListener: FileFinderListener?) {
        this.fileFinderListener = finderListener
    }


}