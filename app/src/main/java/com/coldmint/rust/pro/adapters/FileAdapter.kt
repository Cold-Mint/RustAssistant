package com.coldmint.rust.pro.adapters

import android.os.Build
import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.content.Context
import com.coldmint.rust.pro.tool.GlobalMethod
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.FileItemBinding
import com.github.promeg.pinyinhelper.Pinyin
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.ArrayList

class FileAdapter(private val context: Context, dataList: MutableList<File?>) :
    BaseAdapter<FileItemBinding, File?>(context, dataList), PopupTextProvider {

    /**
     * 获取选中目录
     *
     * @return 选中目录
     */
    var selectPath: String? = null
        private set

    /**
     * 是复制文件
     *
     * @return
     */
    var isCopyFile = false
        private set


    /**
     * 清除选中目录
     */
    fun cleanSelectPath() {
        selectPath = null
    }

    /**
     * 设置选中目录
     *
     * @param selectPath
     * @param isCopyFile
     */
    fun setSelectPath(selectPath: String?, isCopyFile: Boolean) {
        this.selectPath = selectPath
        this.isCopyFile = isCopyFile
        val finalFile = File(selectPath)
        val index = dataList.indexOf(finalFile)
        if (index > -1) {
            notifyItemChanged(index)
        }
    }


//    /**
//     *
//     * @param isDescend Boolean
//     */
//    fun sortData(isDescend : Boolean){
//        dataList.sortByDescending {
//
//        }
//    }

    override fun setNewDataList(dataList: MutableList<File?>) {
        dataList.sortBy {
            val name = it?.name ?: "#"
            getInitial(name)
        }
        super.setNewDataList(dataList)
    }

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): FileItemBinding {
        return FileItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: File?,
        viewBinding: FileItemBinding,
        viewHolder: BaseAdapter.ViewHolder<FileItemBinding>,
        position: Int
    ) {
        if (data == null) {
            val drawable = context.getDrawable(R.drawable.previous_step)
            viewBinding.fileIcon.setImageDrawable(drawable)
            viewBinding.fileName.setText(R.string.return_directents)
            viewBinding.more.isVisible = false
        } else {
            viewBinding.more.isVisible = true
            viewBinding.fileName.text = data.name
            if (data.isDirectory) {
                val tem = data.listFiles()
                if (tem != null) {
                    val fileNum = data.listFiles().size
                    if (fileNum > 0) {
                        viewBinding.fileIcon.setImageDrawable(
                            GlobalMethod.tintDrawable(
                                context.getDrawable(
                                    R.drawable.folder
                                ), ColorStateList.valueOf(GlobalMethod.getColorPrimary(context))
                            )
                        )
                    } else {
                        viewBinding.fileIcon.setImageDrawable(
                            GlobalMethod.tintDrawable(
                                context.getDrawable(
                                    R.drawable.folder
                                ), null
                            )
                        )
                    }
                }
            } else {
                val type = FileOperator.getFileType(data)
                when (type) {
                    "ini", "txt", "template", "log" -> viewBinding.fileIcon.setImageDrawable(
                        GlobalMethod.tintDrawable(
                            context.getDrawable(R.drawable.file),
                            ColorStateList.valueOf(GlobalMethod.getColorPrimary(context))
                        )
                    )
                    "zip", "rwmod", "rar" -> viewBinding.fileIcon.setImageDrawable(
                        GlobalMethod.tintDrawable(
                            context.getDrawable(R.drawable.file),
                            ColorStateList.valueOf(GlobalMethod.getColorPrimary(context))
                        )
                    )
                    "png", "jpg", "bmp" -> Glide.with(context).load(data)
                        .apply(GlobalMethod.getRequestOptions()).into(viewBinding.fileIcon)
                    else -> {
                        viewBinding.fileIcon.setImageDrawable(context.getDrawable(R.drawable.file))
                    }
                }
            }
            if (selectPath != null && data.absolutePath == selectPath) {
                viewBinding.fileName.setTextColor(
                    ColorStateList.valueOf(GlobalMethod.getColorPrimary(context))
                )
            }
        }
    }


    override fun getPopupText(position: Int): String {
        val file = dataList[position]
        val name = file?.name ?: "#"
        return getInitial(name).toString()
    }
}