package com.coldmint.rust.pro.adapters

import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.CompressionManager
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.interfaces.CompressionListener
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.CreateTemplateActivity
import com.coldmint.rust.pro.FileManagerActivity
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.TemplateBottomDialogBinding
import com.coldmint.rust.pro.databinding.TemplateListItemBinding
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.divider.MaterialDividerItemDecoration
import java.io.File

class TemplateListAdapter(
    context: Context,
    dataList: ArrayList<LocalTemplatePackage>,
    private val language: String,
    private val rootPath: String
) : BaseAdapter<TemplateListItemBinding, LocalTemplatePackage>(context, dataList) {

    private val editInfo = context.getString(R.string.mod_action2)
    private val share = context.getString(R.string.share_mod)
    private val update = context.getString(R.string.update)
    private val fileManager = context.getString(R.string.file_manager)
    private val exportFile = context.getString(R.string.export)

    /**
     * 编辑信息
     * @param context Context
     * @param localTemplatePackage LocalTemplatePackage
     */
    private fun editInfo(context: Context, localTemplatePackage: LocalTemplatePackage) {
        val bundle = Bundle()
        bundle.putString("json", FileOperator.readFile(localTemplatePackage.infoFile))
        bundle.putString("path", localTemplatePackage.infoFile.absolutePath)
        val intent = Intent(context, CreateTemplateActivity::class.java)
        intent.putExtra("data", bundle)
        context.startActivity(intent)
    }

    /**
     * 点击了分享
     * @param context Context
     * @param localTemplatePackage LocalTemplatePackage
     */
    private fun share(context: Context, localTemplatePackage: LocalTemplatePackage) {
        val materialDialog = CoreDialog(context)
        val handler = Handler(Looper.getMainLooper())
        Thread {
            val cacheDirectory =
                File(context.cacheDir.absolutePath + "/share/template")
            if (!cacheDirectory.exists()) {
                cacheDirectory.mkdirs()
            }
            val toFile =
                File(
                    cacheDirectory.absolutePath + "/" + localTemplatePackage.getName() + "_" + localTemplatePackage.getInfo()?.versionName + ".rp"
                )
            if (toFile.exists()) {
                toFile.delete()
            }
            handler.post {
                materialDialog.setTitle(R.string.packmod)
                    .setPositiveButton(R.string.dialog_close2){

                    }
                materialDialog.show()
            }
            val compressionManager =
                CompressionManager.instance

            compressionManager.compression(
                localTemplatePackage.directest,
                toFile,
                object : CompressionListener {
                    override fun whenCompressionFile(file: File): Boolean {
                        val msg = String.format(
                            context.getString(R.string.dialog_packing),
                            file.name
                        )
                        handler.post {
                            materialDialog.setMessage(msg)
                        }
                        return true
                    }

                    override fun whenCompressionFolder(folder: File): Boolean {
                        val msg = String.format(
                            context.getString(R.string.dialog_packing),
                            folder.name
                        )
                        handler.post {
                            materialDialog.setMessage(msg)
                        }
                        return true
                    }

                    override fun whenCompressionComplete(result: Boolean) {
                        handler.post {
                            materialDialog.dismiss()
                            if (result) {
                                CoreDialog(context).setTitle(R.string.share_mod).setMessage(
                                    String.format(
                                        context.getString(R.string.pack_success),
                                        localTemplatePackage.getName()
                                    )
                                ).setPositiveButton(R.string.share) {
                                    FileOperator.shareFile(
                                        context,
                                        toFile
                                    )
                                }.setNegativeButton(R.string.dialog_cancel) {

                                }.show()
                            } else {
                                CoreDialog(context).setTitle(R.string.share_mod).setMessage(R.string.pack_failed).setPositiveButton(R.string.dialog_ok){

                                }
                            }
                        }
                    }

                },
                null
            )
        }.start()
    }

    /**
     * 点击了导出文件
     * @param context Context
     * @param localTemplatePackage LocalTemplatePackage
     */
    private fun exportFile(context: Context, localTemplatePackage: LocalTemplatePackage) {
        val materialDialog = MaterialDialog(context)
        val handler = Handler(Looper.getMainLooper())
        Thread {
            val cacheDirectory =
                File(context.cacheDir.absolutePath + "/export/template")
            if (!cacheDirectory.exists()) {
                cacheDirectory.mkdirs()
            }
            val toFile =
                File(
                    cacheDirectory.absolutePath + "/" + localTemplatePackage.getName() + "_" + localTemplatePackage.getInfo()?.versionName + ".rp"
                )
            if (toFile.exists()) {
                toFile.delete()
            }
            handler.post {
                materialDialog.title(R.string.packmod)
                    .positiveButton(R.string.dialog_close2)
                materialDialog.show()
            }
            val compressionManager =
                CompressionManager.instance

            compressionManager.compression(
                localTemplatePackage.directest,
                toFile,
                object : CompressionListener {
                    override fun whenCompressionFile(file: File): Boolean {
                        val msg = String.format(
                            context.getString(R.string.dialog_packing),
                            file.name
                        )
                        handler.post {
                            materialDialog.message(text = msg)
                        }
                        return true
                    }

                    override fun whenCompressionFolder(folder: File): Boolean {
                        val msg = String.format(
                            context.getString(R.string.dialog_packing),
                            folder.name
                        )
                        handler.post {
                            materialDialog.message(text = msg)
                        }
                        return true
                    }

                    override fun whenCompressionComplete(result: Boolean) {
                        handler.post {
                            materialDialog.dismiss()
                            if (result) {
                                MaterialDialog(context).show {
                                    title(R.string.export_file).message(
                                        text = String.format(
                                            context.getString(R.string.pack_success2),
                                            localTemplatePackage.getName()
                                        )
                                    ).positiveButton(R.string.export) {
                                        val intent =
                                            Intent(context, FileManagerActivity::class.java)
                                        val bundle = Bundle()
                                        bundle.putString("type", "exportFile")
                                        bundle.putString("additionalData", toFile.absolutePath)
                                        intent.putExtra("data", bundle)
                                        context.startActivity(intent)
                                    }
                                }.negativeButton(R.string.dialog_cancel)
                            } else {
                                MaterialDialog(context).show {
                                    title(R.string.share_mod).message(R.string.pack_failed)
                                        .positiveButton(R.string.dialog_ok)
                                }
                            }
                        }
                    }

                },
                null
            )
        }.start()
    }


    /**
     * 点击了管理文件
     */
    private fun managesFileItem(context: Context, path: String) {
        val managesIntent = Intent(context, FileManagerActivity::class.java)
        val configurationBundle = Bundle()
        managesIntent.putExtra("data", configurationBundle)
        configurationBundle.putString("rootpath", rootPath)
        configurationBundle.putString("type", "default")
        configurationBundle.putString("path", path)
        configurationBundle.putString("additionalData", path)
        context.startActivity(managesIntent)
    }

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): TemplateListItemBinding {
        return TemplateListItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: LocalTemplatePackage,
        viewBinding: TemplateListItemBinding,
        viewHolder: ViewHolder<TemplateListItemBinding>,
        position: Int
    ) {
        if (data.isTemplate) {
            val info = data.getInfo()
            viewBinding.nameView.text = data.getName()
            viewBinding.describeView.text = info?.description
            viewBinding.developerView.text = "${info?.developer}|${info?.versionName}"
            viewBinding.onTouchView.setOnClickListener {
                val bottomSheetDialog =
                    BottomSheetDialog(context)
                val templateBottomDialogBinding =
                    TemplateBottomDialogBinding.inflate(LayoutInflater.from(context))
                bottomSheetDialog.setContentView(templateBottomDialogBinding.root)
                templateBottomDialogBinding.templateActionList.layoutManager =
                    StableLinearLayoutManager(context)
                templateBottomDialogBinding.titleView.text = data.getName()
                val list = ArrayList<String>()
                list.add(editInfo)
                list.add(fileManager)
                list.add(update)
                list.add(exportFile)
                list.add(share)
                val adapter = TemplateActionAdapter(context, list)
                adapter.setItemEvent { i, modActionItemBinding, viewHolder, s ->
                    modActionItemBinding.root.setOnClickListener {
                        bottomSheetDialog.dismiss()
                        when (s) {
                            share -> {
                                share(context, data)
                            }
                            exportFile -> {
                                exportFile(context, data)
                            }
                            fileManager -> {
                                managesFileItem(context, data.directest.absolutePath)
                            }
                            editInfo -> {
                                editInfo(context, data)
                            }
                            update -> {
                                val updateInfo = info?.update
                                if (updateInfo != null) {
                                    if (updateInfo.matches(Regex("^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$|^https://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"))) {
                                        AppOperator.useBrowserAccessWebPage(
                                            context,
                                            updateInfo
                                        )
                                    } else {
                                        MaterialDialog(context).show {
                                            title(text = s).message(text = updateInfo)
                                                .positiveButton(R.string.dialog_ok)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                templateBottomDialogBinding.templateActionList.adapter = adapter
                val divider = MaterialDividerItemDecoration(
                    context,
                    MaterialDividerItemDecoration.VERTICAL
                )

                templateBottomDialogBinding.templateActionList.addItemDecoration(
                    divider
                )
                bottomSheetDialog.show()
            }
            viewBinding.onTouchView.setOnLongClickListener {
                showDeleteItemDialog(
                    data.getName(),
                    viewHolder.adapterPosition,
                    onClickPositiveButton = { materialDialog, b ->
                        FileOperator.delete_files(data.directest)
                        true
                    })
                true
            }
        } else {
            viewBinding.nameView.text =
                FileOperator.getPrefixName(data.directest)
            viewBinding.describeView.setText(R.string.template_not_find_info)
            viewBinding.developerView.isVisible = false
        }
//        holder.templateListItemBinding.delButton.setOnClickListener {
//            if (holder.templateListItemBinding.delButton.text.toString() == mfragment.resources.getText(
//                    R.string.del_mod
//                )
//            ) {
//                Thread {
//                    mfragment.requireActivity().runOnUiThread {
//                        holder.templateListItemBinding.delButton.setBackgroundColor(R.color.blue_200)
//                        holder.templateListItemBinding.delButton.setText(R.string.del_moding)
//                    }
//                    val offset = mFiles.indexOf(templateClass.directest)
//                    mFiles.removeAt(offset)
//                    FileOperator.delete_files(templateClass.directest)
//                    mfragment.requireActivity().runOnUiThread {
//                        if (mFiles.size == 0) {
//                            mfragment.load_list(mRootPath, true)
//                        } else {
//                            notifyItemRemoved(offset)
//                        }
//                    }
//                }.start()
//            }
//        }
    }

}