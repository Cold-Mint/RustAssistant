package com.coldmint.rust.pro.adapters

import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.content.Context
import android.widget.PopupMenu
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.MapAndMusicItemBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import java.io.File
import java.util.ArrayList

class MapAndMusicAdapter(context: Context, dataList: ArrayList<File>, val isMusic: Boolean) :
    BaseAdapter<MapAndMusicItemBinding, File>(context, dataList) {
    val prefixName = "[noloop]"


    fun onClickItem(
        targetFile: File,
        viewHolder: BaseAdapter.ViewHolder<MapAndMusicItemBinding>,
        viewBinding: MapAndMusicItemBinding
    ) {
        val fileType = FileOperator.getFileType(targetFile)
        var fileName = FileOperator.getPrefixName(targetFile)
        val popupMenu = GlobalMethod.createPopMenu(viewBinding.root)
        if (isMusic) {
            if (fileName.startsWith(prefixName)) {
                popupMenu.menu.add(R.string.enable_loop)
            } else {
                popupMenu.menu.add(R.string.disabled_loop)
            }
        }
        popupMenu.menu.add(R.string.rename)
        popupMenu.menu.add(R.string.delete_title)
        popupMenu.setOnMenuItemClickListener { item ->
            val title = item.title.toString()
            var position = viewHolder.adapterPosition
            if (title == context.getString(R.string.enable_loop)) {
                //启用循环
                val prefixLocation = fileName.indexOf(prefixName)
                if (prefixLocation == 0) {
                    val newFile =
                        File(targetFile.parent + "/" + fileName.substring(prefixLocation + prefixName.length) + "." + fileType)
                    targetFile.renameTo(newFile)
                    replaceItem(newFile, position)
                }
            } else if (title == context.getString(R.string.disabled_loop)) {
                val prefixLocation = fileName.indexOf(prefixName)
                if (prefixLocation == -1) {
                    val newFile =
                        File(targetFile.parent + "/" + prefixName + fileName + "." + fileType)
                    targetFile.renameTo(newFile)
                    replaceItem(newFile, position)
                }
            } else if (title == context.getString(R.string.delete_title)) {
                targetFile.delete()
                if (!isMusic) {
                    val path = targetFile.absolutePath
                    val symbol = path.indexOf(".tmx")
                    if (symbol > -1) {
                        val iconFile = File(path.substring(0, symbol) + "_map.png")
                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
                    }
                }
                removeItem(position)
            } else if (title == context.getString(R.string.rename)) {
                var iconFile: File? = null
                if (!isMusic) {
                    val path = targetFile.absolutePath
                    val symbol = path.indexOf(".tmx")
                    if (symbol > -1) {
                        iconFile = File(path.substring(0, symbol) + "_map.png")
                    }
                }

                val oldName = fileName
                val finalIconFile = iconFile
                MaterialDialog(context).show {
                    title(R.string.rename)
                    input(
                        maxLength = 255,
                        waitForPositiveButton = false, prefill = oldName
                    ) { dialog, text ->
                        if (text.length in 1..255) {
                            dialog.setActionButtonEnabled(
                                WhichButton.POSITIVE,
                                true
                            )
                        }
                    }.positiveButton(R.string.dialog_ok, null) { dialog ->
                        val string = dialog.getInputField().text.toString()
                        if (string != oldName) {
                            if (!isMusic && finalIconFile != null) {
                                val newIcon =
                                    File(FileOperator.getSuperDirectory(finalIconFile) + "/" + string + "_map.png")
                                finalIconFile.renameTo(newIcon)
                            }
                            fileName = string
                            val newFile =
                                File(FileOperator.getSuperDirectory(targetFile) + "/" + string + "." + fileType)
                            targetFile.renameTo(newFile)
                            viewBinding.fileName.text = string
                        }
                    }
                }.negativeButton(R.string.dialog_close)
            }
            false
        }
        popupMenu.show()
    }

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): MapAndMusicItemBinding {
        return MapAndMusicItemBinding.inflate(layoutInflater)
    }

    override fun onBingView(
        data: File,
        viewBinding: MapAndMusicItemBinding,
        viewHolder: BaseAdapter.ViewHolder<MapAndMusicItemBinding>,
        position: Int
    ) {
        var fileName = FileOperator.getPrefixName(data)
        val loadLocation = fileName.indexOf(prefixName)
        if (isMusic) {
            if (loadLocation == 0) {
                //如果开头为[noloop],则合并循环播放提示
                viewBinding.fileName.text = String.format(
                    context.getString(R.string.noloop),
                    fileName.substring(loadLocation + prefixName.length)
                )
            } else {
                viewBinding.fileName.text = fileName
            }
        } else {
            viewBinding.fileName.text = fileName
        }
        viewBinding.root.setOnClickListener {
            onClickItem(data, viewHolder, viewBinding)
        }
    }
}