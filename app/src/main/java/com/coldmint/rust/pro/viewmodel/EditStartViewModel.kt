package com.coldmint.rust.pro.viewmodel

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.coldmint.rust.core.OpenedSourceFile
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.base.BaseAndroidViewModel
import com.coldmint.rust.pro.tool.BookmarkManager
import java.io.File
import java.util.concurrent.Executors

/**
 * @author Cold Mint
 * @date 2022/1/28 16:39
 */
class EditStartViewModel(application: Application) : BaseAndroidViewModel(application) {

    val executorService = Executors.newCachedThreadPool()

    val bookmarkManager by lazy {
        BookmarkManager(application)
    }

    /**
     * 文件列表信息
     */
    val fileListLiveData: MutableLiveData<MutableList<File?>> by lazy {
        MutableLiveData<MutableList<File?>>()
    }

    /**
     * 加载状态
     */
    val loadStatusLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    /**
     * 当前加载的页面
     */
    val loadPathLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }


    /**
     * 初始化加载路径数据（仅在第一次有效）
     * @param nowOpen String
     */
    fun initLoadPathLiveData(nowOpen: String) {
        val oldValue = loadPathLiveData.value
        if (oldValue == null) {
            loadPathLiveData.value =
                FileOperator.getSuperDirectory(nowOpen)
        }
    }

    /**
     * 重载列表
     */
    fun reloadList() {
        val value = loadPathLiveData.value
        if (value != null) {
            loadList(value)
        }
    }


    /**
     * 加载列表
     * @param path String
     */
    fun loadList(path: String) {
        val pathValue = loadPathLiveData.value
        if (pathValue == null) {
            loadPathLiveData.value = path
        } else {
            if (path != pathValue) {
                throw RuntimeException("不要手动调用loadList方法，请修改loadPath使观察者更新。")
            }
        }
        executorService.submit {
            loadStatusLiveData.postValue(false)
            val file = File(path)
            if (file.exists() && file.isDirectory) {
                val fileList: MutableList<File?> = file.listFiles().toMutableList()
                if (path != Environment.getExternalStorageDirectory().absolutePath) {
                    fileList.add(0, null)
                }
                fileListLiveData.postValue(fileList)
                loadStatusLiveData.postValue(true)
            } else {
                loadStatusLiveData.postValue(false)
            }
        }
    }

}