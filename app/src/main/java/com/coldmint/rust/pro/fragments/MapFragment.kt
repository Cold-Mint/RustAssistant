package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.MapClass
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.MapAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentMapBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
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
                                    CoreDialog(requireContext()).setTitle(R.string.edit_map)
                                        .setMessage(R.string.edit_map_tip)
                                        .setNegativeButton(R.string.dialog_close) {

                                        }.setCancelable(false)
                                if (AppOperator.isAppInstalled(
                                        requireContext(),
                                        packName
                                    )
                                ) {
                                    materialDialog.setPositiveButton(R.string.open_nottiled) {
                                        materialDialog.dismiss()
                                        AppOperator.openApp(requireContext(), packName)
                                    }
                                } else {
                                    materialDialog.setPositiveButton(R.string.downlod_nottiled) {
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

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentMapBinding {
        return FragmentMapBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.mapList.layoutManager = StableLinearLayoutManager(requireContext())
        val divider = MaterialDividerItemDecoration(
            requireContext(),
            MaterialDividerItemDecoration.VERTICAL
        )

        viewBinding.mapList.addItemDecoration(
            divider
        )
        val path = AppSettings.getValue(AppSettings.Setting.MapFolder, "")
        if (path.isNotBlank()) {
            loadPath(File(path))
        }
    }
}