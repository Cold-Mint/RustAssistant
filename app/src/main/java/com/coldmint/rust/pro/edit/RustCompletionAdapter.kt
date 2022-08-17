package com.coldmint.rust.pro.edit

import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.view.LayoutInflater
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import com.bumptech.glide.Glide
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import androidx.core.view.isVisible
import com.coldmint.rust.pro.databinding.EditItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter
import java.util.*


/**
 * Rust完成适配器
 * @property layoutInflater (LayoutInflater..LayoutInflater?)
 * @property spannableStringBuilder SpannableStringBuilder
 * @property colorSpan ForegroundColorSpan
 * @property bold StyleSpan
 */
class RustCompletionAdapter : EditorCompletionAdapter() {
    private val layoutInflater by lazy {
        LayoutInflater.from(context)
    }
    private val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder()
    private val colorSpan: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#2196F3"))
    private val bold = StyleSpan(Typeface.BOLD)

    override fun getView(
        position: Int,
        view: View?,
        parent: ViewGroup,
        isCurrentCursorPosition: Boolean
    ): View {
        val editItem = EditItemBinding.inflate(layoutInflater, parent, false)
        val completionItem = getItem(position) as RustCompletionItem
        spannableStringBuilder.clear()
        val label = completionItem.label.toString()
        spannableStringBuilder.append(label)
        //节补丁
        if (RustAutoComplete.keyWord.startsWith('[') && RustAutoComplete.keyWord.length > 1) {
            RustAutoComplete.keyWord =
                RustAutoComplete.keyWord.subSequence(1, RustAutoComplete.keyWord.length)
                    .toString()
        }
        val start = label.lowercase(Locale.getDefault())
            .indexOf(RustAutoComplete.keyWord.lowercase(Locale.getDefault()))
        if (start > -1) {
            val end = start + RustAutoComplete.keyWord.length
            spannableStringBuilder.setSpan(
                colorSpan,
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableStringBuilder.setSpan(bold, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        editItem.titleView.text = spannableStringBuilder
        editItem.contentView.text = completionItem.desc
        if (completionItem.subtitle != null) {
            editItem.subTitleView.text = completionItem.subtitle
        } else {
            editItem.subTitleView.isVisible = false
        }
        val icon = completionItem.icon
        if (icon != null) {
            Glide.with(context).load(icon).apply(GlobalMethod.getRequestOptions()).into(editItem.iconView)
        }
        return editItem.root
    }

    override fun getItemHeight(): Int {
        // 80 dp
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            80f,
            context.resources.displayMetrics
        ).toInt()
    }
}