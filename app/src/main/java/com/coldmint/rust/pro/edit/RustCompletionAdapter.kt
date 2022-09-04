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
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.databinding.EditItemBinding
import com.coldmint.rust.pro.tool.AppSettings
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
    private val simpleDisplayOfAutoCompleteMenu by lazy {
        AppSettings.getValue(AppSettings.Setting.SimpleDisplayOfAutoCompleteMenu, true)
    }
    private val spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder()
    private val colorSpan: ForegroundColorSpan by lazy {
        ForegroundColorSpan(GlobalMethod.getColorPrimary(context))
    }
    private val bold = StyleSpan(Typeface.BOLD)

    private var useEditBackground = false

    /**
     * 使用编辑器背景
     * @param enable Boolean
     */
    fun setEditBackground(enable: Boolean) {
        useEditBackground = enable
    }

    override fun getView(
        position: Int,
        view: View?,
        parent: ViewGroup,
        isCurrentCursorPosition: Boolean
    ): View {

        val editItem = EditItemBinding.inflate(layoutInflater, parent, false)
        if (useEditBackground) {
            editItem.root.setBackgroundResource(R.drawable.edit_item_background)
        }
        val completionItem = getItem(position) as RustCompletionItem
        spannableStringBuilder.clear()
        val label = completionItem.title
        spannableStringBuilder.append(label)
        //节补丁
        if (RustAutoCompleteProvider.keyWord.startsWith('[') && RustAutoCompleteProvider.keyWord.length > 1) {
            RustAutoCompleteProvider.keyWord =
                RustAutoCompleteProvider.keyWord.subSequence(
                    0,
                    RustAutoCompleteProvider.keyWord.length
                )
                    .toString()
        }
        val start = label.lowercase(Locale.getDefault())
            .indexOf(RustAutoCompleteProvider.keyWord.lowercase(Locale.getDefault()))
        if (start > -1) {
            val end = start + RustAutoCompleteProvider.keyWord.length
            spannableStringBuilder.setSpan(
                colorSpan,
                start,
                end,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            spannableStringBuilder.setSpan(bold, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        editItem.titleView.text = spannableStringBuilder
        if (simpleDisplayOfAutoCompleteMenu) {
            editItem.contentView.isVisible = false
            editItem.subTitleView.isVisible = false
            editItem.iconView.isVisible = false

        } else {
            editItem.contentView.isVisible = true
            editItem.subTitleView.isVisible = true
            editItem.iconView.isVisible = true
            editItem.contentView.text = completionItem.desc
            if (completionItem.subtitle != null) {
                editItem.subTitleView.text = completionItem.subtitle
            } else {
                editItem.subTitleView.isVisible = false
            }
            val icon = completionItem.icon
            if (icon != null) {
                Glide.with(context).load(icon).apply(GlobalMethod.getRequestOptions())
                    .into(editItem.iconView)
            }
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