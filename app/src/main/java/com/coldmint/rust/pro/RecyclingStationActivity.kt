package com.coldmint.rust.pro

import com.coldmint.rust.pro.base.BaseActivity
import android.os.Bundle
import android.view.Menu
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.pro.tool.AppSettings
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.isVisible
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.adapters.FileAdapter
import com.coldmint.rust.pro.databinding.ActivityRecyclingStationBinding
import java.io.File
import java.util.ArrayList

class RecyclingStationActivity : BaseActivity<ActivityRecyclingStationBinding>() {
    private lateinit var mWorkFolder: File
    private var working = false
    private var mDayNum = 0

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            title = getString(R.string.enable_the_recovery_station)
            viewBinding.backupList.layoutManager =
                LinearLayoutManager(this@RecyclingStationActivity)
            val workFolderPath = appSettings.getValue(
                AppSettings.Setting.RecoveryStationFolder,
                this@RecyclingStationActivity.filesDir.absolutePath + "/backup/"
            )
            mDayNum = Integer.valueOf(
                appSettings.getValue(
                    AppSettings.Setting.RecoveryStationFileSaveDays,
                    7
                )
            )
            mWorkFolder = File(workFolderPath)
            if (!mWorkFolder.exists()) {
                mWorkFolder.mkdirs()
            }
            loadFiles()
        }
    }


    private fun loadFiles() {
        Thread {
            val files = mWorkFolder.listFiles()
            val fileArrayList: MutableList<File?> = ArrayList()
            val nowTime = System.currentTimeMillis()
            //先设置为多少秒
            val targetDifferences = (mDayNum * 86400000).toLong()
            for (file in files) {
                val last = file.lastModified()
                val differences = nowTime - last
                if (differences > targetDifferences) {
                    FileOperator.delete_files(file)
                } else {
                    fileArrayList.add(file)
                }
            }
            if (fileArrayList.size > 0) {
                val fileAdapter = FileAdapter(this@RecyclingStationActivity, fileArrayList)
                fileAdapter.setItemEvent { i, fileItemBinding, viewHolder, file ->
                    fileItemBinding.more.setOnClickListener {
                        if (file == null) {
                            return@setOnClickListener
                        }
                        val popupMenu =
                            PopupMenu(this@RecyclingStationActivity, fileItemBinding.more)
                        popupMenu.menu.add(R.string.recovery_file)
                        popupMenu.menu.add(R.string.delete_title)
                        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                            if (working) {
                                return@OnMenuItemClickListener true
                            }
                            val title = item.title.toString()
                            if (title == getString(R.string.delete_title)) {
                                Thread {
                                    working = true
                                    FileOperator.delete_files(file)
                                    runOnUiThread {
                                        working = false
                                        loadFiles()
                                    }
                                }.start()
                            } else if (title == getString(R.string.recovery_file)) {
                                Thread {
                                    working = true
                                    val removePath: String
                                    val modDirectory =
                                        appSettings.getValue(AppSettings.Setting.ModFolder, "")
                                    val removeFile: File
                                    if (file.isDirectory) {
                                        removePath = modDirectory + file.name + "/"
                                        removeFile = File(removePath)
                                        if (!removeFile.exists()) {
                                            removeFile.mkdirs()
                                        }
                                    } else {
                                        removePath = modDirectory + file.name
                                        removeFile = File(removePath)
                                    }
                                    FileOperator.removeFiles(file, removeFile)
                                    runOnUiThread {
                                        working = false
                                        loadFiles()
                                    }
                                }.start()
                            }
                            true
                        })
                        popupMenu.show()
                    }
                }
                runOnUiThread {
                    viewBinding.progressBar.isVisible = false
                    viewBinding.backupError.isVisible = false
                    viewBinding.backupList.isVisible = true
                    viewBinding.backupList.adapter = fileAdapter
                }
            } else {
                runOnUiThread {
                    viewBinding.progressBar.isVisible = false
                    viewBinding.backupError.isVisible = true
                    viewBinding.backupList.isVisible = false
                    viewBinding.backupError.setText(R.string.not_find_mod)
                }
            }
        }.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.clear_recovery_station -> {
                Thread {
                    runOnUiThread {
                        viewBinding.progressBar.isVisible = true
                        viewBinding.backupError.isVisible = true
                        viewBinding.backupList.isVisible = false
                        viewBinding.backupError.setText(R.string.del_moding)
                    }
                    FileOperator.delete_files(mWorkFolder)
                    runOnUiThread {
                        viewBinding.progressBar.isVisible = false
                        viewBinding.backupError.isVisible = true
                        viewBinding.backupList.isVisible = false
                        viewBinding.backupError.setText(R.string.not_find_mod)
                    }
                }.start()
                return true
            }
            R.id.restore_all -> {
                Thread {
                    runOnUiThread {
                        viewBinding.progressBar.isVisible = true
                        viewBinding.backupError.isVisible = true
                        viewBinding.backupList.isVisible = false
                        viewBinding.backupError.setText(R.string.restoreing)
                    }
                    FileOperator.removeFiles(
                        mWorkFolder,
                        File(appSettings.getValue(AppSettings.Setting.ModFolder, ""))
                    )
                    runOnUiThread {
                        viewBinding.progressBar.isVisible = false
                        viewBinding.backupError.isVisible = true
                        viewBinding.backupList.isVisible = false
                        viewBinding.backupError.setText(R.string.not_find_mod)
                    }
                }.start()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_recovery_station, menu)
        return true
    }


    override fun getViewBindingObject(): ActivityRecyclingStationBinding {
        return ActivityRecyclingStationBinding.inflate(layoutInflater)
    }


}