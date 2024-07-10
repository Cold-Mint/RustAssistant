package com.coldmint.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


/**
 * 基础底部对话框
 * @param ViewBindingType : ViewBinding
 * @property viewBinding ViewBindingType
 */
abstract class BaseBottomDialog<ViewBindingType : ViewBinding>(val context: Context) :
    BottomDialog {


   protected val bottomSheetDialog: BottomSheetDialog by lazy {
        BottomSheetDialog(context)
   }

   protected val layoutInflater by lazy {
        LayoutInflater.from(context)
    }

   protected val viewBinding by lazy {
        getViewBindingObject(layoutInflater)
    }

    /**
     * 获取布局绑定对象
     * @param layoutInflater LayoutInflater
     * @return ViewBindingType
     */
    abstract fun getViewBindingObject(layoutInflater: LayoutInflater): ViewBindingType

    /**
     * 当显示对话框
     */
    abstract fun onShowDialog(viewBinding:ViewBindingType)

    @Deprecated("不建议这样设置，请继承BaseBottomDialog类")
    override fun setContentView(view: View): BottomDialog {
        bottomSheetDialog.setContentView(view)
        return this
    }

    override fun setCancelable(cancelable: Boolean): BottomDialog {
        bottomSheetDialog.setCancelable(cancelable)
        return this
    }


    override fun dismiss() {
        bottomSheetDialog.dismiss()
    }

    override fun show(): BottomDialog {
        setContentView(viewBinding.root)
        onShowDialog(viewBinding)
        bottomSheetDialog.show()
        return this
    }


}