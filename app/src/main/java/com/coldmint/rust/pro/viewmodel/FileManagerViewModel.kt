package com.coldmint.rust.pro.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseViewModel
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.BookmarkManager
import com.yalantis.ucrop.util.FileUtils.getPath
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

    //附加数据
    var additionalData: String? = null

    private lateinit var bookmarkManager: BookmarkManager

    /**
     * 初始化书签管理器
     * @param context Context
     * @return Boolean 返回是否初始化成功
     */
    fun initBookmarkManager(context: Context): Boolean {
        return if (!this::bookmarkManager.isInitialized) {
            bookmarkManager = BookmarkManager(context)
            bookmarkManager.load()
        } else {
            false
        }
    }


    /**
     * 获取书签管理器
     * @return BookmarkManager
     */
    fun getBookmarkManager(): BookmarkManager {
        return bookmarkManager
    }

    /**
     * 文件排序方式 名称 大小 类型 时间
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
        //从设置中读取排序方式
        val sortType = AppSettings.getValue(
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
    fun saveSortType(context: Context): Boolean {
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
        return AppSettings.setValue(AppSettings.Setting.FileSortType, text)
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
     * 获取Root目录
     * @return String
     */
    fun getRootPath(): String {
        return rootPath
    }

    /**
     * 解析文件路径
     *
     * @param context 上下文环境
     * @param uri  定位符
     * @return 成功返回文件路径，失败返回null
     */
    fun parseFilePath(context: Context, uri: Uri?): String? {
        return try {
            var chooseFilePath: String? = null
            if ("file".equals(uri!!.scheme, ignoreCase = true)) { //使用第三方应用打开
                chooseFilePath = uri.path
                return chooseFilePath
            }
            chooseFilePath = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) { //4.4以后
                getPath(context, uri)
            } else { //4.4以下下系统调用方法
                getRealPathFromURI(context, uri)
            }
            return chooseFilePath
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取uri的绝对路径
     *
     * @param context    上下文环境
     * @param contentUri uri
     * @return 文件路径
     */
    private fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
        if (null != cursor && cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
            cursor.close()
        }
        return res
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