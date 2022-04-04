package com.coldmint.rust.core

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Handler
import android.os.Looper
import com.coldmint.rust.core.dataBean.TemplateDataBean
import com.coldmint.rust.core.dataBean.template.TemplateInfo
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.CodeInfo
import com.coldmint.rust.core.database.code.SectionInfo
import com.coldmint.rust.core.interfaces.GameSynchronizerListener
import com.coldmint.rust.core.interfaces.UnzipListener
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LineParser
import java.io.File
import java.util.concurrent.Executors
import java.util.zip.ZipEntry

/**
 * 游戏同步者
 * @property context Context 上下文环境
 * @property packageInfo PackageInfo 包信息（构造时要先检查，是否存在了程序）
 * @constructor
 */
class GameSynchronizer(val context: Context, val packageInfo: PackageInfo) {

    companion object {
        /**
         * 获取和此包关联的路径
         * @return File
         */
        fun getPackAgeFolder(context: Context, packageName: String): File {
            return File(context.filesDir.absolutePath + "/game/" + packageName)
        }
    }

    private val gameFolder: File by lazy {
        val file = File(context.filesDir.absolutePath + "/game/")
        if (!file.exists()) {
            file.exists()
        }
        file
    }

    /**
     * 获取包名指定的存放目录
     */
    private val folder by lazy {
        File(gameFolder.absolutePath + "/" + packageInfo.packageName)
    }

    private val appName by lazy {
        packageInfo.applicationInfo.loadLabel(context.packageManager).toString()
    }

    private val codeDao by lazy {
        CodeDataBase.getInstance(context).getCodeDao()
    }


    /**
     * 单位名映射(英文名,中文)
     */
    private val unitNameMap: HashMap<String, String> by lazy {
        HashMap()
    }

    /**
     * 次构造函数
     * @param context Context 上下文环境
     * @param string String 包名
     * @constructor
     */
    constructor(context: Context, string: String) : this(
        context,
        context.packageManager.getPackageInfo(string, 0)
    )


    /**
     * 导出Apk文件
     * @param exportPath String 导出路径
     * @return Boolean 是否导出成功
     */
    fun exportApk(exportPath: String): Boolean {
        val apkFile = File(packageInfo.applicationInfo.sourceDir)
        val exportFile = File(exportPath)
        if (exportFile.exists()) {
            exportFile.delete()
        }
        return FileOperator.copyFile(apkFile, exportFile)
    }


    /**
     * 读取单位文件
     * @param file File
     * @param gameSynchronizerListener GameSynchronizerListener
     * @param handler Handler
     * @param zipEntry ZipEntry
     */
    private fun readUnitsFile(
        file: File,
        gameSynchronizerListener: GameSynchronizerListener,
        handler: Handler,
        zipEntry: ZipEntry
    ) {
        val data = FileOperator.readFile(file)
        if (data != null) {
            val head = "units."
            val end = ".name"
            val s = "="
            val description = appName + packageInfo.versionName
            val lineParser = LineParser(data)
            lineParser.analyse { lineNum, lineData, isEnd ->
                gameSynchronizerListener.whenChanged(
                    handler,
                    zipEntry.name + lineNum.toString() + lineData
                )
                val headIndex = lineData.indexOf(head)
                if (headIndex > -1) {
                    val endIndex = lineData.indexOf(end)
                    if (endIndex > -1) {
                        val sIndex = lineData.indexOf(s, endIndex + end.length)
                        if (sIndex > -1) {
                            val key =
                                lineData.subSequence(
                                    headIndex + head.length,
                                    endIndex
                                ).toString()
                            val value =
                                lineData.subSequence(
                                    sIndex + s.length,
                                    lineData.length
                                ).toString()
                            val codeInfo = CodeInfo(
                                key,
                                value,
                                description,
                                "internalUnits",
                                1,
                                section = description
                            )
                            unitNameMap[key] = value
                            val old = codeDao.findCodeByCode(key)
                            if (old == null) {
                                codeDao.insert(codeInfo)
                            } else {
                                codeDao.update(codeInfo)
                            }
                            val section = SectionInfo(
                                description,
                                description,
                                false,
                                true
                            )
                            val oldSection = CodeDataBase.getInstance(context)
                                .getSectionDao()
                                .findSectionInfoByCode(section.code)
                            if (oldSection == null) {
                                CodeDataBase.getInstance(context)
                                    .getSectionDao().insert(section)
                            } else {
                                CodeDataBase.getInstance(context)
                                    .getSectionDao().update(section)
                            }
                        }
                    }
                }
                true
            }
        }
    }

    /**
     * 生成数据
     * @param templateFolder String 模板路径
     * @param gameSynchronizerListener GameSynchronizerListener 同步监听器
     */
    fun generateData(templateFolder: String, gameSynchronizerListener: GameSynchronizerListener) {
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executorService.submit {
            val templatePackage = TemplatePackage(File(templateFolder + packageInfo.packageName))

            if (folder.exists()) {
                FileOperator.delete_files(folder)
                folder.mkdirs()
            } else {
                folder.mkdirs()
            }
            unitNameMap.clear()
            val defaultIcon = "/icon.png"
            var hasIcon = false
            val sourceFileList = ArrayList<SourceFile>()
            var canUse = false
            CompressionManager.instance.unzip(File(packageInfo.applicationInfo.sourceDir), folder,
                object : UnzipListener {
                    override fun whenUnzipFile(zipEntry: ZipEntry, file: File): Boolean {
                        gameSynchronizerListener.whenChanged(handler, zipEntry.name)
                        //是简体中文翻译文件
                        if (zipEntry.name.endsWith("Strings_zh_cn.properties")) {
                            canUse = true
                            readUnitsFile(file, gameSynchronizerListener, handler, zipEntry)
                        } else if (zipEntry.name.endsWith(".ini")) {
                            sourceFileList.add(SourceFile(file))
                        } else if (zipEntry.name.endsWith("icon.png")) {
                            hasIcon =
                                FileOperator.copyFile(file, templatePackage.getFile(defaultIcon))
                        }
                        return true
                    }

                    override fun whenUnzipFolder(zipEntry: ZipEntry, folder: File): Boolean {
                        gameSynchronizerListener.whenChanged(handler, zipEntry.name)
                        return true
                    }

                    override fun whenUnzipComplete(result: Boolean) {
                        if (!result || !canUse) {
                            FileOperator.delete_files(folder)
                        }
                        //如果有源文件那么添加到模板包内
                        if (sourceFileList.isNotEmpty()) {
                            templatePackage.create(
                                TemplateInfo(
                                    AppOperator.getAppVersionNum(context),
                                    context.getString(R.string.template_default_description),
                                    packageNameToDeveloper(packageInfo.packageName),
                                    appName,
                                    context.getString(R.string.template_update),
                                    packageInfo.versionName,
                                    packageInfo.versionCode
                                )
                            )

                            val defaultActionList = ArrayList<TemplateDataBean.Action>()
                            val inputType = "input"
                            defaultActionList.add(createTemplateAction("name", "core", inputType))
                            defaultActionList.add(createTemplateAction("maxHp", "core", inputType))
                            defaultActionList.add(createTemplateAction("price", "core", inputType))
//                        defaultActionList.add(createTemplateAction("price", "core", inputType))
//                        defaultActionList.add(createTemplateAction("price", "core", inputType))

                            sourceFileList.forEach { sourceFile ->
                                val fileData = FileOperator.readFile(sourceFile.file) ?: ""
                                val dataBean =
                                    TemplateDataBean(
                                        name = unitNameMap[sourceFile.readValue("name")]
                                            ?: sourceFile.file.name,
                                        data = fileData,
                                        language = "zh"
                                    )
                                if (hasIcon) {
                                    dataBean.icon = defaultIcon
                                }
                                val action = ArrayList<TemplateDataBean.Action>()
                                defaultActionList.forEach {
                                    if (fileData.contains("\n${it.key}:")) {
                                        action.add(it)
                                    }
                                }
                                if (action.isNotEmpty()) {
                                    dataBean.action = action
                                }
                                val tagFile =
                                    templatePackage.getFile(
                                        "/" + FileOperator.getPrefixName(
                                            sourceFile.file
                                        ) + ".json"
                                    )
                                FileOperator.writeFile(
                                    tagFile,
                                    templatePackage.gson.toJson(dataBean)
                                )

                            }
                        } else {
                            FileOperator.delete_files(templatePackage.directest)
                        }

                        handler.post {
                            gameSynchronizerListener.whenCompleted(canUse)
                        }
                    }

                })

        }
    }

    /**
     * 包名转开发者名
     * @param packageName String
     * @return String
     */
    fun packageNameToDeveloper(packageName: String): String {
        var result = packageName
        val index1 = packageName.indexOf('.')
        if (index1 > -1) {
            val index2 = packageName.indexOf('.', index1 + 1)
            if (index2 > -1) {
                result = packageName.substring(index1 + 1, index2)
            }
        }
        return result
    }

    /**
     * 创建模板活动
     * 用于部分由程序自动生成的模板
     * @param key String
     * @param section String
     * @param type String
     */
    fun createTemplateAction(key: String, section: String, type: String): TemplateDataBean.Action {
        val nameInfo = codeDao.findCodeByCode(key)
        return TemplateDataBean.Action(
            key,
            nameInfo?.translate ?: key,
            section,
            key + "-" + section,
            type
        )
    }

}