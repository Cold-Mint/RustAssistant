package com.coldmint.rust.core.templateParser

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import com.coldmint.rust.core.R
import com.coldmint.rust.core.dataBean.InputParserDataBean
import com.coldmint.rust.core.databinding.ParserInputBinding
import com.coldmint.rust.core.interfaces.TemplateParser

/**
 * 输入解析器
 */
class InputParser(val context: Context, val data: InputParserDataBean) : TemplateParser {
    private val parserInputBinding: ParserInputBinding =
        ParserInputBinding.inflate(LayoutInflater.from(context))

    override val input: String
        get() = parserInputBinding.inputView.text.toString()

    override val contextView: View
        get() {
            parserInputBinding.inputLayout.hint = data.name
            if (data.helpText != null) {
                parserInputBinding.inputLayout.helperText = data.helpText
            }
            if (data.num != null) {
                parserInputBinding.inputLayout.isCounterEnabled = true
                parserInputBinding.inputLayout.counterMaxLength = data.num
            }
            parserInputBinding.inputView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    if (text.isBlank()) {
                        setError(context.getString(R.string.please_enter_content))
                    } else {
                        parserInputBinding.inputLayout.isErrorEnabled = false
                    }
                }

            })
            return parserInputBinding.root
        }


    /**
     * 使输入框指向下一个视图
     */
    fun pointToNextView() {
        parserInputBinding.inputView.imeOptions = EditorInfo.IME_ACTION_NEXT
    }

    override fun setError(info: String) {
        parserInputBinding.inputLayout.isErrorEnabled = true
        parserInputBinding.inputLayout.error = info
    }

    override val section: String?
        get() = data.section

    override val code: String
        get() = data.key
    override val needParse: Boolean
        get() = true

}