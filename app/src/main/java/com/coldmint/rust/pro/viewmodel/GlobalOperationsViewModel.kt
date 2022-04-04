package com.coldmint.rust.pro.viewmodel

import androidx.lifecycle.MutableLiveData
import com.coldmint.rust.core.interfaces.FileFinderListener
import com.coldmint.rust.core.tool.FileFinder2
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.base.BaseViewModel
import java.io.File
import java.util.concurrent.Executors

/**
 * 全局操作viewModel
 */
class GlobalOperationsViewModel : BaseViewModel() {

    private var modPath: String? = null


    private val fileFinder2 by lazy {
        val o = FileFinder2(File(modPath!!))
        o.findMode = true
        o
    }

    private val executors by lazy {
        Executors.newSingleThreadExecutor()
    }

    val fileListLiveData: MutableLiveData<MutableList<File>> by lazy {
        MutableLiveData()
    }


    /**
     * 操作类型
     */
    enum class OperationType {
        Replace, BeginningAdditional, EndingAdditional
    }

    /**
     * 设置模组路径
     * @param path String
     */
    fun setModPath(path: String) {
        this.modPath = path
    }

    /**
     * 查找文件
     * @param findRule String
     * @param asRe Boolean
     */
    fun findFile(findRule: String, asRe: Boolean) {
        executors.submit {
            val fileList = ArrayList<File>()
            fileFinder2.asRe = asRe
            fileFinder2.findRule = findRule
            fileFinder2.setFinderListener(object : FileFinderListener {
                override fun whenFindFile(file: File): Boolean {
                    fileList.add(file)
                    return true
                }

                override fun whenFindFolder(folder: File): Boolean {
                    return true
                }
            })
            fileFinder2.onStart()
            fileListLiveData.postValue(fileList)
        }
    }


    /**
     * 操作文件方法
     * @param operationType OperationType
     * @param action1 String
     * @param action2 String
     */
    fun operationFile(operationType: OperationType, action1: String, action2: String) {
        fileListLiveData.value?.forEach {
            val data = FileOperator.readFile(it)
            if (data != null) {
                when (operationType) {
                    OperationType.Replace -> {
                        val newData = data.replace(action1, action2)
                        FileOperator.writeFile(it, newData)
                    }
                    OperationType.EndingAdditional -> {
                        val newData = data + "\n" + action1
                        FileOperator.writeFile(it, newData)
                    }
                    OperationType.BeginningAdditional -> {
                        val newData = action1 + "\n" + data
                        FileOperator.writeFile(it, newData)
                    }
                }
            }
        }
    }


}