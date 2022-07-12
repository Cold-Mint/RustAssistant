package com.coldmint.rust.pro.dialog

import android.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.coldmint.dialog.BaseBottomDialog
import com.coldmint.rust.pro.databinding.DialogCommentBinding
import com.coldmint.rust.pro.tool.LinkAutoCompleteHelper
import com.coldmint.rust.pro.tool.TextStyleMaker
import com.google.android.material.bottomsheet.BottomSheetBehavior


/**
 * 评论对话框，部分方法与输入对话框逻辑相同
 * @property autoDismiss Boolean
 * @property inputCanBeEmpty Boolean
 * @property errorTipFunction Function2<String, TextInputLayout, Unit>?
 */
class CommentDialog(context: Context) : BaseBottomDialog<DialogCommentBinding>(context) {
    override fun getViewBindingObject(layoutInflater: LayoutInflater): DialogCommentBinding {
        return DialogCommentBinding.inflate(layoutInflater)
    }


    override fun onShowDialog(viewBinding: DialogCommentBinding) {
        viewBinding.textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    TextStyleMaker.instance.setStyle(p0, { type, data ->
                        TextStyleMaker.instance.clickEvent(context, type, data)
                    },context)
                }
            }

        })
    }


}