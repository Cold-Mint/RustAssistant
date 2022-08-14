package com.coldmint.rust.pro.fragments

import android.view.LayoutInflater
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.coldmint.rust.core.DataSet
import com.coldmint.rust.core.dataBean.dataset.CodeDataBean
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.adapters.DataSetAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.DatabaseFragmentBinding
import com.coldmint.rust.pro.databinding.DialogDatasetBinding
import com.google.gson.Gson
import java.io.File
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class DatabaseFragment : BaseFragment<DatabaseFragmentBinding>() {


    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.databaseList.layoutManager = LinearLayoutManager(requireContext())
        val database_directory = AppSettings.getValue(
            AppSettings.Setting.DatabaseDirectory,
            requireContext().filesDir.absolutePath + "/database/"
        )
        val directory = File(database_directory)
        if (!directory.exists()) {
            directory.mkdirs()
            showToast {
                "文件夹不存在"
            }
        } else {
            val files = directory.listFiles()
            val dataSet = ArrayList<DataSet>()
            if (files != null && files.isNotEmpty()) {
                for (f in files) {
                    if (f.isDirectory) {
                        dataSet.add(DataSet(f))
                    }
                }
                if (dataSet.size > 0) {
                    viewBinding.imageView.isVisible = false
                    viewBinding.databaseError.isVisible = false
                    viewBinding.databaseList.isVisible = true
                    val adapter = DataSetAdapter(requireContext(), dataSet)
                    adapter.setItemEvent { i, databaseItemBinding, viewHolder, dataSet ->

                        databaseItemBinding.databaseUse.setOnClickListener {
                            val array = requireContext().getStringArray(R.array.dateset_read_tips)

                            val datasetViewBing = DialogDatasetBinding.inflate(layoutInflater)
                            datasetViewBing.updateSpinner.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        p0: AdapterView<*>?,
                                        p1: View?,
                                        p2: Int,
                                        p3: Long
                                    ) {
                                        datasetViewBing.tipView.text = array[p2]
                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {
                                        datasetViewBing.tipView.text = array[0]
                                    }

                                }
                            val materialDialog =
                                MaterialDialog(requireContext()).title(R.string.use_database)
                                    .positiveButton(R.string.dialog_ok).positiveButton {
                                        val handler = Handler(Looper.getMainLooper())
                                        val executorService = Executors.newSingleThreadExecutor()
                                        val materialDialog2 = MaterialDialog(requireContext())
                                        executorService.submit {
                                            val valueArray =
                                                requireContext().getStringArray(R.array.dateset_read_entries)
                                            val type =
                                                valueArray[datasetViewBing.updateSpinner.selectedItemPosition]
                                            val readMode =
                                                when (type) {
                                                    requireContext().getString(R.string.read_mode_additional) -> {
                                                        CodeDataBase.ReadMode.Additional
                                                    }
                                                    requireContext().getString(R.string.read_mode_append_or_update) -> {
                                                        CodeDataBase.ReadMode.AppendOrUpdate
                                                    }
                                                    requireContext().getString(R.string.read_mode_copy) -> {
                                                        CodeDataBase.ReadMode.Copy
                                                    }
                                                    requireContext().getString(R.string.read_mode_delete) -> {
                                                        CodeDataBase.ReadMode.Delete
                                                    }
                                                    requireContext().getString(R.string.read_mode_update) -> {
                                                        CodeDataBase.ReadMode.Update
                                                    }
                                                    else -> {
                                                        CodeDataBase.ReadMode.Additional
                                                    }
                                                }
                                            handler.post {
                                                materialDialog2.cancelable(false)
                                                materialDialog2.title(text = type)
                                                materialDialog2.message(R.string.reading)
                                                materialDialog2.positiveButton(R.string.close)
                                                materialDialog2.setActionButtonEnabled(
                                                    WhichButton.POSITIVE,
                                                    false
                                                )
                                                materialDialog2.show()
                                            }
                                            val database =
                                                CodeDataBase.getInstance(requireContext())
                                            val result = database.loadDataSet(dataSet, readMode)
                                            val message = if (result) {
                                                R.string.read_the_complete
                                            } else {
                                                R.string.read_the_fail
                                            }
                                            handler.post {
                                                materialDialog2.setActionButtonEnabled(
                                                    WhichButton.POSITIVE,
                                                    true
                                                )
                                                materialDialog2.message(message)
                                            }
                                        }
                                    }
                            materialDialog.customView(view = datasetViewBing.root)
                            materialDialog.show()
                        }
                    }
                    viewBinding.databaseList.adapter = adapter
                }
            }
        }
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): DatabaseFragmentBinding {
        return DatabaseFragmentBinding.inflate(layoutInflater)
    }
}