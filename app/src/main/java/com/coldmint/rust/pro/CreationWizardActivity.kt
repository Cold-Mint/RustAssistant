package com.coldmint.rust.pro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coldmint.rust.pro.adapters.GuideAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databean.GuideData
import com.coldmint.rust.pro.databinding.ActivityCreationWizardBinding
import com.coldmint.rust.pro.tool.AppSettings

class CreationWizardActivity : BaseActivity<ActivityCreationWizardBinding>() {
    //创建向导类型（模组，模板包）
    lateinit var type: String
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        setReturnButton()
        title = getString(R.string.creation_wizard)
        val temType = intent.getStringExtra("type")
        if (temType.isNullOrBlank()) {
            showToast("请传入类型")
            finish()
            return
        }
        type = temType
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        when (temType) {
            "mod" -> {
                val dataList = ArrayList<GuideData>()
                dataList.add(
                    GuideData(
                        R.string.create_mod_lable,
                        R.string.create_mod_describe,
                        R.drawable.ic_outline_create_24
                    )
                )
                dataList.add(
                    GuideData(
                        R.string.import_mod,
                        R.string.import_mod_from_file_manager_describe,
                        R.drawable.folder
                    )
                )
                dataList.add(
                    GuideData(
                        R.string.import_mod_from_package_directory,
                        R.string.import_mod_from_package_directory_describe,
                        R.drawable.zip
                    )
                )
                dataList.add(
                    GuideData(
                        R.string.import_mod_from_recycle_bin,
                        R.string.import_mod_from_recycle_bin_describe,
                        R.drawable.auto_delete
                    )
                )
                val adapter = GuideAdapter(this, dataList)
                adapter.setItemEvent { i, itemGuideBinding, viewHolder, guideData ->
                    itemGuideBinding.root.setOnClickListener {
                        finish()
                        when (guideData.titleRes) {
                            R.string.create_mod_lable -> {
                                startActivity(
                                    Intent(
                                        this,
                                        CreateModActivity::class.java
                                    )
                                )
                            }
                            R.string.import_mod -> {
                                val startIntent =
                                    Intent(this, FileManagerActivity::class.java)
                                val fileBundle = Bundle()
                                fileBundle.putString("type", "selectFile")
                                startIntent.putExtra("data", fileBundle)
                                startActivity(startIntent)
                            }
                            R.string.import_mod_from_package_directory -> {
                                val startIntent =
                                    Intent(this, FileManagerActivity::class.java)
                                val fileBundle = Bundle()
                                fileBundle.putString("type", "selectFile")
                                val packDirectory = appSettings.getValue(
                                    AppSettings.Setting.PackDirectory,
                                    AppSettings.dataRootDirectory + "/bin/"
                                )
                                fileBundle.putString("path", packDirectory)
                                fileBundle.putString("rootpath", packDirectory)
                                startIntent.putExtra("data", fileBundle)
                                startActivity(startIntent)
                            }
                            R.string.import_mod_from_recycle_bin -> {
                                startActivity(Intent(this, RecyclingStationActivity::class.java))
                            }
                        }
                    }
                }

                viewBinding.recyclerView.adapter = adapter
            }
        }
    }

    override fun getViewBindingObject(): ActivityCreationWizardBinding {
        return ActivityCreationWizardBinding.inflate(layoutInflater)
    }

}