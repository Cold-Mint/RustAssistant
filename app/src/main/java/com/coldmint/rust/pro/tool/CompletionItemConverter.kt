package com.coldmint.rust.pro.tool

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.dataBean.ValueTypeDataBean
import com.coldmint.rust.core.database.code.*
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.database.file.FileTable
import com.coldmint.rust.core.database.file.ValueTable
import com.coldmint.rust.core.interfaces.EnglishMode
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LocalVariableName
import com.coldmint.rust.core.web.Dynamic
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.edit.RustCompletionItem
import java.io.File
import java.lang.StringBuilder


/**
 * 自动完成对象转换器
 * @property isInitComplete Boolean
 * @property associatedTip String
 * @property section String
 * @property boxDrawable Drawable?
 * @property fileDrawable Drawable?
 * @property typeMap HashMap<String, ValueTypeInfo>
 * @property sourceFilePath String?
 * @property rootCodeName String?
 * @property modFolder String?
 * @property canUseFileConversion Boolean
 * @property valueTypeDao ValueTypeDao?
 */
object CompletionItemConverter {


    /**
     * 初始化
     * @param context Context
     */
    fun init(context: Context): CompletionItemConverter {
        if (!isInitComplete) {
            boxDrawable = context.getDrawable(R.drawable.box)
            fileDrawable = context.getDrawable(R.drawable.file)
            associatedTip = context.getString(R.string.associated_tip)
            section = context.getString(R.string.section)
            variableName = context.getString(R.string.variable_name)
            valueTypeDao = CodeDataBase.getInstance(context).getValueTypeDao()
            isInitComplete = true
            valueTypeList =
                FileDataBase.readValueTypeFile(FileDataBase.getDefaultValueFile(context))
        }
        return this
    }

    var valueTypeList: ArrayList<ValueTypeDataBean>? = null
    private var isInitComplete = false
    var associatedTip: String = ""
    var section: String = ""
    var boxDrawable: Drawable? = null
    var fileDrawable: Drawable? = null
    var variableName: String = ""

    private val typeMap: HashMap<String, ValueTypeInfo> by lazy {
        HashMap()
    }
    private var sourceFilePath: String? = null
    private var rootCodeName: String? = null
    private var modFolder: String? = null
    private var canUseFileConversion = false
    private var valueTypeDao: ValueTypeDao? = null
    private val valueToValueNameMap: HashMap<String, String> by lazy {
        HashMap()
    }
    private var isEnglishMode = false
        get() = AppSettings.getValue(AppSettings.Setting.EnglishEditingMode, false)


    /**
     * 设置源文件目录
     * @param sourceFilePath String
     */
    fun setSourceFilePath(sourceFilePath: String) {
        this.sourceFilePath = sourceFilePath
    }


    /**
     * 配置文件数据转换功能
     * @param sourceFilePath String 源文件夹
     * @param rootCodeName String root名称
     * @param modFolder String 模组文件夹
     */
    fun configurationFileConversion(
        sourceFilePath: String,
        rootCodeName: String,
        modFolder: String
    ) {
        val file = File(sourceFilePath)
        if (!file.isDirectory) {
            throw RuntimeException("源文件目录，必须是文件夹")
        }
        val modFile = File(modFolder)
        if (!modFile.isDirectory) {
            throw RuntimeException("模组目录，必须是文件夹")
        }
        this.sourceFilePath = sourceFilePath
        this.rootCodeName = rootCodeName
        this.modFolder = modFolder
        this.canUseFileConversion = true
    }


    /**
     * 节信息转换为自动完成对象
     * @param sectionInfo SectionInfo
     * @return RustCompletionItem
     */
    fun sectionInfoToCompletionItem(sectionInfo: SectionInfo): RustCompletionItem {
        if (!isInitComplete) {
            NullPointerException("没有初始化。")
        }
        val end = if (sectionInfo.needName) {
            "_"

        } else {
            "]"
        }
        val commit = if (isEnglishMode) {
            "[" + sectionInfo.code + end
        } else {
            "[" + sectionInfo.translate + end
        }
        val completionItem = if (isEnglishMode) {
            RustCompletionItem(
                sectionInfo.code,
                commit,
                section,
                boxDrawable
            )
        } else {
            RustCompletionItem(
                sectionInfo.translate,
                commit,
                section,
                boxDrawable
            )
        }
        completionItem.cursorOffset = if (sectionInfo.needName) {
            commit.length - 1
        } else {
            commit.length
        }
        return completionItem
    }

    /**
     * 本地变量转自动完成对象
     * @param localVariableName LocalVariableName
     * @param previousText String? 之前的内容(冒号后面的值)
     * @return RustCompletionItem
     */
    fun localVariableNameToCompletionItem(
        localVariableName: LocalVariableName,
        previousText: String? = null
    ): RustCompletionItem {
        val commit = if (previousText == null) {
            "\${" + localVariableName.name + "}"
        } else {
            previousText + "\${" + localVariableName.name + "}"
        }
        return RustCompletionItem(
            localVariableName.name,
            commit,
            variableName, boxDrawable
        )
    }

    /**
     * 转换代码信息到自动完成对象
     * @param codeInfo CodeInfo
     * @return RustCompletionItem
     */
    fun codeInfoToCompletionItem(codeInfo: CodeInfo): RustCompletionItem {
        if (!isInitComplete) {
            NullPointerException("没有初始化。")
        }
        val typeInfo = getValueType(codeInfo.type)
        val completionItem = if (isEnglishMode) {
            RustCompletionItem(
                codeInfo.code,
                codeInfo.code + typeInfo?.external,
                codeInfo.description, boxDrawable
            )
        } else {
            RustCompletionItem(
                codeInfo.translate,
                codeInfo.translate + typeInfo?.external,
                codeInfo.description, boxDrawable
            )
        }
//        val typeList = typeInfo?.list
//        if (typeList != null && typeList.isNotBlank()) {
//            val bundle = Bundle()
//            bundle.putString("list", typeList)
//            completionItem.extrasData = bundle
//        }
        val offset = typeInfo?.offset
        if (offset != null && offset.isNotBlank()) {
            //如果偏移不为空
            val head = "@length("
            val cursorOffset = if (offset.startsWith(head) && offset.endsWith(")")) {
                offset.subSequence(head.length, offset.length - 1).toString().toInt()
            } else {
                offset.toInt()
            }
            completionItem.cursorOffset = cursorOffset
        }
        return completionItem
    }

    /**
     * 获取值类型外部数据
     * @param type String
     * @return String
     */
    fun getValueType(type: String): ValueTypeInfo? {
        if (valueTypeDao != null) {
            if (typeMap.containsKey(type)) {
                return typeMap[type]
            }
            val valueTypeInfo = valueTypeDao!!.findTypeByType(type)
            if (valueTypeInfo != null) {
                typeMap[type] = valueTypeInfo
            }
            return valueTypeInfo
        } else {
            return null
        }
    }


    /**
     * 值数据转自动完成对象
     * @param valueTable ValueTable
     * @return CompletionItem
     */
    fun valueTableToCompletionItem(valueTable: ValueTable): RustCompletionItem {
        if (isInitComplete) {
            NullPointerException("没有初始化。")
        }
        var desc = valueTable.type
        if (valueToValueNameMap.containsKey(valueTable.type)) {
            desc = valueToValueNameMap[valueTable.type] ?: valueTable.type
        } else {
            if (valueTypeList != null) {
                for (value in valueTypeList!!) {
                    if (value.type == valueTable.type) {
                        desc = value.name
                        valueToValueNameMap[value.type] = value.name
                        break
                    }
                }
            }
        }
        return RustCompletionItem(valueTable.keyWord, desc, boxDrawable)
    }

    /**
     * 导航项目转自动完成对象
     * @param navigationItem NavigationItem
     * @return CompletionItem
     */
//    fun navigationItemToCompletionItem(navigationItem: NavigationItem): RustCompletionItem {
//        val name = SourceFile.getAbsoluteSectionName(navigationItem.label)
//        val type = SourceFile.getSectionType(navigationItem.label)
//        return RustCompletionItem(
//            name,
//            name,
//            type,
//            boxDrawable
//        )
//    }

    /**
     * 文件信息转自动完成对象
     * @param fileTable FileTable
     * @return CompletionItem
     */
    fun fileTableToCompletionItem(fileTable: FileTable): RustCompletionItem {
        if (!isInitComplete) {
            NullPointerException("没有初始化。")
        }
        val finalSourceFilePath = sourceFilePath ?: ""
        val finalModFolder = modFolder ?: ""
        val finalRootCodeName = rootCodeName ?: ""
        if (canUseFileConversion) {
            val drawable = when (fileTable.type) {
                "png", "jpg" -> {
                    Drawable.createFromPath(fileTable.path)
                }
                else -> {
                    fileDrawable
                }
            }
            val stringBuilder = StringBuilder()
            val relativePath =
                FileOperator.getRelativePath(
                    fileTable.path,
                    finalSourceFilePath,
                    finalModFolder
                )
            if (relativePath != null) {
                if (relativePath.contains("/")) {
                    stringBuilder.append(finalRootCodeName)
                    stringBuilder.append(':')
                    stringBuilder.append(relativePath.substring(1))
                } else {
                    stringBuilder.append(relativePath)
                }
            }
            val completionItem =
                RustCompletionItem(
                    fileTable.fileName,
                    stringBuilder.toString(),
                    fileTable.type, drawable
                )
            return completionItem
        } else {
            if (sourceFilePath == null) {
                throw NullPointerException("未设置源文件路径")
            }

            if (modFolder == null) {
                throw NullPointerException("未设置模组路径")
            }

            if (rootCodeName == null) {
                throw NullPointerException("未设置Root名称")
            }
            return RustCompletionItem(
                fileTable.fileName,
                fileTable.fileName,
                fileTable.type, fileDrawable
            )
        }
    }


}