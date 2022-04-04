package com.coldmint.rust.pro.adapters

import android.widget.BaseExpandableListAdapter
import android.view.LayoutInflater
import com.kongzue.stacklabelview.interfaces.OnLabelClickListener
import android.view.ViewGroup
import com.coldmint.rust.pro.R
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.CodeInfo
import com.coldmint.rust.core.database.code.SectionInfo
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.databinding.CodeTableGroupBinding
import com.coldmint.rust.pro.databinding.CodeTableItemBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import java.util.concurrent.Executors

class CodeTableAdapter(
    val context: Context,
    private val group: List<SectionInfo>,
    private val itemList: List<List<CodeInfo>>
) : BaseExpandableListAdapter() {

    private val layoutInflater = LayoutInflater.from(context)

    private var versionMap: HashMap<Int, String>? = null
    private var typeNameMap: HashMap<String, String>? = null
    private var sectionMap: HashMap<String, String>? = null
    private val executorService by lazy {
        Executors.newSingleThreadExecutor()
    }
    private val lineParser = LineParser()

    //Label点击事件
    var labelFunction: ((Int, View, String) -> Unit)? = null
    private val developerMode by lazy {
        AppSettings.getInstance(context)
            .getValue(AppSettings.Setting.DeveloperMode, false)
    }

    init {
        lineParser.symbol = ","
    }


    /**
     * 节名映射
     * @param sectionMap HashMap<String, String>
     */
    fun setSectionMap(sectionMap: HashMap<String, String>) {
        this.sectionMap = sectionMap
    }


    /**
     * 设置类型名称映射
     * @param typeNameMap HashMap<String, String>?
     */
    fun setTypeNameMap(typeNameMap: HashMap<String, String>?) {
        this.typeNameMap = typeNameMap
    }

    /**
     * 设置版本映射
     * @param versionMap HashMap<Int, String>?
     */
    fun setVersionMap(versionMap: HashMap<Int, String>?) {
        this.versionMap = versionMap
    }

    override fun getGroupCount(): Int {
        return group.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return itemList[groupPosition].size
    }

    override fun getGroup(groupPosition: Int): Any {
        return group[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return itemList[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val resultView: CodeTableGroupBinding =
            CodeTableGroupBinding.inflate(layoutInflater, parent, false)
        val info =
            String.format(
                context.getString(R.string.filenum),
                itemList[groupPosition].size
            )
        resultView.nameView.text = group[groupPosition].translate
        resultView.numView.text = info
        return resultView.root
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val resultView: CodeTableItemBinding =
            CodeTableItemBinding.inflate(layoutInflater, parent, false)
        val codeInfo = itemList[groupPosition][childPosition]
        resultView.belongStackLabelView.onLabelClickListener = OnLabelClickListener { index, v, s ->
            labelFunction?.invoke(index, v, s)
        }
        resultView.descriptionView.text = codeInfo.description
        resultView.descriptionView.setOnClickListener {
            GlobalMethod.copyText(context, codeInfo.description, it)
        }
        resultView.titleView.text = codeInfo.translate
        resultView.titleView.setOnClickListener {
            GlobalMethod.copyText(context, codeInfo.translate, it)
        }
        resultView.subTitleView.text = codeInfo.code
        resultView.subTitleView.setOnClickListener {
            GlobalMethod.copyText(context, codeInfo.code, it)
        }
        resultView.valueTypeView.text = typeNameMap?.get(codeInfo.type) ?: codeInfo.type
        val list = ArrayList<String>()
        lineParser.text = codeInfo.section
        lineParser.analyse { lineNum, lineData, isEnd ->
            list.add(sectionMap?.get(lineData) ?: lineData)
        }
        resultView.belongStackLabelView.isVisible = list.isNotEmpty()
        resultView.belongStackLabelView.labels = list
        resultView.valueTypeView.setOnClickListener {
            val handler = Handler(Looper.getMainLooper())
            executorService.submit {
                val codeDataBase = CodeDataBase.getInstance(context)
                val typeInfo = codeDataBase.getValueTypeDao().findTypeByType(codeInfo.type)
                if (typeInfo == null) {
                    handler.post {
                        handler.post {
                            MaterialDialog(context).show {
                                title(text = codeInfo.type).message(
                                    text = String.format(
                                        context.getString(
                                            R.string.unknown_type
                                        ), codeInfo.type
                                    )
                                )
                                    .positiveButton(R.string.dialog_ok)
                            }
                        }
                    }
                } else {
                    if (developerMode) {
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("介绍:")
                        stringBuilder.append(typeInfo.describe)
                        stringBuilder.append("\n附加信息:")
                        stringBuilder.append(typeInfo.external)
                        stringBuilder.append("\n关联的自动提示:")
                        stringBuilder.append(typeInfo.list)
                        stringBuilder.append("\n光标偏差:")
                        stringBuilder.append(typeInfo.offset)
                        stringBuilder.append("\n标签:")
                        stringBuilder.append(typeInfo.tag)
                        stringBuilder.append("\n数据规则:")
                        stringBuilder.append(typeInfo.rule)
                        handler.post {
                            MaterialDialog(context).show {
                                title(text = typeInfo.name + "(开发者模式)").message(text = stringBuilder.toString())
                                    .positiveButton(R.string.dialog_ok)
                            }
                        }
                    } else {
                        if (typeInfo.describe == "@search(code)") {
                            handler.post {
                                MaterialDialog(context).show {
                                    title(text = typeInfo.name).message(text = codeInfo.description)
                                        .positiveButton(R.string.dialog_ok)
                                }
                            }
                        } else {
                            handler.post {
                                MaterialDialog(context).show {
                                    title(text = typeInfo.name).message(text = typeInfo.describe)
                                        .positiveButton(R.string.dialog_ok)
                                }
                            }
                        }
                    }
                }
            }
        }
        resultView.versionView.text =
            versionMap?.get(codeInfo.addVersion) ?: codeInfo.addVersion.toString()
        return resultView.root
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}