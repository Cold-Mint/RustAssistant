package com.coldmint.rust.pro.livedata

import androidx.lifecycle.LiveData
import com.coldmint.rust.core.OpenedSourceFile
import java.util.*

/**
 * 打开的文件信息
 * @author Cold Mint
 * @date 2022/1/27 23:24
 */
class OpenedSourceFileListLiveData : LiveData<LinkedList<OpenedSourceFile>>() {
    private val linkedList by lazy {
        LinkedList<OpenedSourceFile>()
    }

    /**
     * 获取值
     * @return linkedList<OpenedSourceFile>
     */
    override fun getValue(): LinkedList<OpenedSourceFile> {
        return linkedList
    }


    /**
     * 添加元素到集合
     * @param openedSourceFile OpenedSourceFile
     * @return Int 返回-1，添加成功，其他为元素位置
     */
    fun add(openedSourceFile: OpenedSourceFile): Int {
        val index = linkedList.indexOf(openedSourceFile)
        if (index == -1) {
            linkedList.add(openedSourceFile)
            postValue(linkedList)
        }
        return index
    }

    /**
     * 通知观察者更新数据
     */
    fun refresh(){
        postValue(linkedList)
    }

    /**
     * 获取源文件对象
     * @param index Int 位置
     * @return OpenedSourceFile 源文件
     */
    fun getOpenedSourceFile(index: Int): OpenedSourceFile {
        return linkedList[index]
    }


    /**
     * 获取某个项目位置不存在返回-1
     * @param openedSourceFile OpenedSourceFile 对象
     * @return Int
     */
    fun indexOf(openedSourceFile: OpenedSourceFile): Int {
        return linkedList.indexOf(openedSourceFile)
    }

    /**
     * 移除值
     * @param openedSourceFile OpenedSourceFile
     */
    fun remove(openedSourceFile: OpenedSourceFile) {
        linkedList.remove(openedSourceFile)
        postValue(linkedList)
    }
}