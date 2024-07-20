package com.coldmint.rust.pro.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.coldmint.rust.core.database.code.CodeInfo
import com.coldmint.rust.core.database.code.SectionInfo
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.databinding.ActivityCodeTableBinding
import com.coldmint.rust.pro.databinding.ItemCodetableBinding
import com.google.android.gms.common.internal.Objects
import com.muqing.VH

class CodeTableAdapter(
        val context: Context,
        private val group: List<SectionInfo>,
        private val itemList: List<List<CodeInfo>>,
        private val binding: ActivityCodeTableBinding
) : RecyclerView.Adapter<VH<ItemCodetableBinding>>() {
    private var versionMap: HashMap<Int, String>? = null
    private var typeNameMap: HashMap<String, String>? = null
    private var sectionMap: HashMap<String, String>? = null

    /*    private val executorService by lazy {
            Executors.newSingleThreadExecutor()
        }*/
    private val lineParser = LineParser()

    //Label点击事件
//    var labelFunction: ((Int, View, String) -> Unit)? = null
    /*    private val developerMode by lazy {
            AppSettings
                    .getValue(AppSettings.Setting.DeveloperMode, false)
        }*/

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

    /*    fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup
        ): View {
            val resultView: CodeTableItemBinding =
                CodeTableItemBinding.inflate(layoutInflater, parent, false)
            val codeInfo = itemList[groupPosition][childPosition]

    //        resultView.belongStackLabelView.onLabelClickListener = OnLabelClickListener { index, v, s ->
    //        }
            resultView.descriptionView.text = codeInfo.description
            resultView.descriptionView.setOnClickListener {
                GlobalMethod.copyText(context, codeInfo.description, it)
            }
            resultView.titleView.text = codeInfo.translate
            resultView.titleView.setOnClickListener {
                GlobalMethod.copyText(context, codeInfo.translate, it)
            }

            val demo = codeInfo.demo
            resultView.imageView.isVisible = demo.isNotBlank()
            resultView.imageView.setOnClickListener {
                val dialog = MaterialAlertDialogBuilder(context);
                dialog.setTitle(R.string.code_demo)
                dialog.setMessage(demo)
                dialog.setPositiveButton(R.string.dialog_ok){
                    v,a->
                }
                dialog.show()
            }

            resultView.subTitleView.text = codeInfo.code
            resultView.subTitleView.setOnClickListener {
                GlobalMethod.copyText(context, codeInfo.code, it)
            }
            resultView.valueTypeView.text = typeNameMap?.get(codeInfo.type) ?: codeInfo.type
            lineParser.text = codeInfo.section
            resultView.chipGroup.removeAllViews()
            var isNotEmpty = false
            lineParser.analyse { lineNum, lineData, isEnd ->
                isNotEmpty = true
                val text = sectionMap?.get(lineData) ?: lineData
                val chip = Chip(context)
                chip.text = text
                chip.setOnClickListener {
                    labelFunction?.invoke(lineNum, it, text)
                }
                resultView.chipGroup.addView(chip)
                true
            }

            resultView.chipGroup.isVisible = isNotEmpty
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
        }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<ItemCodetableBinding> {
        return VH(ItemCodetableBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return group.size
    }

    companion object {
        var i: String = null.toString()
        var pick: Int = 0
        var pickString: String = ""
        var picklist: MutableList<Int> = mutableListOf()
    }

    var item: CodeTableItemAdapter? = null

    init {
        lineParser.symbol = ","
    }

    @SuppressLint("StringFormatInvalid")
    override fun onBindViewHolder(holder: VH<ItemCodetableBinding>, position: Int) {
        holder.binging.title.text = group[position].translate
        val format = String.format(
                context.getString(R.string.filenum),
                itemList[position].size
        )
        holder.binging.message.text = format
        holder.itemView.setOnClickListener {
            i = group[position].translate
            if (item != null) {
                item!!.list = itemList[position]
                binding.codeRecyclerB.adapter = item
                notifyDataSetChanged()
            }
            if (!binding.edittext.text.isNullOrEmpty())
                binding.edittext.setText("")

//            notifyItemChanged(p)
        }
        if (Objects.equal(group[position].translate, i)) {
            //背景高亮
            holder.binging.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_dark_onSecondaryContainer))
        } else {
            //背景恢复
            holder.binging.root.setCardBackgroundColor(Color.parseColor("#FBEEF5"))
        }

    }
}