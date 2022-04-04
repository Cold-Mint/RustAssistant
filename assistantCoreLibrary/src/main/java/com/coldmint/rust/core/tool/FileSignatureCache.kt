package com.coldmint.rust.core.tool

import android.content.Context
import org.json.JSONObject
import org.json.JSONException
import java.io.File

//文件签名缓存器
class FileSignatureCache(private val mContext: Context, private val mName: String) {
    //文件缓存映射
    private val mCacheFile: File
    private var mJsonObject: JSONObject? = null
    var mRootFolder: File? = null

    /**
     * 是否为加载模式（文件存在返回真，不存在返回假）
     *
     * @return
     */
    val isLoadMode: Boolean
        get() = mCacheFile.exists()

    /**
     * 设置根路径，用于计算绝对路径
     *
     * @param rootFolder
     */
    fun setRootFolder(rootFolder: File?) {
        mRootFolder = rootFolder
    }

    /**
     * 向缓存器内添加文件
     *
     * @param file 文件
     * @return 是否添加成功
     */
    fun putFile(file: File): Boolean {
        return try {
            val finalRoot = mRootFolder
            if (finalRoot == null) {
                mJsonObject!!.put(file.absolutePath, FileOperator.getMD5(file))
            } else {
                val relativePath = FileOperator.getRelativePath(file, finalRoot)
                if (relativePath != null) {
                    if (relativePath.isEmpty()) {
                        mJsonObject!!.put(file.absolutePath, FileOperator.getMD5(file))
                    } else {
                        mJsonObject!!.put(relativePath, FileOperator.getMD5(file))
                    }
                } else {
                    return false
                }
            }
            true
        } catch (e: JSONException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取文件签名
     *
     * @param file 文件
     * @return 成功，返回文件签名。失败返回null
     */
    fun getSign(file: File): String? {
        val value = file.absolutePath
        return if (mJsonObject!!.has(value)) {
            try {
                mJsonObject!!.getString(value)
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    /**
     * 此文件是否改变
     *
     * @param file 文件
     * @return 没有记录返回真，读取失败返回真，文件与签名相同返回假，不同返回真。
     */
    fun isChange(file: File): Boolean {
        val value = file.absolutePath
        val now = FileOperator.getMD5(file)
        return if (mJsonObject!!.has(value)) {
            try {
                val sign = mJsonObject!!.getString(value)
                now != sign
            } catch (e: JSONException) {
                e.printStackTrace()
                true
            }
        } else {
            true
        }
    }

    /**
     * 删除缓存文件
     *
     * @return 是否删除成功
     */
    fun delete(): Boolean {
        return mCacheFile.delete()
    }

    /**
     * 保存缓存文件
     *
     * @return 是否保存成功
     */
    fun save(): Boolean {
        return try {
            FileOperator.writeFile(mCacheFile, mJsonObject!!.toString(4))
        } catch (e: JSONException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 创建文件缓存器
     *
     * @param name 缓存器名
     */
    init {
        val cacheFolder = File(mContext.cacheDir.toString() + "/FileSign")
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs()
        }
        mCacheFile = File("$cacheFolder/$mName.json")
        if (!mCacheFile.parentFile.exists()) {
            mCacheFile.parentFile.mkdirs()
        }
        mJsonObject = if (mCacheFile.exists()) {
            val fileText = FileOperator.readFile(mCacheFile)
            try {
                JSONObject(fileText)
            } catch (e: JSONException) {
                e.printStackTrace()
                JSONObject()
            }
        } else {
            JSONObject()
        }
    }
}