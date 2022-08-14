package com.coldmint.rust.pro.adapters

import android.content.Context
import android.content.pm.PackageInfo
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.coldmint.rust.pro.R
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.coldmint.rust.core.CompressionManager
import com.coldmint.rust.core.GameSynchronizer
import com.coldmint.rust.core.interfaces.GameSynchronizerListener
import com.coldmint.rust.core.interfaces.UnzipListener
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ApplicationItemBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.zip.ZipEntry
import kotlin.concurrent.thread

/**
 * 应用适配器
 * @property context Context
 * @constructor
 */
class ApplicationListAdapter(
     context: Context, dataList: MutableList<PackageInfo>
) : BaseAdapter<ApplicationItemBinding, PackageInfo>(context, dataList) {

    val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    val materialDialog: MaterialDialog by lazy {
        MaterialDialog(context)
    }


    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ApplicationItemBinding {
        return ApplicationItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: PackageInfo,
        viewBinding: ApplicationItemBinding,
        viewHolder: ViewHolder<ApplicationItemBinding>,
        position: Int
    ) {
        val packageManager = context.packageManager
        Glide.with(context).load(data.applicationInfo.loadIcon(packageManager)).apply(GlobalMethod.getRequestOptions())
            .into(viewBinding.appIconView)
        val appName = data.applicationInfo.loadLabel(packageManager).toString()
        viewBinding.appNameView.text = appName
        viewBinding.appVersionView.text = data.versionName
        viewBinding.packageNameView.text = data.packageName
        viewBinding.root.setOnClickListener {
            val popupMenu = PopupMenu(context, viewBinding.root)
            popupMenu.menu.add(R.string.set_game_pack)
            popupMenu.menu.add(R.string.exportApk)
            popupMenu.menu.add(R.string.application_information)
            popupMenu.menu.add(R.string.synchronous)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                val title = item.title.toString()
                if (title == context.getString(R.string.set_game_pack)) {
                    if (context.applicationInfo.packageName == data.packageName) {
                        Snackbar.make(
                            viewBinding.root,
                            context.getString(R.string.cannot_set_self_to_a_game),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        return@OnMenuItemClickListener false
                    }
                    val result = AppSettings.setValue(
                        AppSettings.Setting.GamePackage,
                        data.packageName
                    )
                    if (result) {
                        Snackbar.make(
                            viewBinding.root,
                            context.getString(R.string.set_success),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        Snackbar.make(
                            viewBinding.root,
                            context.getString(R.string.set_failed),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else if (title == context.getString(R.string.application_information)) {
                    context.startActivity(AppOperator.openSettings(data.packageName))
                } else if (title == context.getString(R.string.exportApk)) {
                    Thread {
                        val gameSynchronizer = GameSynchronizer(context, data)
                        val folder = AppSettings.dataRootDirectory + "/apk"
                        val apkFolder = File(folder)
                        if (!apkFolder.exists()) {
                            apkFolder.mkdirs()
                        }
                        handler.post {
                            materialDialog.show {
                                title(R.string.export_apk_title).message(
                                    R.string.export_apk_load
                                ).positiveButton(R.string.dialog_close).cancelable(false)
                            }
                        }
                        val path = folder + "/" + appName + "_" + data.versionName + ".apk"
                        val result =
                            gameSynchronizer.exportApk(path)
                        if (result) {
                            handler.post {
                                materialDialog.message(
                                    text =
                                    String.format(
                                        context.getString(
                                            R.string.export_apk_path
                                        ), path
                                    )
                                )
                            }
                        } else {
                            handler.post {
                                materialDialog.dismiss()
                                Snackbar.make(
                                    viewBinding.root,
                                    context.getString(R.string.export_apk_failure),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }.start()
                } else if (title == context.getString(R.string.synchronous)) {
                    val materialDialog = MaterialDialog(context).show {
                        title(text = appName).message(R.string.synchronous_ing)
                            .positiveButton(R.string.dialog_ok).cancelable(false)
                    }
                    materialDialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                    val gameSynchronizer = GameSynchronizer(context, data)
                    gameSynchronizer.generateData(
                        AppSettings.getValue(
                            AppSettings.Setting.TemplateDirectory,
                            context.filesDir.absolutePath + "/template/"
                        ), object : GameSynchronizerListener {
                            override fun whenChanged(handler: Handler, name: String) {
                                handler.post {
                                    materialDialog.message(text = name)
                                }
                            }


                            override fun whenCompleted(boolean: Boolean) {
                                if (boolean) {
                                    materialDialog.message(R.string.synchronous_ok)
                                } else {
                                    materialDialog.message(R.string.synchronous_failure)
                                }
                                materialDialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                            }

                        })
                }
                false
            })
        }
    }

}