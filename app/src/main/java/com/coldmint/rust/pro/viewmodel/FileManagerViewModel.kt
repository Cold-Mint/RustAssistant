package com.coldmint.rust.pro.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.base.BaseViewModel
import kotlinx.coroutines.launch
import java.io.File

class FileManagerViewModel : BaseViewModel() {

    /**
     * 启动模式枚举类
     * 默认，选择目录，导出文件，选择文件
     */
    enum class StartType {
        DEFAULT, SELECT_DIRECTORY, EXPORT_FILE, SELECT_FILE
    }

    private var directs = Environment.getExternalStorageDirectory().absolutePath

    //根目录
    private var rootPath: String = directs

    /**
     * 当前打开的目录
     */
    val currentPathLiveData: MutableLiveData<String> by lazy {
        MutableLiveData(rootPath)
    }


    /**
     * 加载状态
     */
    val loadStateLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData(true)
    }

    /**
     * 文件列表数据
     */
    val fileListLiveData: MutableLiveData<MutableList<File?>> by lazy {
        MutableLiveData()
    }

    var startTypeData: StartType = StartType.DEFAULT

    /**
     * 设置根目录
     * @param path String?
     */
    fun setRootPath(path: String?) {
        rootPath = path ?: directs
    }

    /**
     * 加载文件列表
     * @param path String
     */
    fun loadFiles(path: String = rootPath) {
        viewModelScope.launch {
            loadStateLiveData.value = true
            val folder = File(path)
            if (!folder.exists()) {
                return@launch
            }
            val arrayList = ArrayList<File?>()
            Log.d("文件管理器", "当前路径" + path + "根路径" + rootPath + "添加返回" + (path != rootPath))
            if (path != rootPath) {
                //如果不是根目录添加返回
                arrayList.add(null)
            }
            folder.listFiles()?.forEach {
                arrayList.add(it)
            }
            fileListLiveData.value = arrayList
            loadStateLiveData.value = false
        }
    }

    /**
     * 返回上级目录
     */
    fun returnDirects() {
        currentPathLiveData.value =
            FileOperator.getSuperDirectory(currentPathLiveData.value ?: rootPath, rootPath)
    }

}