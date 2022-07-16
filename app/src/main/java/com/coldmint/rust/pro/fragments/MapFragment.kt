package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.MapClass
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.MapAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentMapBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.google.android.material.divider.MaterialDividerItemDecoration
import java.io.File
import kotlin.concurrent.thread

/**
 * @author Cold Mint
 * @date 2022/1/5 10:57
 */
class MapFragment : BaseFragment<FragmentMapBinding>() {

    /**
     * 加载路径
     * @param folder File 文件夹
     */
    fun loadPath(folder: File) {
        val handler = Handler(Looper.getMainLooper())
        thread {
            if (folder.exists() && folder.isDirectory) {
                val fileList = folder.listFiles()
                if (fileList != null && fileList.isNotEmpty()) {
                    val mapList: ArrayList<MapClass> = ArrayList()
                    for (f in fileList) {
                        val type = FileOperator.getFileType(f)
                        if (type == "tmx") {
                            mapList.add(MapClass(f))
                        }
                    }
                    if (mapList.isEmpty()) {
                        handler.post {
                            withoutMap()
                        }
                    } else {
                        val adapter = MapAdapter(requireContext(), mapList)
                        adapter.setItemEvent { i, itemMapBinding, viewHolder, mapClass ->
                            itemMapBinding.mapLinearlayout.setOnClickListener {
                                val packName = "com.mirwanda.nottiled"
                                val materialDialog =
                                    MaterialDialog(requireContext()).title(R.string.edit_map)
                                        .message(R.string.edit_map_tip)
                                        .negativeButton(R.string.dialog_close).cancelable(false)
                                if (AppOperator.isAppInstalled(
                                        requireContext(),
                                        packName
                                    )
                                ) {
                                    materialDialog.positiveButton(R.string.open_nottiled)
                                        .positiveButton {
                                            materialDialog.dismiss()
                                            AppOperator.openApp(requireContext(), packName)
                                        }
                                } else {
                                    materialDialog.positiveButton(R.string.downlod_nottiled)
                                        .positiveButton {
                                            AppOperator.useBrowserAccessWebPage(
                                                requireContext(),
                                                "https://mint.lanzouo.com/ilXgJyfol8h"
                                            )
                                        }
                                }
                                materialDialog.show()
                            }

                            itemMapBinding.mapLinearlayout.setOnLongClickListener {
                                adapter.showDeleteItemDialog(
                                    mapClass.getName(),
                                    viewHolder.adapterPosition,
                                    onClickPositiveButton = { d, b ->
                                        mapClass.delete()
                                        true
                                    })
                                false
                            }
                        }
                        adapter.setItemChangeEvent { changeType, i, mapClass, i2 ->
                            if (mapList.isEmpty()) {
                                loadPath(folder)
                            }
                        }
                        handler.postDelayed({
                            viewBinding.mapList.adapter = adapter
                            viewBinding.mapList.isVisible = true
                            viewBinding.mapError.isVisible = false
                            viewBinding.progressBar.isVisible = false
                            viewBinding.mapErrorIcon.isVisible = false
                        }, MainActivity.hideViewDelay)
                    }
                } else {
                    handler.post {
                        withoutMap()
                    }
                }
            } else {
                handler.post {
                    withoutMap()
                }
            }
        }
    }

    /**
     * 没有地图状态
     */
    fun withoutMap() {
        viewBinding.progressBar.isVisible = false
        viewBinding.mapList.isVisible = false
        viewBinding.mapError.isVisible = true
        viewBinding.mapErrorIcon.isVisible = true
    }

    override fun getViewBindingObject(): FragmentMapBinding {
        return FragmentMapBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.mapList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.mapList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        val path = appSettings.getValue(AppSettings.Setting.MapFolder, "")
        if (path.isNotBlank()) {
            loadPath(File(path))
        }
    }
}