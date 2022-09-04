package com.coldmint.rust.pro.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.*
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.dataBean.ModConfigurationData
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.ModActionAdapter
import com.coldmint.rust.pro.adapters.ModAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.ModDialogBinding
import com.coldmint.rust.pro.databinding.FragmentModBinding
import com.coldmint.rust.pro.databinding.ModListItemBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.viewmodel.ModViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.File

class ModFragment : BaseFragment<FragmentModBinding>() {
    val viewModel: ModViewModel by lazy {
        ModViewModel()
    }
    lateinit var modAdapter: ModAdapter
    val needRecycling by lazy {
        if (GlobalMethod.isActive) {
            AppSettings.getValue(
                AppSettings.Setting.EnableRecoveryStation,
                true
            )
        } else {
            false
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
        val scope = CoroutineScope(Job())
        scope.launch {
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
                    removePath = AppSettings.getValue(
                        AppSettings.Setting.RecoveryStationFolder,
                        requireContext().filesDir.absolutePath + "/backup/"
                    ).toString() + targetFile.name + "/"
                    removeFile = File(removePath)
                    if (!removeFile.exists()) {
                        removeFile.mkdirs()
                    }
                } else {
                    removePath = AppSettings.getValue(
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

    /**
     * 加载模组列表
     */
    fun loadModList() {
        val scope = CoroutineScope(Job())
        val handler = Handler(Looper.getMainLooper())
        scope.launch {
            val dataList = viewModel.loadMod()
            handler.post {
                viewBinding.progressBar.isVisible = true
                viewBinding.modErrorIcon.isVisible = false
                viewBinding.modError.isVisible = false
                viewBinding.swipeRefreshLayout.isVisible = false
            }
            if (dataList == null) {
                handler.post {
                    viewBinding.modError.setText(R.string.not_find_mod)
                    viewBinding.modError.isVisible = true
                    viewBinding.modErrorIcon.isVisible = true
                    viewBinding.swipeRefreshLayout.isVisible = false
                    viewBinding.progressBar.isVisible = false
                }
            } else {
                handler.post {
                    viewBinding.swipeRefreshLayout.isVisible = true
                    viewBinding.progressBar.isVisible = false
                    viewBinding.modErrorIcon.isVisible = false
                    viewBinding.modError.isVisible = false
                    if (isAdded) {
                        modAdapter = ModAdapter(requireContext(), dataList)
                        FastScrollerBuilder(viewBinding.modList).useMd2Style()
                            .setPopupTextProvider(modAdapter).build()
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
                        viewBinding.modList.adapter = modAdapter
                    } else {
                        DebugHelper.printLog("加载模组列表", "没有附加到活动", isError = true)
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        loadModList()
    }

    @SuppressLint("RestrictedApi")
    fun onClickItemWork(viewBinding: ModListItemBinding, modClass: ModClass) {
        val context = requireContext()
        val modDialogBinding =
            ModDialogBinding.inflate(LayoutInflater.from(context))
        val bottomSheetDialog =
            BottomSheetDialog(context)


        modDialogBinding.modNameView.text = viewBinding.modNameView.text
        modDialogBinding.modNameDescription.text = viewBinding.modIntroductionView.text
        val configurationManager = modClass.modConfigurationManager
        val configurationData: ModConfigurationData? =
            configurationManager?.readData()
        val works: MutableList<String> = ArrayList()
        if (modClass.modFile.isDirectory) {
            val developerMode = AppSettings.getValue(AppSettings.Setting.DeveloperMode, false)
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
            works.add(getString(R.string.rename))
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

//    /**
//     * 显示没有找到模组
//     */
//    fun showNotFindMod() {
//        viewBinding.modError.setText(R.string.not_find_mod)
//        viewBinding.modError.isVisible = true
//        viewBinding.modErrorIcon.isVisible = true
//        viewBinding.modList.isVisible = false
//        viewBinding.progressBar.isVisible = false
//    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentModBinding {
        return FragmentModBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.modList.layoutManager = LinearLayoutManager(context)
        val divider = MaterialDividerItemDecoration(
            requireContext(),
            MaterialDividerItemDecoration.VERTICAL
        )
        viewBinding.modList.addItemDecoration(
            divider
        )
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            loadModList()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
        viewModel.loadMod()
    }
}