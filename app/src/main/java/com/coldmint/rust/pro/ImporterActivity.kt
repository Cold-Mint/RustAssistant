package com.coldmint.rust.pro

import com.coldmint.rust.pro.base.BaseActivity
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.coldmint.rust.core.CompressionManager
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.template.TemplateInfo
import com.coldmint.rust.core.interfaces.UnzipListener
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.FileOperator.copyFile
import com.coldmint.rust.pro.databinding.ActivityImporterBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.viewmodel.StartViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.io.*
import org.json.JSONObject
import java.util.zip.ZipEntry

class ImporterActivity : BaseActivity<ActivityImporterBinding>() {
    private var uri: Uri? = null
    private var fileName: String? = null
    private var type: String? = null
    val startViewModel by lazy {
        ViewModelProvider(this).get(StartViewModel::class.java)
    }

    private fun initView() {
        setTitle(R.string.file_importer)
        startViewModel.initAllData()
        GlobalMethod.requestStoragePermissions(this) {
            if (it) {
                val intent = intent
                uri = intent.data
                fileName = getFileName(uri)
                type = getFileType(fileName)
                if (type != null) {
                    if (type == "rwmod" || type == "zip") {
                        viewBinding.importerTip.text =
                            String.format(getString(R.string.import_tip2), fileName)
                        viewBinding.okButton.isVisible = true
                    } else if (type == "rp") {
                        viewBinding.importerTip.text =
                            String.format(getString(R.string.import_tip3), fileName)
                        viewBinding.okButton.isVisible = true
                    } else {
                        val tip =
                            String.format(
                                getString(R.string.import_type_could_not_be_resolved),
                                type
                            )
                        viewBinding.importerTip.text = tip
                    }
                } else {
                    val tip =
                        String.format(
                            getString(R.string.import_path_could_not_be_resolved),
                            uri?.path
                        )
                    viewBinding.importerTip.text = tip
                }
            }
        }
    }


    /**
     * 从uri获取文件名称
     * @param uri Uri uri
     * @return String? 文件格式无法获取返回null
     */
    fun getFileName(uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        val path = uri.path
        return if (path != null) {
            val index = path.lastIndexOf("/")
            if (index > -1) {
                path.substring(index + 1)
            } else {
                null
            }
        } else {
            null
        }
    }


    /**
     * 获取文件类型
     * @param fileName String 文件名
     * @return 返回文件类型
     */
    fun getFileType(fileName: String?): String? {
        if (fileName == null) {
            return null
        }
        val index = fileName.lastIndexOf(".")
        return if (index > -1) {
            fileName.substring(index + 1)
        } else {
            null
        }
    }

    private fun initAction() {
        viewBinding.okButton.setOnClickListener {
            val onclickType = viewBinding.okButton.text
            val importName = getString(R.string.import_name)
            if ((type == "rwmod" || type == "zip") && onclickType == importName) {
                importMod(File(AppSettings.getValue(AppSettings.Setting.ModFolder, "")))
            } else if (type == "rp") {
                val file = File(
                    AppSettings.getValue(
                        AppSettings.Setting.TemplateDirectory,
                        this.filesDir.absolutePath + "/template/"
                    ) + LocalTemplatePackage.getAbsoluteFileName(filename = fileName)
                )
                val cacheDirectory = File(cacheDir.absolutePath + "/template/")
                if (!cacheDirectory.exists()) {
                    cacheDirectory.mkdirs()
                }
                importTemplate(cacheDirectory, file)
            } else {
                Toast.makeText(this, "类型错误$type", Toast.LENGTH_SHORT).show()
            }
        }
        viewBinding.cancelButton.setOnClickListener { finish() }
    }


    /**
     * 导入模板方法
     * @param outputDirectory File 输出文件夹（缓存目录）
     * @param templateDirectory File 模板目录(解压目录)
     */
    private fun importTemplate(outputDirectory: File, templateDirectory: File) {
        val handler = Handler(Looper.getMainLooper())
        Thread(Runnable {
            if (uri == null) {
                handler.post {
                    Snackbar.make(
                        viewBinding.okButton,
                        R.string.uri_null,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
            if (parcelFileDescriptor != null) {
                val fileDescriptor = parcelFileDescriptor.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val newFile = File(outputDirectory.absolutePath + "/" + fileName)
                if (newFile.exists()) {
                    newFile.delete()
                }
                handler.post {
                    viewBinding.okButton.setText(R.string.importing)
                    viewBinding.okButton.setBackgroundColor(
                        GlobalMethod.getThemeColor(
                            this,
                            R.attr.colorPrimaryVariant
                        )
                    )
                }
                val result = copyFile(inputStream, newFile)
                //检查版本
                if (result) {
                    val compressionManager = CompressionManager.instance
                    val gson = Gson()
                    //如果建立缓存完成，并且模板文件存在
                    if (templateDirectory.exists()) {
                        val newInfoData =
                            compressionManager.readEntry(newFile, LocalTemplatePackage.INFONAME)
                        val newInfo =
                            gson.fromJson(newInfoData, TemplateInfo::class.java)
                        if (newInfo == null) {
                            handler.post {
                                viewBinding.okButton.setBackgroundColor(
                                    GlobalMethod.getColorPrimary(
                                        this
                                    )
                                )
                                viewBinding.okButton.setText(R.string.import_name)
                                Snackbar.make(
                                    viewBinding.okButton,
                                    getString(R.string.import_failed2),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                            return@Runnable
                        } else {
                            val templateClass = LocalTemplatePackage(templateDirectory)
                            val oldInfo = templateClass.getInfo()
                            if (oldInfo == null) {
                                handler.post {
                                    Snackbar.make(
                                        viewBinding.okButton,
                                        getString(R.string.import_failed2),
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                                return@Runnable
                            }
                            val thisAppVersion =
                                AppOperator.getAppVersionNum(this, this.packageName)
                            if (newInfo.appVersionNum > thisAppVersion) {
                                handler.post {
                                    viewBinding.okButton.setBackgroundColor(
                                        GlobalMethod.getColorPrimary(
                                            this
                                        )
                                    )
                                    viewBinding.okButton.setText(R.string.import_name)
                                    Snackbar.make(
                                        viewBinding.okButton,
                                        String.format(
                                            getString(R.string.app_version_error),
                                            fileName
                                        ), Snackbar.LENGTH_LONG
                                    ).show()
                                }
                                return@Runnable
                            }
                            if (newInfo.versionNum < oldInfo.versionNum) {
                                handler.post {
                                    MaterialDialog(this).show {
                                        title(text = oldInfo.name).cancelable(false).message(
                                            text = String.format(
                                                getString(R.string.covers_the_import),
                                                newInfo.versionName, oldInfo.versionName
                                            )
                                        ).positiveButton(R.string.dialog_ok).positiveButton {
                                            FileOperator.delete_files(templateDirectory)
                                            importTemplate(outputDirectory, templateDirectory)
                                        }.negativeButton(R.string.dialog_cancel).negativeButton {
                                            viewBinding.okButton.setBackgroundColor(
                                                GlobalMethod.getColorPrimary(
                                                    this@ImporterActivity
                                                )
                                            )
                                            viewBinding.okButton.setText(R.string.import_name)
                                        }
                                    }
                                }
                                return@Runnable
                            } else {
                                //同等版本，不做处理（覆盖安装）
                            }
                        }
                    } else {
                        //常规导入
                        val newInfo =
                            compressionManager.readEntry(newFile, LocalTemplatePackage.INFONAME)
                        if (newInfo == null) {
                            handler.post {
                                viewBinding.okButton.setBackgroundColor(
                                    GlobalMethod.getColorPrimary(
                                        this
                                    )
                                )
                                viewBinding.okButton.setText(R.string.import_name)
                                Snackbar.make(
                                    viewBinding.okButton,
                                    getString(R.string.import_failed2),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                            return@Runnable
                        } else {
                            val jsonObject = JSONObject(newInfo)
                            val appVersion = jsonObject.getInt("appVersionNum")
                            val thisAppVersion =
                                AppOperator.getAppVersionNum(this, this.packageName)
                            if (appVersion > thisAppVersion) {
                                handler.post {
                                    viewBinding.okButton.setBackgroundColor(
                                        GlobalMethod.getColorPrimary(
                                            this
                                        )
                                    )
                                    viewBinding.okButton.setText(R.string.import_name)
                                    Snackbar.make(
                                        viewBinding.okButton,
                                        String.format(
                                            getString(R.string.app_version_error),
                                            fileName
                                        ), Snackbar.LENGTH_LONG
                                    ).show()
                                }
                                return@Runnable
                            }
                        }
                    }

                    compressionManager.unzip(
                        newFile,
                        templateDirectory,
                        object : UnzipListener {
                            override fun whenUnzipFile(zipEntry: ZipEntry, file: File): Boolean {
                                return true
                            }

                            override fun whenUnzipFolder(
                                zipEntry: ZipEntry,
                                folder: File
                            ): Boolean {
                                return true
                            }

                            override fun whenUnzipComplete(result: Boolean) {
                                newFile.delete()
                                handler.post {
                                    handler.post {
                                        viewBinding.okButton.setBackgroundColor(
                                            GlobalMethod.getColorPrimary(
                                                this@ImporterActivity
                                            )
                                        )
                                        viewBinding.okButton.isVisible = false
                                        Snackbar.make(
                                            viewBinding.okButton,
                                            String.format(
                                                getString(R.string.import_complete),
                                                fileName
                                            ), Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }


                        })

                } else {
                    handler.post {
                        viewBinding.okButton.setBackgroundColor(
                            GlobalMethod.getColorPrimary(
                                this
                            )
                        )
                        viewBinding.okButton.setText(R.string.import_name)
                        Snackbar.make(
                            viewBinding.okButton,
                            String.format(
                                getString(R.string.import_failed),
                                fileName
                            ), Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                handler.post {
                    showError("parcelFileDescriptor为空")
                }
            }
        }).start()
    }


    /**
     * 导入模组
     * @param outputDirectory File
     */
    private fun importMod(outputDirectory: File) {
        val handler = Handler(Looper.getMainLooper())
        Thread(Runnable {
            if (uri == null) {
                handler.post {
                    Snackbar.make(
                        viewBinding.okButton,
                        R.string.uri_null,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                return@Runnable
            }
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
            if (parcelFileDescriptor != null) {
                val fileDescriptor = parcelFileDescriptor.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val newFile = File(outputDirectory.absolutePath + "/" + fileName)
                if (newFile.exists()) {
                    handler.post {
                        MaterialAlertDialogBuilder(this).setTitle(R.string.import_name)
                            .setCancelable(false)
                            .setMessage(
                                String.format(
                                    getString(R.string.covers_the_import_mod),
                                    fileName
                                )
                            ).setPositiveButton(
                                R.string.dialog_ok
                            ) { dialog, which ->
                                newFile.delete()
                                importMod(outputDirectory)
                            }
                            .setNegativeButton(R.string.dialog_cancel, null).show()
                    }
                    return@Runnable
                }
                handler.post {
                    viewBinding.okButton.setText(R.string.importing)
                    viewBinding.okButton.setBackgroundColor(
                        GlobalMethod.getThemeColor(
                            this,
                            R.attr.colorPrimaryVariant
                        )
                    )
                }
                val result = copyFile(inputStream, newFile)
                if (result) {
                    handler.post {
                        handler.post {
                            viewBinding.okButton.setBackgroundColor(
                                GlobalMethod.getColorPrimary(
                                    this
                                )
                            )
                            viewBinding.okButton.isVisible = false
                            Snackbar.make(
                                viewBinding.okButton,
                                String.format(
                                    getString(R.string.import_complete),
                                    fileName
                                ), Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    handler.post {
                        viewBinding.okButton.setBackgroundColor(
                            GlobalMethod.getColorPrimary(
                                this
                            )
                        )
                        viewBinding.okButton.setText(R.string.import_name)
                        Snackbar.make(
                            viewBinding.okButton,
                            String.format(
                                getString(R.string.import_failed),
                                fileName
                            ), Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                handler.post {
                    Toast.makeText(this, "parcelFileDescriptor为空", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }).start()
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityImporterBinding {
        return ActivityImporterBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            initView()
            initAction()
        }
    }
}