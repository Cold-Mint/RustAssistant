package com.coldmint.rust.core.database.code

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Index
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.coldmint.rust.core.DataSet
import com.coldmint.rust.core.dataBean.dataset.*
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.tool.FileOperator
import com.google.gson.Gson
import java.util.concurrent.Executors

/**
 * @author Cold Mint
 * @date 2022/1/19 11:34
 */
@Database(
    entities = [ChainInspection::class, CodeInfo::class, SectionInfo::class, ValueTypeInfo::class, Version::class],
    version = 3,
    exportSchema = false
)
abstract class CodeDataBase : RoomDatabase() {
    companion object {
        private var instance: CodeDataBase? = null


        fun getInstance(
            context: Context,
            name: String = "codeDataTable",
            openNewDataBase: Boolean = false
        ): CodeDataBase {
            if (openNewDataBase && instance != null) {
                instance!!.close()
                instance =
                    Room.databaseBuilder(context.applicationContext, CodeDataBase::class.java, name)
                        .fallbackToDestructiveMigration()
                        .build()
                return instance!!
            }
            if (instance == null) {
                synchronized(FileDataBase::class.java)
                {
                    if (instance == null) {
                        instance =
                            Room.databaseBuilder(
                                context.applicationContext,
                                CodeDataBase::class.java,
                                name
                            ).fallbackToDestructiveMigration().build()
                    }
                }
            }
            return instance!!
        }

    }

    enum class ReadMode {
        Additional, Update, AppendOrUpdate, Delete, Copy
    }

    /**
     * 加载DataSet数据集
     * @param dataSet DataSet
     * @return Boolean
     */
    fun loadDataSet(dataSet: DataSet, readMode: ReadMode): Boolean {
        val datasetTag = "数据集加载"
        LogCat.d(datasetTag, "读取位于(" + dataSet.folder.absolutePath + ")数据集，方法" + readMode.name)
        val executorService = Executors.newSingleThreadExecutor()
        var result = false
        val future = executorService.submit {
            val gson = Gson()
            val manifest = dataSet.getDataBaseManifest()
            if (manifest == null) {
                LogCat.e(datasetTag, "读取数据集错误，清单文件不存在。")
                return@submit
            }

            //加载代码表数据
            LogCat.d(datasetTag, "加载代码表...")
            try {
                val codeData = FileOperator.readFile(dataSet.getAbsolutePath(manifest.tables.code))
                if (codeData != null) {
                    val codeDataBean =
                        gson.fromJson<CodeDataBean>(codeData, CodeDataBean::class.java)
                    when (readMode) {
                        ReadMode.Copy -> {
                            //清空数据库，并从源重新导入
                            getCodeDao().clearTable()
                            getCodeDao().insertAll(codeDataBean.data)
                        }
                        ReadMode.Additional -> {
                            //添加记录到数据库
                            codeDataBean.data.forEach {
                                val old = getCodeDao().findCodeByCode(it.code)
                                if (old == null) {
                                    getCodeDao().insert(it)
                                }
                            }
                        }
                        ReadMode.AppendOrUpdate -> {
                            //如果目标存在记录，更新它，否则添加它。
                            codeDataBean.data.forEach {
                                val old = getCodeDao().findCodeByCode(it.code)
                                if (old == null) {
                                    getCodeDao().insert(it)
                                } else {
                                    getCodeDao().update(it)
                                }
                            }
                        }
                        ReadMode.Delete -> {
                            //删除目标记录中和源记录相符的记录
                            codeDataBean.data.forEach {
                                getCodeDao().delete(it)
                            }
                        }
                        ReadMode.Update -> {
                            //更新和源记录相符的记录
                            codeDataBean.data.forEach {
                                val old = getCodeDao().findCodeByCode(it.code)
                                if (old != null) {
                                    getCodeDao().update(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is SQLiteConstraintException) {
                    val codeData =
                        FileOperator.readFile(dataSet.getAbsolutePath(manifest.tables.code))
                    if (codeData != null) {
                        e.printStackTrace()
                        val hashSet = HashSet<String>()
                        val s = StringBuilder()
                        val codeDataBean =
                            gson.fromJson<CodeDataBean>(codeData, CodeDataBean::class.java)
                        var num = 0
                        codeDataBean.data.forEach {
                            if (hashSet.contains(it.code)) {
                                s.append('\n')
                                s.append(it.code)
                                num++
                            } else {
                                hashSet.add(it.code)
                            }
                        }
                        LogCat.e(datasetTag, "读取代码表错误(主键约束)，因为" + num + "个元素重复。列表：" + s.toString())
                    } else {
                        e.printStackTrace()
                        LogCat.e(datasetTag, "读取代码表错误(主键约束)。" + e.toString())
                    }
                } else {
                    e.printStackTrace()
                    LogCat.e(datasetTag, "读取代码表错误。" + e.toString())
                }
            }

            //加载节表数据
            try {
                LogCat.d(datasetTag, "加载节表...")
                val sectionData =
                    FileOperator.readFile(dataSet.getAbsolutePath(manifest.tables.section))
                if (sectionData != null) {
                    val sectionDataBean =
                        gson.fromJson<SectionDataBean>(sectionData, SectionDataBean::class.java)
                    when (readMode) {
                        ReadMode.Copy -> {
                            //清空数据库，并从源重新导入
                            getSectionDao().clearTable()
                            getSectionDao().insertAll(sectionDataBean.data)
                        }
                        ReadMode.Additional -> {
                            //添加记录到数据库
                            sectionDataBean.data.forEach {
                                val old =
                                    getSectionDao().findSectionInfoByCodeNotCheckAvailability(it.code)
                                if (old == null) {
                                    getSectionDao().insert(it)
                                }
                            }
                        }
                        ReadMode.AppendOrUpdate -> {
                            //如果目标存在记录，更新它，否则添加它。
                            sectionDataBean.data.forEach {
                                val old =
                                    getSectionDao().findSectionInfoByCodeNotCheckAvailability(it.code)
                                if (old == null) {
                                    getSectionDao().insert(it)
                                } else {
                                    getSectionDao().update(it)
                                }
                            }
                        }
                        ReadMode.Delete -> {
                            //删除目标记录中和源记录相符的记录
                            sectionDataBean.data.forEach {
                                getSectionDao().delete(it)
                            }
                        }
                        ReadMode.Update -> {
                            //更新和源记录相符的记录
                            sectionDataBean.data.forEach {
                                val old =
                                    getSectionDao().findSectionInfoByCodeNotCheckAvailability(it.code)
                                if (old != null) {
                                    getSectionDao().update(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogCat.e(datasetTag, "读取节表错误。" + e.toString())
            }

            //值类型数据
            try {
                LogCat.d("数据集加载", "加载值表...")
                val valueTypeData =
                    FileOperator.readFile(dataSet.getAbsolutePath(manifest.tables.valueType))
                if (valueTypeData != null) {
                    val valueTypeDataBean =
                        gson.fromJson<ValueTypeDataBean>(
                            valueTypeData,
                            ValueTypeDataBean::class.java
                        )
                    when (readMode) {
                        ReadMode.Copy -> {
                            //清空数据库，并从源重新导入
                            getValueTypeDao().clearTable()
                            getValueTypeDao().insertAll(valueTypeDataBean.data)
                        }
                        ReadMode.Additional -> {
                            //添加记录到数据库
                            valueTypeDataBean.data.forEach {
                                val old = getValueTypeDao().findTypeByType(it.type)
                                if (old == null) {
                                    getValueTypeDao().insert(it)
                                }
                            }
                        }
                        ReadMode.AppendOrUpdate -> {
                            //如果目标存在记录，更新它，否则添加它。
                            valueTypeDataBean.data.forEach {
                                val old = getValueTypeDao().findTypeByType(it.type)
                                if (old == null) {
                                    getValueTypeDao().insert(it)
                                } else {
                                    getValueTypeDao().update(it)
                                }
                            }
                        }
                        ReadMode.Delete -> {
                            //删除目标记录中和源记录相符的记录
                            valueTypeDataBean.data.forEach {
                                getValueTypeDao().delete(it)
                            }
                        }
                        ReadMode.Update -> {
                            //更新和源记录相符的记录
                            valueTypeDataBean.data.forEach {
                                val old = getValueTypeDao().findTypeByType(it.type)
                                if (old != null) {
                                    getValueTypeDao().update(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogCat.e(datasetTag, "读取值表错误。" + e.toString())
            }


            //插入链式检查数据
            try {
                LogCat.d(datasetTag, "加载链式检查表...")
                val chainInspectionData =
                    FileOperator.readFile(dataSet.getAbsolutePath(manifest.tables.chainInspection))
                if (chainInspectionData != null) {
                    val chainInspectionDataBean = gson.fromJson<ChainInspectionDataBean>(
                        chainInspectionData,
                        ChainInspectionDataBean::class.java
                    )
                    when (readMode) {
                        ReadMode.Copy -> {
                            //清空数据库，并从源重新导入
                            getChainInspectionDao().clearTable()
                            getChainInspectionDao().insertAll(chainInspectionDataBean.data)
                        }
                        ReadMode.Additional -> {
                            //添加记录到数据库
                            chainInspectionDataBean.data.forEach {
                                val old = getChainInspectionDao().findChainInspectionById(it.id)
                                if (old == null) {
                                    getChainInspectionDao().insert(it)
                                }
                            }
                        }
                        ReadMode.AppendOrUpdate -> {
                            //如果目标存在记录，更新它，否则添加它。
                            chainInspectionDataBean.data.forEach {
                                val old = getChainInspectionDao().findChainInspectionById(it.id)
                                if (old == null) {
                                    getChainInspectionDao().insert(it)
                                } else {
                                    getChainInspectionDao().update(it)
                                }
                            }
                        }
                        ReadMode.Delete -> {
                            //删除目标记录中和源记录相符的记录
                            chainInspectionDataBean.data.forEach {
                                getChainInspectionDao().delete(it)
                            }
                        }
                        ReadMode.Update -> {
                            //更新和源记录相符的记录
                            chainInspectionDataBean.data.forEach {
                                val old = getChainInspectionDao().findChainInspectionById(it.id)
                                if (old != null) {
                                    getChainInspectionDao().update(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogCat.e(datasetTag, "读取链式检查表错误。" + e.toString())
            }


            //游戏版本数据
            try {
                LogCat.d("数据集加载", "加载版本表...")
                val versionData =
                    FileOperator.readFile(dataSet.getAbsolutePath(manifest.tables.gameVersion))
                if (versionData != null) {
                    val versionDataBean =
                        gson.fromJson<GameVersionDataBean>(
                            versionData,
                            GameVersionDataBean::class.java
                        )
                    when (readMode) {
                        ReadMode.Copy -> {
                            //清空数据库，并从源重新导入
                            getVersionDao().clearTable()
                            getVersionDao().insertAll(versionDataBean.data)
                        }
                        ReadMode.Additional -> {
                            //添加记录到数据库
                            versionDataBean.data.forEach {
                                val old = getVersionDao().findVersionByVersionName(it.versionName)
                                if (old == null) {
                                    getVersionDao().insert(it)
                                }
                            }
                        }
                        ReadMode.AppendOrUpdate -> {
                            //如果目标存在记录，更新它，否则添加它。
                            versionDataBean.data.forEach {
                                val old = getVersionDao().findVersionByVersionName(it.versionName)
                                if (old == null) {
                                    getVersionDao().insert(it)
                                } else {
                                    getVersionDao().update(it)
                                }
                            }
                        }
                        ReadMode.Delete -> {
                            //删除目标记录中和源记录相符的记录
                            versionDataBean.data.forEach {
                                getVersionDao().delete(it)
                            }
                        }
                        ReadMode.Update -> {
                            //更新和源记录相符的记录
                            versionDataBean.data.forEach {
                                val old = getVersionDao().findVersionByVersionName(it.versionName)
                                if (old != null) {
                                    getVersionDao().update(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogCat.e(datasetTag, "读取版本表错误。" + e.toString())
            }
            LogCat.d("数据集加载", "加载完成。")
            result = true
        }
        return if (future.get() == null) {
            result
        } else {
            false
        }
    }

    abstract fun getCodeDao(): CodeDao
    abstract fun getSectionDao(): SectionDao
    abstract fun getValueTypeDao(): ValueTypeDao
    abstract fun getVersionDao(): VersionDao
    abstract fun getChainInspectionDao(): ChainInspectionDao

}