package com.coldmint.rust.pro.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.AttachFileItemBinding
import java.io.File
import java.util.ArrayList

class AttachFileAdapter(val context: Context, dataList: MutableList<File>) :
    BaseAdapter<AttachFileItemBinding, File>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): AttachFileItemBinding {
        return AttachFileItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: File,
        viewBinding: AttachFileItemBinding,
        viewHolder: ViewHolder<AttachFileItemBinding>,
        position: Int
    ) {
        if (data.exists()) {
            viewBinding.filePath.text = data.absolutePath
        } else {
            viewBinding.filePath.setText(R.string.file_not_exist)
        }
        viewBinding.fileName.text = data.name
    }
}