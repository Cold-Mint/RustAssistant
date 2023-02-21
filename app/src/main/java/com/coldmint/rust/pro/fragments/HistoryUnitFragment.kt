package com.coldmint.rust.pro.fragments

import com.coldmint.rust.pro.EditActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.HistoryAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentHistoryBinding
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Cold Mint
 * @date 2022/1/14 15:54
 */
class HistoryUnitFragment(
) :
    BaseFragment<FragmentHistoryBinding>() {
    var fragmentActivity: FragmentActivity? = null
    var modClass: ModClass? = null
    var fileDataBase: FileDataBase? = null

    //当内容改变时的数据监听
    var whenNumberChanged: ((Int) -> Unit)? = null
    val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.unitList.layoutManager = StableLinearLayoutManager(requireContext())
        val divider = MaterialDividerItemDecoration(
            requireContext(),
            MaterialDividerItemDecoration.VERTICAL
        )

        viewBinding.unitList.addItemDecoration(
            divider
        )
        loadList()
    }

    //是否需要更新单位列表
    var needUpDateUnitsList = false

    /**
     * 加载列表
     */
    fun loadList() {
        val handler = Handler(Looper.getMainLooper())
        executorService.submit {
            if (fileDataBase == null) {
                return@submit
            }
            handler.post {
                viewBinding.unitError.isVisible = false
                viewBinding.progressBar.isVisible = true
                viewBinding.unitList.isVisible = false
            }
            val dataList = fileDataBase!!.getHistoryDao().getAll().toMutableList()
            if (dataList.isEmpty()) {
                handler.post {
                    showInfoToView(R.string.not_find_history)
                }
            } else {
                handler.post {
                    viewBinding.unitError.isVisible = false
                    viewBinding.progressBar.isVisible = false
                    viewBinding.unitList.isVisible = true
                    val adapter = HistoryAdapter(requireContext(), dataList)
                    adapter.setItemEvent { i, itemHistoryBinding, viewHolder, historyRecord ->
                        itemHistoryBinding.root.setOnLongClickListener {
                            adapter.showDeleteItemDialog(
                                historyRecord.fileName,
                                viewHolder.adapterPosition,
                                onClickPositiveButton = { d, b ->
                                    if (b) {
                                        val file = File(historyRecord.path)
                                        file.delete()
                                        needUpDateUnitsList = true
                                    }
                                    executorService.submit {
                                        fileDataBase!!.getHistoryDao().delete(historyRecord)
                                    }
                                    true
                                },
                                checkBoxPrompt = requireContext().getString(R.string.delete_source_file_check)
                            )
                            false
                        }

                        adapter.setItemChangeEvent { changeType, i, historyRecord, i2 ->
                            whenNumberChanged?.invoke(i2)
                            if (i2 == 0) {
                                loadList()
                            }
                        }

                        itemHistoryBinding.root.setOnClickListener {
                            val file = SourceFile(File(historyRecord.path))
                            openEditActivity(file)
                        }
                    }
                    viewBinding.unitList.adapter = adapter

                    whenNumberChanged?.invoke(dataList.size)
                }
            }
        }
    }


    /**
     * 打开编辑器活动
     * @param file SourceFileClass
     */
    fun openEditActivity(file: SourceFile) {
        if (modClass != null && fragmentActivity != null) {
            val bundle = Bundle()
            val path = file.file.absolutePath
            bundle.putString("path", path)
            bundle.putString("modPath", modClass!!.modFile.absolutePath)
            val intent = Intent(requireContext(), EditActivity::class.java)
            intent.putExtra("data", bundle)
            fragmentActivity!!.startActivityForResult(intent, 2)
        }
    }

    /**
     * 显示信息到视图上
     * @param resId Int?
     * @param string String?
     */
    private fun showInfoToView(resId: Int? = null, string: String? = null) {
        viewBinding.unitError.isVisible = true
        viewBinding.progressBar.isVisible = false
        viewBinding.unitList.isVisible = false
        if (resId != null) {
            viewBinding.unitError.setText(resId)
        }
        if (string != null) {
            viewBinding.unitError.text = string
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentHistoryBinding {
        return FragmentHistoryBinding.inflate(layoutInflater)
    }
}