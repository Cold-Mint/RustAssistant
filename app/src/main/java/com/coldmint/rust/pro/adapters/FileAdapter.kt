package com.coldmint.rust.pro.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.FileItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.viewmodel.FileManagerViewModel
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.io.File

class FileAdapter(context: Context, dataList: MutableList<File?>) :
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


    private var sortType: FileManagerViewModel.SortType = FileManagerViewModel.SortType.BY_NAME

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

    /**
     * 设置排序方式
     * @param sortType SortType
     */
    fun setSort(sortType: FileManagerViewModel.SortType) {
        this.sortType = sortType
    }

    override fun setNewDataList(dataList: MutableList<File?>) {
//        FileSort(dataList, sortType)
        when (sortType) {
            FileManagerViewModel.SortType.BY_NAME -> {
                dataList.sortBy {
                    val name = it?.name ?: "#"
                    getInitial(name)
                }
            }
            FileManagerViewModel.SortType.BY_SIZE -> {
                dataList.sortBy {
                    val size = it?.length() ?: 0.toLong()
                    size
                }
            }
            FileManagerViewModel.SortType.BY_TYPE -> {
                dataList.sortBy {
                    val type = if (it != null) {
                        FileOperator.getFileType(it)
                    } else {
                        "#"
                    }
                    type
                }
            }
            FileManagerViewModel.SortType.BY_LAST_MODIFIED -> {
                dataList.sortBy {
                    val lastModified = it?.lastModified() ?: 0.toLong()
                    lastModified
                }
            }
            else -> {
                dataList.sortBy {
                    val name = it?.name ?: "#"
                    getInitial(name)
                }
            }
        }
        dataList.sortBy {
            val name = it?.isFile ?: it?.isDirectory
            name
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
        viewHolder: ViewHolder<FileItemBinding>,
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
                when (FileOperator.getFileType(data)) {
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
        if (position >= dataList.size) {
            return "#"
        }
        val file = dataList[position]
        val data = when (sortType) {
            FileManagerViewModel.SortType.BY_NAME -> {
                val name = file?.name ?: "#"
                getInitial(name)
            }
            FileManagerViewModel.SortType.BY_SIZE -> {
                val size = file?.length() ?: 0.toLong()
                size
            }
            FileManagerViewModel.SortType.BY_TYPE -> {
                val type = if (file != null) {
                    FileOperator.getFileType(file)
                } else {
                    "#"
                }
                type
            }
            FileManagerViewModel.SortType.BY_LAST_MODIFIED -> {
                val lastModified = file?.lastModified() ?: 0.toLong()
                ServerConfiguration.toStringTime(lastModified)
            }
            else -> {
                val name = file?.name ?: "#"
                getInitial(name)
            }
        }.toString()
        return data
    }
}