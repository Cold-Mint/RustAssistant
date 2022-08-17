package com.coldmint.rust.core.templateParser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import com.coldmint.rust.core.dataBean.InputParserDataBean
import com.coldmint.rust.core.dataBean.ListParserDataBean
import com.coldmint.rust.core.databinding.ParserListBinding
import com.coldmint.rust.core.interfaces.TemplateParser

/**
 * 列表解析器
 */
class ListParser(val context: Context, val data: ListParserDataBean) : TemplateParser {
    private val parserListBinding: ParserListBinding = ParserListBinding.inflate(
        LayoutInflater.from(context)
    )
    private val itemList: List<String> = data.itemList.split(",")
    private val dataList: List<String>? = data.dataList?.split(",")
    override fun getInput(): String {
        val index = parserListBinding.spacer.selectedItemPosition
        return if (dataList != null && dataList.size == itemList.size) {
            dataList[index]
        } else {
            itemList[index]
        }
    }


    override val contextView: View
        get() {
            val adapter =
                ArrayAdapter<String>(
                    context,
                    android.R.layout.simple_spinner_dropdown_item,
                    itemList
                )
            parserListBinding.spacer.adapter = adapter
            parserListBinding.nameView.text = data.name
            return parserListBinding.root
        }

    override fun setError(info: String) {
        return
    }

    override val section: String
        get() = data.section
    override val code: String
        get() = data.key
    override val needParse: Boolean
        get() = true
}