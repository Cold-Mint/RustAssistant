package com.coldmint.rust.core.database.file

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.dataBean.ValueTypeDataBean
import com.coldmint.rust.core.tool.FileOperator
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import kotlin.collections.ArrayList

/**
 * @author Cold Mint
 * @date 2022/1/14 13:10
 */
@Database(
    entities = [FileTable::class, HistoryRecord::class, ValueTable::class],
    version = 1,
    exportSchema = false
)
abstract class FileDataBase : RoomDatabase() {

    companion object {
        private var instance: FileDataBase? = null

        fun getInstance(
            context: Context,
            name: String,
            openNewDataBase: Boolean = false
        ): FileDataBase {
            if (openNewDataBase && instance != null) {
                instance!!.close()
                instance =
                    Room.databaseBuilder(context, FileDataBase::class.java, name)
                        .fallbackToDestructiveMigration().build()
                return instance!!
            }
            if (instance == null) {
                synchronized(FileDataBase::class.java)
                {
                    if (instance == null) {
                        instance =
                            Room.databaseBuilder(context, FileDataBase::class.java, name)
                                .fallbackToDestructiveMigration().build()
                    }
                }
            }
            return instance!!
        }


        /**
         * 从文件创建-文件信息
         * @param file File
         * @return FileTable
         */
        fun createFileInfoFromFile(file: File): FileTable {
            return FileTable(
                file.absolutePath,
                FileOperator.getPrefixName(file),
                FileOperator.getMD5(file) ?: "",
                FileOperator.getFileType(file)
            )
        }

        val methodCollection =
            arrayOf("@method readValue()", "@method absoluteSectionName()", "@method fileName")
        const val scopeFilePath = "filePath"
        const val scopeGlobal = "global"
        const val scopeThisFile = "thisFile"

        /**
         * 获取默认值文件位置
         *
         * @param context 上下文环境
         * @return 文件对象
         */
        fun getDefaultValueFile(context: Context): File {
            return File(context.filesDir.absolutePath + "/values.json")
        }


        /**
         * 读取值类型文件
         *
         * @param valueTypeFile 值类型文件
         * @return 成功返回  [ValueTypeDataBean] 值类型数据集合，失败返回null
         */
        fun readValueTypeFile(valueTypeFile: File): ArrayList<ValueTypeDataBean>? {
            val gson = Gson()
            val valueTypeDataBeans = ArrayList<ValueTypeDataBean>()
            try {
                val array = JSONArray(FileOperator.readFile(valueTypeFile))
                val len = array.length()
                if (len > 0) {
                    var index = 0
                    while (index < len) {
                        val `object` = array.getJSONObject(index)
                        valueTypeDataBeans.add(
                            gson.fromJson(
                                `object`.toString(),
                                ValueTypeDataBean::class.java
                            )
                        )
                        index++
                    }
                }
                return valueTypeDataBeans
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }
    }

    //可用的文件列表（需要分析的内容）
    val availableValueTypes: ArrayList<ValueTypeDataBean> by lazy {
        ArrayList<ValueTypeDataBean>()
    }

    //保留旧的值类型json配置
    private var oldData: String? = null

    /**
     * 初始化可用的值类型数据集合
     * @param context Context
     * @return ArrayList<ValueTypeDataBean>?
     */
    fun initAvailableValueTypeFiles(context: Context) {
        val file = getDefaultValueFile(context)
        val newData = FileOperator.readFile(file)
        //如果数据发生了改变，那么重新读取数据集合
        if (newData != oldData) {
            availableValueTypes.clear()
            val list = readValueTypeFile(file)
            if (list != null && list.isNotEmpty()) {
                list.forEach {
                    if (it.scope == scopeGlobal || it.scope == scopeFilePath) {
                        availableValueTypes.add(it)
                    }
                }
                oldData = newData
            }
            oldData = ""
        }
    }

    /**
     * 从一个源文件分析值并到数据库
     * @param context Context
     * @param sourceFile SourceFile
     */
    fun addValuesFromSourceFile(context: Context, sourceFile: SourceFile) {
        //如果没有缓存任何文件的数据，那么初始化可用的值类型数据列表
        if (oldData == null) {
            initAvailableValueTypeFiles(context)
        }
        if (availableValueTypes.isNotEmpty()) {
            availableValueTypes.forEach { valueTypeDataBean ->
                val from = sourceFile.file.absolutePath
                if (valueTypeDataBean.data.startsWith("@method")) {
                    //如果有函数调用
                    if (valueTypeDataBean.data.startsWith("@method readValue(") && valueTypeDataBean.data.endsWith(
                            ")"
                        )
                    ) {
                        val key = valueTypeDataBean.data.subSequence(
                            "@method readValue(".length,
                            valueTypeDataBean.data.length - ")".length
                        ).toString()
                        val value = sourceFile.readValue(key)
                        if (value != null) {
                            addValueTableIfNeed(ValueTable(value, valueTypeDataBean.type, from))
                        }
                    } else if (valueTypeDataBean.data.startsWith("@method absoluteSectionName(") && valueTypeDataBean.data.endsWith(
                            ")"
                        )
                    ) {
                        val key = valueTypeDataBean.data.subSequence(
                            "@method absoluteSectionName(".length,
                            valueTypeDataBean.data.length - ")".length
                        ).toString()
                        sourceFile.allSection.iterator().forEach {
                            val t = SourceFile.getSectionType(it)
                            if (t == key) {
                                //如果当前节类型与规定的类型相等
                                val value = SourceFile.getAbsoluteSectionName(it)
                                addValueTableIfNeed(
                                    ValueTable(
                                        value,
                                        valueTypeDataBean.type,
                                        from
                                    )
                                )
                            }
                        }
                    } else if (valueTypeDataBean.data == "@method fileName") {
                        addValueTableIfNeed(
                            ValueTable(
                                sourceFile.file.name,
                                valueTypeDataBean.type,
                                from
                            )
                        )
                    }
                } else {
                    //是否满足正则
                    valueTypeDataBean.data.toRegex().findAll(sourceFile.text).forEach {
                        addValueTableIfNeed(ValueTable(it.value, valueTypeDataBean.type, from))
                    }

                }
            }
        }
    }

    /**
     * 插入或更新值数据
     * @param valueTable ValueTable
     */
    private fun addValueTableIfNeed(valueTable: ValueTable) {
        val old = getValueDao().findValueByKey(valueTable.keyWord)
        if (old == null) {
            getValueDao().insert(valueTable)
        } else {
            getValueDao().update(valueTable)
        }
    }


    abstract fun getValueDao(): ValueDao

    abstract fun getFileInfoDao(): FileInfoDao

    abstract fun getHistoryDao(): HistoryDao
}