package com.coldmint.rust.core.templateParser

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import com.coldmint.rust.core.dataBean.IntroducingDataBean
import com.coldmint.rust.core.databinding.ParserIntroduceBinding
import com.coldmint.rust.core.interfaces.TemplateParser

/**
 * @author Cold Mint
 * @date 2021/12/17 14:55
 */
class IntroducingParser(
    val context: Context,
    val data: IntroducingDataBean
) :
    TemplateParser {
    private val parserIntroduceBinding: ParserIntroduceBinding = ParserIntroduceBinding.inflate(
        LayoutInflater.from(context)
    )


    override val input: String
        get() = ""
    override val contextView: View
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                parserIntroduceBinding.textView.text =
                    Html.fromHtml(data.htmlData, FROM_HTML_MODE_LEGACY)
            } else {
                parserIntroduceBinding.textView.text = data.plainData
            }
            return parserIntroduceBinding.root
        }

    override fun setError(info: String) {

    }

    override val section: String?
        get() = null
    override val code: String
        get() = ""
    override val needParse: Boolean
        get() = false


}