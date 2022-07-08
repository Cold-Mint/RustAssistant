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
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.ArrayList

class FileAdapter(private val context: Context, private var dataList: MutableList<File?>) :
    BaseAdapter<FileItemBinding, File?>(context, dataList) {

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

    enum class SortType {
        FileName, ModifyDate, FileVolume
    }

    /**
     * 设置排序方式
     * @param type SortType?
     */
    fun setSortType(type: SortType?) {
        when (type) {
            SortType.FileName -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dataList.sortedWith(java.util.Comparator<File?> { o1, o2 ->
                    if (o1 == null) {
                        return@Comparator -1
                    }
                    if (o2 == null) {
                        return@Comparator 1
                    }
                    if (o1.isDirectory && o2.isFile) {
                        -1
                    } else if (o1.isFile && o2.isDirectory) {
                        1
                    } else {
                        o1.name.compareTo(o2.name)
                    }
                })
            }
            SortType.ModifyDate -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dataList.sortedWith(java.util.Comparator<File?> { o1, o2 ->
                    if (o1 == null) {
                        return@Comparator -1
                    }
                    if (o2 == null) {
                        return@Comparator 1
                    }
                    val diff = o1.lastModified() - o2.lastModified()
                    if (diff > 0) {
                        1
                    } else if (diff < 0) {
                        -1
                    } else {
                        0
                    }
                })
            }
            SortType.FileVolume -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dataList.sortedWith(java.util.Comparator<File?> { o1, o2 ->
                    if (o1 == null) {
                        return@Comparator -1
                    }
                    if (o2 == null) {
                        return@Comparator 1
                    }
                    val diff = o1.length() - o2.length()
                    if (diff > 0) {
                        1
                    } else if (diff < 0) {
                        -1
                    } else {
                        0
                    }
                })
            }
            else -> {}
        }
    }


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


    companion object {
        /**
         * 转换后符号将id数组转换为字符串数组
         *
         * @param context 上下文环境
         * @param ints    数组
         * @return 数据数组
         */
        fun conversionSymbol(context: Context, ints: IntArray): Array<String> {
            val list: MutableList<String> = ArrayList()
            for (r in ints) {
                val s = context.getText(r).toString()
                list.add(s)
            }
            return list.toTypedArray()
        }
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
        viewviewBinding: BaseAdapter.ViewHolder<FileItemBinding>,
        position: Int
    ) {
        if (data == null) {
            val drawable = context.getDrawable(R.drawable.previous_step)
            viewBinding.fileIcon.setImageDrawable(drawable)
            viewBinding.fileName.setText(R.string.return_directents)
            viewBinding.more.isVisible = false
            viewBinding.fileTime.isVisible = false
        } else {
            viewBinding.more.isVisible = true
            viewBinding.fileName.text = data.name
            val update_time = data.lastModified()
            val timeStringBuilder = StringBuilder()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            timeStringBuilder.append(formatter.format(update_time))
            if (data.isDirectory) {
                val tem = data.listFiles()
                if (tem != null) {
                    val fileNum = data.listFiles().size
                    if (fileNum > 0) {
                        timeStringBuilder.append(" ")
                        timeStringBuilder.append(
                            String.format(
                                (context.getText(R.string.filenum) as String),
                                fileNum
                            )
                        )
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
                    "png", "jpg", "bmp" -> Glide.with(context).load(data).apply(GlobalMethod.getRequestOptions()).into(viewBinding.fileIcon)
                    else -> {
                        viewBinding.fileIcon.setImageDrawable(context.getDrawable(R.drawable.file))
                    }
                }
            }
            viewBinding.fileTime.text = timeStringBuilder.toString()
            if (selectPath != null && data.absolutePath == selectPath) {
                viewBinding.fileName.setTextColor(Color.GREEN)
                viewBinding.fileTime.setTextColor(Color.GREEN)
            }
        }
    }
}