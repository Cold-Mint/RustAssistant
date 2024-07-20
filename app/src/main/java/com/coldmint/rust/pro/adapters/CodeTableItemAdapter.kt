package com.coldmint.rust.pro.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.CodeInfo
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.databinding.CodeTableItemBinding
import com.coldmint.rust.pro.dialog.MaterialBottomDialog
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.chip.Chip
import com.muqing.VH
import java.util.concurrent.Executors

class CodeTableItemAdapter(val recyclerView: RecyclerView,
                           var list: List<CodeInfo>) : RecyclerView.Adapter<VH<CodeTableItemBinding>>() {

    private val lineParser = LineParser()

    init {
        lineParser.symbol = ","
    }

    var labelFunction: ((Int, View, String) -> Unit)? = null
    var versionMap: HashMap<Int, String>? = null
    var typeNameMap: HashMap<String, String>? = null
    var sectionMap: HashMap<String, String>? = null
    private val executorService by lazy {
        Executors.newSingleThreadExecutor()
    }
    private val developerMode by lazy {
        AppSettings
                .getValue(AppSettings.Setting.DeveloperMode, false)
    }

    //在list查找是否有这个关键字
    fun search(keyword: String) {
        CodeTableAdapter.pickString = keyword
        for (i in list.indices) {
            if (list[i].translate.contains(keyword) || list[i].description.contains(keyword) || list[i].code.contains(keyword)) {
                CodeTableAdapter.picklist.add(i)
            }
        }
        (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(CodeTableAdapter.pick, 0)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<CodeTableItemBinding> {
        return VH(CodeTableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun contains(string: String, textView: TextView) {
        if (string.contains(CodeTableAdapter.pickString)) {
            val spannableText = SpannableStringBuilder(string)
            val highlightWord = CodeTableAdapter.pickString
            val start = string.indexOf(highlightWord)
            val end = start + highlightWord.length
            if (start != -1) {
                // 设置文字颜色
                val colorSpan = ForegroundColorSpan(Color.RED)
                spannableText.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                // 设置背景颜色
                val backgroundSpan = BackgroundColorSpan(Color.YELLOW)
                spannableText.setSpan(backgroundSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = spannableText
        } else {
            textView.text = string
        }
    }

    private fun containsB(string: String, textView: TextView) {
        if (string.contains(CodeTableAdapter.pickString)) {
            val spannableText = SpannableStringBuilder(string)
            val highlightWord = CodeTableAdapter.pickString
            val start = string.indexOf(highlightWord)
            val end = start + highlightWord.length
            if (start != -1) {
                // 设置文字颜色
                val colorSpan = ForegroundColorSpan(Color.RED)
                spannableText.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                // 设置背景颜色
                val backgroundSpan = BackgroundColorSpan(Color.BLUE)
                spannableText.setSpan(backgroundSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = spannableText
        } else {
            textView.text = string
        }
    }

    @SuppressLint("StringFormatInvalid")
    override fun onBindViewHolder(holder: VH<CodeTableItemBinding>, position: Int) {
        val context = holder.itemView.context
        val resultView: CodeTableItemBinding = holder.binging
        val codeInfo = list[position]

        contains(codeInfo.description, resultView.descriptionView)
        resultView.descriptionView.setOnClickListener {
            GlobalMethod.copyText(it.context, codeInfo.description, it)
        }
        contains(codeInfo.translate, resultView.titleView)
        resultView.titleView.setOnClickListener {
            GlobalMethod.copyText(it.context, codeInfo.translate, it)
        }
        val demo = codeInfo.demo
        resultView.imageView.isVisible = demo.isNotBlank()
        resultView.imageView.setOnClickListener {
            val materialBottomDialog = MaterialBottomDialog(it.context)
            materialBottomDialog.setTitle(R.string.code_demo)
            materialBottomDialog.setMessage(demo)
            materialBottomDialog.show()
        }
        contains(codeInfo.code, resultView.subTitleView)
        resultView.subTitleView.setOnClickListener {
            GlobalMethod.copyText(it.context, codeInfo.code, it)
        }

        resultView.valueTypeView.text = typeNameMap?.get(codeInfo.type) ?: codeInfo.type
        lineParser.text = codeInfo.section

        var isNotEmpty = false
        lineParser.analyse { lineNum, lineData, isEnd ->
            isNotEmpty = true
            val text = sectionMap?.get(lineData) ?: lineData
            val chip = Chip(context)
            chip.text = text
            chip.setOnClickListener {
                labelFunction?.invoke(lineNum, it, text)
            }
//                resultView.chipGroup.addView(chip)
            true
        }

//            resultView.chipGroup.isVisible = isNotEmpty
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


    }
}