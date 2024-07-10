package com.coldmint.rust.pro.dialog

import android.content.Context
import android.widget.TextView
import com.coldmint.rust.pro.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class MaterialBottomDialog(context: Context) : BottomSheetDialog(context) {
    init {
        setContentView(R.layout.dialog_bottom)
    }

    fun setMessage(string: String) {
        val findViewById = findViewById<TextView>(R.id.message)
        findViewById?.text = string
    }
    fun setTitle(string: String) {
        val findViewById = findViewById<TextView>(R.id.title)
        findViewById?.text = string
    }

    override fun setTitle(titleId: Int) {
        super.setTitle(titleId)
        val string = context.getString(titleId)
        delegate.setTitle(string)
        val findViewById = findViewById<TextView>(R.id.title)
        findViewById?.text = string
    }
}
