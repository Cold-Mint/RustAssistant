package com.coldmint.rust.pro.fragments

import com.coldmint.rust.core.ModClass
import android.view.LayoutInflater
import android.os.Bundle
import com.coldmint.rust.pro.R
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Looper
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.tool.AppSettings
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.coldmint.rust.core.dataBean.ModConfigurationData
import android.content.res.ColorStateList
import android.os.Build
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.adapters.ModActionAdapter
import com.coldmint.rust.pro.adapters.ModAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.ModDialogBinding
import com.coldmint.rust.pro.databinding.ModFragmentBinding
import com.coldmint.rust.pro.databinding.ModListItemBinding
import java.io.File
import java.util.ArrayList
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ModFragment : BaseFragment<ModFragmentBinding>() {
    lateinit var modAdapter: ModAdapter
    val needRecycling by lazy {
        if (GlobalMethod.isActive) {
            appSettings.getValue(
                AppSettings.Setting.EnableRecoveryStation,
                true
            )
        } else {
            false
        }
    }
    val executorService = Executors.newSingleThreadExecutor()

    //初始化视图
    fun loadMods() {
        val useProgressBar = !this::modAdapter.isInitialized || modAdapter.itemCount == 0
        val handler = Handler(Looper.getMainLooper())
        thread {
            if (useProgressBar) {
                handler.post {
                    viewBinding.progressBar.isVisible = true
                    viewBinding.modError.isVisible = false
                    viewBinding.modErrorIcon.isVisible = false
                    viewBinding.modList.isVisible = false
                }
            }
            val mod_directory = File(appSettings.getValue(AppSettings.Setting.ModFolder, ""))
            if (!mod_directory.exists()) {
                mod_directory.mkdirs()
            }
            val files = mod_directory.listFiles()
            val thisContent = requireContext()
            if (files != null && files.isNotEmpty()) {
                val data = ArrayList<ModClass>()
                for (t in files) {
                    if (ModClass.isMod(t)) {
                        data.add(ModClass(t))
                    }
                }
                if (data.isEmpty()) {
                    handler.post {
                        showNotFindMod()
                    }
                    return@thread
                }
                if (useProgressBar) {
                    modAdapter = ModAdapter(thisContent, data)
                } else {
                    modAdapter.setNewDataList(data)
                }
                modAdapter.setItemChangeEvent { changeType, i, modClass, i2 ->
                    if (i2 == 0) {
                        handler.post { loadMods() }
                    }
                }
                modAdapter.setItemEvent { i, modListItemBinding, viewHolder, modClass ->
                    modListItemBinding.root.setOnClickListener {
                        onClickItemWork(modListItemBinding, modClass)
                    }
                    modListItemBinding.root.setOnLongClickListener {
                        modAdapter.showDeleteItemDialog(
                            modClass.modName,
                            viewHolder.adapterPosition,
                            onClickPositiveButton = { d, b ->
                                delFile(handler, modClass, viewHolder.adapterPosition)
                                false
                            })
                        false
                    }
                }
                if (useProgressBar) {
                    handler.postDelayed({
                        viewBinding.modList.isVisible = true
                        viewBinding.modError.isVisible = false
                        viewBinding.modErrorIcon.isVisible = false
                        viewBinding.progressBar.isVisible = false

                        viewBinding.modList.adapter = modAdapter
                    }, MainActivity.hideViewDelay)
                } else {
                    handler.post {
                        modAdapter.notifyDataSetChanged()
                        viewBinding.modList.adapter = modAdapter
                    }
                }
            } else {
                handler.post {
                    showNotFindMod()
                }
            }
        }
    }

    /**
     * 删除文件
     * @param handler Handler
     * @param modClass ModClass
     */
    fun delFile(
        handler: Handler,
        modClass: ModClass,
        index: Int? = null
    ) {
        executorService.submit {
            val targetFile = modClass.modFile
            val errorFolder =
                File(AppSettings.dataRootDirectory + "/modErrorReport/" + modClass.modName)
            if (errorFolder.exists()) {
                FileOperator.delete_files(errorFolder)
            }
            val dataBasePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requireActivity().applicationContext.dataDir.absolutePath + "/databases/"
            } else {
                FileOperator.getSuperDirectory(
                    requireContext().cacheDir
                ) + "/databases/"
            }
            val name = modClass.modName
            val file = File(dataBasePath + name)
            val shmFile = File(dataBasePath + name + "-shm")
            val walFile = File(dataBasePath + name + "-wal")
            file.delete()
            shmFile.delete()
            walFile.delete()
            if (needRecycling) {
                var result = false
                val removePath: String
                val removeFile: File
                if (targetFile.isDirectory) {
                    removePath = appSettings.getValue(
                        AppSettings.Setting.RecoveryStationFolder,
                        requireContext().filesDir.absolutePath + "/backup/"
                    ).toString() + targetFile.name + "/"
                    removeFile = File(removePath)
                    if (!removeFile.exists()) {
                        removeFile.mkdirs()
                    }
                } else {
                    removePath = appSettings.getValue(
                        AppSettings.Setting.RecoveryStationFolder,
                        requireContext().filesDir.absolutePath + "/backup/"
                    ).toString() + targetFile.name
                    removeFile = File(removePath)
                }
                if (removeFile.exists()) {
                    FileOperator.delete_files(removeFile)
                }
                handler.post {
                    Snackbar.make(
                        viewBinding.modList,
                        String.format(
                            getString(R.string.recoverying_prompt),
                            modClass.modName
                        ),
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
                result = FileOperator.removeFiles(targetFile, removeFile)
                if (result) {
                    handler.post {
                        Snackbar.make(
                            viewBinding.modList,
                            String.format(
                                requireContext().getString(R.string.recovery_prompt),
                                modClass.modName
                            ),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        if (index != null) {
                            modAdapter.removeItem(index)
                        }
                    }
                } else {
                    handler.post {
                        Snackbar.make(
                            viewBinding.modList,
                            getString(R.string.cut_failed),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                handler.post {
                    Snackbar.make(
                        viewBinding.modList,
                        String.format(
                            getString(R.string.del_moding_tip),
                            modClass.modName
                        ),
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
                FileOperator.delete_files(targetFile)
                handler.post {
                    Snackbar.make(
                        viewBinding.modList,
                        String.format(
                            getString(R.string.del_completed),
                            modClass.modName
                        ),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    if (index != null) {
                        modAdapter.removeItem(index)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMods()
    }

    fun onClickItemWork(viewBinding: ModListItemBinding, modClass: ModClass) {
        val context = requireContext()
        val modDialogBinding =
            ModDialogBinding.inflate(LayoutInflater.from(context))
        val bottomSheetDialog =
            BottomSheetDialog(context, R.style.BottomSheetDialog)
        modDialogBinding.modNameView.text = viewBinding.modNameView.text
        modDialogBinding.modNameDescription.text = viewBinding.modIntroductionView.text
        val configurationManager = modClass.modConfigurationManager
        val configurationData: ModConfigurationData? =
            configurationManager?.readData()
        val works: MutableList<String> = ArrayList()
        if (modClass.modFile.isDirectory) {
            val developerMode = appSettings.getValue(AppSettings.Setting.DeveloperMode, false)
            if (developerMode) {
                works.add(getString(R.string.generate_error_report))
            }
            if (GlobalMethod.isActive) {
                works.add(getString(R.string.mod_action1))
                if (modClass.hasInfo()) {
                    works.add(getString(R.string.mod_action2))
                } else {
                    works.add(getString(R.string.mod_action10))
                }
                works.add(getString(R.string.global_operations))
                works.add(getString(R.string.manages_files))
                works.add(getString(R.string.optimization))
                works.add(getString(R.string.packmod))
            }
            works.add(getString(R.string.release))
            if (configurationData != null) {
                val title = configurationData.updateTitle
                if (!title.isEmpty()) {
                    works.add(title)
                }

                val modId = configurationData.modId
                if (modId != null) {
                    works.add(0, getString(R.string.work_of_home_page))
                }

            }
        } else {
            works.add(getString(R.string.mod_action9))
            works.add(getString(R.string.mod_action8))
        }
        works.add(getString(R.string.share_mod))
        val modActionAdapter = ModActionAdapter(
            context,
            works,
            modClass.modFile.path,
            this@ModFragment,
            bottomSheetDialog
        )
        if (configurationData != null) {
            modActionAdapter.setModConfigurationData(configurationData)
        }
        modDialogBinding.modActionList.adapter = modActionAdapter
        if (modClass.modFile.isDirectory) {
            if (modClass.modIcon == null) {
                val drawable = context.getDrawable(R.drawable.image)
                modDialogBinding.modIcon.setImageDrawable(
                    GlobalMethod.tintDrawable(
                        drawable, ColorStateList.valueOf(
                            GlobalMethod.getColorPrimary(
                                requireContext()
                            )
                        )
                    )
                )
            } else {
                modDialogBinding.modIcon.setImageBitmap(modClass.modIcon)
            }
        } else {
            val drawable = context.getDrawable(R.drawable.file)
            modDialogBinding.modIcon.setImageDrawable(
                GlobalMethod.tintDrawable(
                    drawable, ColorStateList.valueOf(
                        GlobalMethod.getColorPrimary(
                            requireContext()
                        )
                    )
                )
            )
        }
        bottomSheetDialog.setContentView(modDialogBinding.root)
        bottomSheetDialog.show()
    }

    /**
     * 显示没有找到模组
     */
    fun showNotFindMod() {
        viewBinding.modError.setText(R.string.not_find_mod)
        viewBinding.modError.isVisible = true
        viewBinding.modErrorIcon.isVisible = true
        viewBinding.modList.isVisible = false
        viewBinding.progressBar.isVisible = false
    }

    override fun getViewBindingObject(): ModFragmentBinding {
        return ModFragmentBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.modList.layoutManager = LinearLayoutManager(context)
        viewBinding.modList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }
}