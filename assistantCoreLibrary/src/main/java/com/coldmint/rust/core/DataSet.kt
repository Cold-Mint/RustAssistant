package com.coldmint.rust.core

import android.util.Log
import com.coldmint.rust.core.dataBean.dataset.DataBaseManifest
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.tool.FileOperator
import com.google.gson.Gson
import java.io.File

/**
 * 数据集类
 * @author Cold Mint
 * @date 2022/1/20 11:16
 */
class DataSet(val folder: File) {

    val manifestFileName = "DataBaseManifest.json"

    /**
     * 获取清单文件数据，若清单文件不存在返回null
     * @param gson 使用的Gson对象，若不传入则自动生成
     * @return DataBaseManifest?
     */
    fun getDataBaseManifest(gson: Gson? = null): DataBaseManifest? {
        val temGson = gson ?: Gson()
        val file = File(folder.absolutePath + "/" + manifestFileName)
        return if (file.exists()) {
            val data = FileOperator.readFile(file)
            temGson.fromJson(data, DataBaseManifest::class.java)
        } else {
            null;
        }
    }

    /**
     * 获取数据集名称
     * @return String
     */
    fun getDataSetName(gson: Gson? = null): String {
        val temGson = gson ?: Gson()
        val dataBaseManifest = getDataBaseManifest(temGson)
        return dataBaseManifest?.name ?: folder.name
    }

    /**
     * 获取绝对路径指向的文件
     * @param gson 使用的Gson对象，若不传入则自动生成
     * @param path String
     */
    fun getAbsolutePath(path: String): File {
        return File(folder.absolutePath + path)
    }

    /**
     * 更新数据集合（如果需要的话）
     * @param dataSet DataSet
     * @return 是否升级了
     */
    fun update(dataSet: DataSet): Boolean {
        val newDataSetManifest = dataSet.getDataBaseManifest()
        val thisManifest = getDataBaseManifest()
        if (newDataSetManifest == null) {
            LogCat.w(
                "数据集更新",
                "新清单不存在，无法升级"
            )
            return false
        }
        if (thisManifest == null) {
            //如果自身清单文件为空直接升级
            LogCat.w(
                "数据集更新",
                "自身清单不存在，已执行升级"
            )
            FileOperator.delete_files(folder)
            return FileOperator.removeFile(dataSet.folder, folder)
        }
        return if (newDataSetManifest.id == thisManifest.id) {
            if (newDataSetManifest.versionNumber > thisManifest.versionNumber) {
                LogCat.d(
                    "数据集更新",
                    "版本号不一致,执行更新(新版本" + newDataSetManifest.versionNumber + "旧版本:" + thisManifest.versionNumber + ")"
                )
                FileOperator.delete_files(folder)
                FileOperator.copyFile(dataSet.folder, folder)
            } else {
                LogCat.d(
                    "数据集更新",
                    "新的版本号小于或等于当前版本无需更新（新版本:" + newDataSetManifest.versionNumber + "旧版本:" + thisManifest.versionNumber + ")"
                )
                false
            }
        } else {
            LogCat.w(
                "数据集更新",
                "id不一致"
            )
            false
        }
    }


}