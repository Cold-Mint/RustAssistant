package com.coldmint.rust.pro.viewmodel

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseViewModel
import com.coldmint.rust.pro.tool.AppSettings
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

    /**
     * 文件排序方式
     */
    enum class SortType {
        BY_NAME, BY_SIZE, BY_TYPE, BY_LAST_MODIFIED
    }

    private var directs = Environment.getExternalStorageDirectory().absolutePath

    //根目录
    private var rootPath: String = directs

    /**
     * 当前打开的目录(获取目录的话，请使用[FileManagerViewModel.getCurrentPath]方法])
     */
    val currentPathLiveData: MutableLiveData<String> by lazy {
        MutableLiveData(rootPath)
    }

    /**
     * 文件排序方式
     */
    val sortTypeLiveData: MutableLiveData<SortType> by lazy {
        MutableLiveData(SortType.BY_NAME)
    }

    /**
     * 从设置中读取排序方式
     */
    fun loadSortType(context: Context) {
        val appSettings = AppSettings.getInstance(context)
        //从设置中读取排序方式
        val sortType = appSettings.getValue(
            AppSettings.Setting.FileSortType,
            context.getString(R.string.setting_file_list_action_sort_by_name)
        )
        when (sortType) {
            context.getString(R.string.setting_file_list_action_sort_by_name) -> {
                sortTypeLiveData.value = SortType.BY_NAME
            }
            context.getString(R.string.setting_file_list_action_sort_by_last_modified) -> {
                sortTypeLiveData.value = SortType.BY_LAST_MODIFIED
            }
            context.getString(R.string.setting_file_list_action_sort_by_size) -> {
                sortTypeLiveData.value = SortType.BY_SIZE
            }
            context.getString(R.string.setting_file_list_action_sort_by_type) -> {
                sortTypeLiveData.value = SortType.BY_TYPE
            }
            else -> {
                sortTypeLiveData.value = SortType.BY_NAME
            }
        }
    }

    /**
     * 保存排序值到设置
     * @param context Context
     * @return Boolean
     */
    fun saveSortType(context: Context):Boolean {
        val appSettings = AppSettings.getInstance(context)
        val value =
            sortTypeLiveData.value ?: SortType.BY_NAME
        val text = when (value) {
            SortType.BY_NAME -> {
                context.getString(R.string.setting_file_list_action_sort_by_name)
            }
            SortType.BY_LAST_MODIFIED -> {
                context.getString(R.string.setting_file_list_action_sort_by_last_modified)
            }
            SortType.BY_SIZE -> {
                context.getString(R.string.setting_file_list_action_sort_by_size)
            }
            SortType.BY_TYPE -> {
                context.getString(R.string.setting_file_list_action_sort_by_type)
            }
            else -> {
                context.getString(R.string.setting_file_list_action_sort_by_name)
            }
        }
       return appSettings.setValue(AppSettings.Setting.FileSortType,text)
    }

    /**
     * 获取当前打开的目录
     * @return String
     */
    fun getCurrentPath(): String {
        return currentPathLiveData.value ?: rootPath
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
     * @param path String 默认加载根目录
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