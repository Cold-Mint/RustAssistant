package com.coldmint.rust.pro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.interfaces.FileFinderListener
import com.coldmint.rust.core.tool.FileFinder2
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.adapters.ErrorInfoAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databean.ErrorInfo
import com.coldmint.rust.pro.databinding.ActivityErrorInfoBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.google.gson.Gson
import java.io.File
import java.util.concurrent.Executors

class ErrorInfoActivity : BaseActivity<ActivityErrorInfoBinding>() {
    val executorService by lazy {
        Executors.newSingleThreadExecutor()
    }
    val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            viewBinding.toolbar.title = getText(R.string.see_error_info)
            setReturnButton()
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
            val path = AppSettings.dataRootDirectory + "/carsh/"
            loadList(path)
        }
    }

    fun loadList(path: String) {
        executorService.submit {
            val folder = File(path)
            if (folder.exists()) {
                val dataList = ArrayList<ErrorInfo>()
                val gson = Gson()
                val fileFinder2 = FileFinder2(folder)
                fileFinder2.findRule = ".+\\.log"
                fileFinder2.asRe = true
                fileFinder2.findMode = true
                fileFinder2.setFinderListener(object : FileFinderListener {
                    override fun whenFindFile(file: File): Boolean {
                        val data = FileOperator.readFile(file)
                        if (data != null) {
                            try {
                                val errorInfo =
                                    gson.fromJson<ErrorInfo>(data, ErrorInfo::class.java)
                                dataList.add(errorInfo)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        return true
                    }

                    override fun whenFindFolder(folder: File): Boolean {
                        return true
                    }

                })
                fileFinder2.onStart()
                if (dataList.isEmpty()) {
                    handler.post {
                        showInfoToView(R.string.no_error)
                    }
                } else {
                    handler.post {
                        viewBinding.progressBar.isVisible = false
                        viewBinding.recyclerView.isVisible = true
                        viewBinding.tipView.isVisible = false
                        viewBinding.recyclerView.adapter = ErrorInfoAdapter(this, dataList)
                    }
                }
            } else {
                handler.post {
                    showInfoToView(R.string.no_error)
                }
            }
        }
    }

    fun showInfoToView(resId: Int) {
        viewBinding.progressBar.isVisible = false
        viewBinding.recyclerView.isVisible = false
        viewBinding.tipView.isVisible = true
        viewBinding.tipView.setText(resId)
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityErrorInfoBinding {
        return ActivityErrorInfoBinding.inflate(layoutInflater)
    }
}