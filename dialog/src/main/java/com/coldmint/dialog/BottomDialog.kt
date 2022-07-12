package com.coldmint.dialog

import android.view.View

interface BottomDialog {

    fun setContentView(view:View): BottomDialog

    fun setCancelable(cancelable:Boolean):BottomDialog

    fun dismiss()

    fun show():BottomDialog

}