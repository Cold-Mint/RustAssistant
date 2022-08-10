package com.coldmint.rust.pro.adapters


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.BaseAdapter
import com.coldmint.rust.core.dataBean.ModConfigurationData
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import com.coldmint.rust.core.tool.AppOperator
import android.widget.Toast
import android.widget.EditText
import android.os.Bundle
import android.content.Intent
import com.coldmint.rust.pro.tool.AppSettings
import com.afollestad.materialdialogs.MaterialDialog
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.coldmint.dialog.InputDialog
import com.coldmint.rust.core.*
import com.google.android.material.snackbar.Snackbar
import com.coldmint.rust.core.dataBean.CompileConfiguration
import com.coldmint.rust.core.dataBean.ModErrorReport
import com.coldmint.rust.core.database.code.ValueTypeInfo
import com.coldmint.rust.core.interfaces.*
import com.coldmint.rust.core.tool.FileFinder2
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.*
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.databinding.ModActionItemBinding
import com.coldmint.rust.pro.fragments.ModFragment
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.lang.NumberFormatException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import kotlin.collections.ArrayList

/**
 * @author Cold Mint
 */
class ModActionAdapter(
    private val mContext: Context,
    private val mdata: List<String>,
    private val mModpath: String,
    private val mModFragment: ModFragment,
    private val mBottomSheetDialog: BottomSheetDialog
) : BaseAdapter() {
    private var modConfigurationData: ModConfigurationData? = null
    private val handler = Handler(Looper.getMainLooper())

    /**
     * 设置模组配置信息
     *
     * @param modConfigurationData 模组配置
     */
    fun setModConfigurationData(modConfigurationData: ModConfigurationData?) {
        this.modConfigurationData = modConfigurationData
    }

    override fun getCount(): Int {
        return mdata.size
    }

    override fun getItem(position: Int): Any {
        return mdata[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val dialogView = ModActionItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        //LayoutInflater.from(mContext).inflate(R.layout.mod_action_item, parent, false)
        val textView = dialogView.operation
        val itemName = mdata[position]
        textView.text = itemName
        dialogView.root.setOnClickListener {
            mBottomSheetDialog.cancel()
            when (itemName) {
                mContext.getString(R.string.manages_files) -> {
                    managesFileItem()
                }
                mContext.getString(R.string.packmod) -> {
                    packmodItem()
                }
                mContext.getString(R.string.share_mod) -> {
                    shareItem()
                }
                mContext.getString(R.string.mod_action10) -> {
                    repairItem()
                }
                mContext.getString(R.string.optimization) -> {
                    optimizationItem()
                }
                mContext.getString(R.string.mod_action1) -> {
                    editUnitItem()
                }
                mContext.getString(R.string.mod_action8) -> {
                    unzipItem()
                }
                mContext.getString(R.string.mod_action2) -> {
                    editInfoItem()
                }
                mContext.getString(R.string.rename) -> {
                    renameItem()
                }
                mContext.getString(R.string.release) -> {
                    releaseItem()
                }
                mContext.getString(R.string.work_of_home_page) -> {
                    openHomePage()
                }
                mContext.getString(R.string.global_operations) -> {
                    val intent = Intent(mContext, GlobalOperationsActivity::class.java)
                    intent.putExtra("modPath", mModpath)
                    mContext.startActivity(intent)
                }
                mContext.getString(R.string.generate_error_report) -> {
//而coroutineScope会挂起所在的协程直至其内部任务(包括子协程)执行完成，它不会阻塞所在的线程。
//coroutineScope是一个挂起函数，它被挂起后，会转而执行之前的子协程。
                    val scope = CoroutineScope(Job())
                    scope.launch {
                        //启动协程
                        var dialog: MaterialDialog? = null
                        handler.post {
                            dialog = MaterialDialog(mContext).show {
                                title(R.string.generate_error_report).message(R.string.loading_data)
                                    .positiveButton(R.string.dialog_ok).cancelable(false)
                            }
                        }
                        val gson = Gson()
                        val modClass = ModClass(File(mModpath))
                        val modErrorReport = ModErrorReport(modClass.modFile.absolutePath)
                        val outputFolder =
                            File(AppSettings.dataRootDirectory + "/modErrorReport/" + modClass.modName)
                        val warningFolder = File(outputFolder.absolutePath + "/warning")
                        if (!warningFolder.exists()) {
                            warningFolder.mkdirs()
                        }
                        val errorFolder = File(outputFolder.absolutePath + "/error")
                        if (!errorFolder.exists()) {
                            errorFolder.mkdirs()
                        }
                        val fileFinder2 = FileFinder2(modClass.modFile)
                        fileFinder2.findMode = true
                        fileFinder2.asRe = true
                        fileFinder2.findRule = modConfigurationData?.sourceFileFilteringRule
                            ?: ".+\\.ini|.+\\.template"
                        val codeCompiler2 = CodeCompiler2.getInstance(mContext)
                        val apkFolder = GameSynchronizer.getPackAgeFolder(
                            mContext, AppSettings.getInstance(mContext).getValue(
                                AppSettings.Setting.GamePackage,
                                GlobalMethod.DEFAULT_GAME_PACKAGE
                            )
                        )
                        fileFinder2.setFinderListener(object : FileFinderListener {
                            override fun whenFindFile(file: File): Boolean {
                                val code = FileOperator.readFile(file)
                                if (code != null) {
                                    handler.post {
                                        dialog?.message(text = file.absolutePath)
                                    }
                                    codeCompiler2.translation(
                                        code,
                                        object : CodeTranslatorListener {
                                            override fun beforeTranslate() {

                                            }

                                            override fun onTranslateComplete(code: String) {
                                                codeCompiler2.compile(
                                                    code,
                                                    CompileConfiguration(
                                                        mContext,
                                                        OpenedSourceFile(file),
                                                        modClass, apkFolder
                                                    ), object : CodeCompilerListener {
                                                        override fun onCompilationComplete(
                                                            compileConfiguration: CompileConfiguration,
                                                            code: String
                                                        ) {
                                                            //如果有错误或警告那么创建文件
                                                            if (compileConfiguration.getWarningNumber() > 0) {
                                                                val outFile =
                                                                    File(
                                                                        warningFolder.absolutePath +
                                                                                "/" + FileOperator.getPrefixName(
                                                                            file.name
                                                                        ) + ".json"
                                                                    )
                                                                val warningList =
                                                                    ArrayList<String>()
                                                                compileConfiguration.getAnalysisResult()
                                                                    .forEach {
                                                                        if (it.errorType == CompileConfiguration.ErrorType.Warning) {
                                                                            warningList.add("【" + it.lineData + "】" + it.errorInfo)
                                                                        }
                                                                    }
                                                                FileOperator.writeFile(
                                                                    outFile,
                                                                    gson.toJson(warningList)
                                                                )
                                                            }
                                                            if (compileConfiguration.getErrorNumber() > 0) {
                                                                val outFile =
                                                                    File(
                                                                        errorFolder.absolutePath +
                                                                                "/" + FileOperator.getPrefixName(
                                                                            file.name
                                                                        ) + ".json"
                                                                    )
                                                                val errorList =
                                                                    ArrayList<String>()
                                                                compileConfiguration.getAnalysisResult()
                                                                    .forEach {
                                                                        if (it.errorType == CompileConfiguration.ErrorType.Error) {
                                                                            errorList.add("【" + it.lineData + "】" + it.errorInfo)
                                                                        }
                                                                    }
                                                                FileOperator.writeFile(
                                                                    outFile,
                                                                    gson.toJson(errorList)
                                                                )
                                                            }
                                                            modErrorReport.addFileReport(
                                                                compileConfiguration
                                                            )
                                                        }

                                                        override fun beforeCompilation() {

                                                        }

                                                        override fun onClickKeyNotFoundItem(
                                                            lineNum: Int,
                                                            columnNum: Int,
                                                            view: View,
                                                            code: String,
                                                            section: String
                                                        ) {

                                                        }

                                                        override fun onClickValueTypeErrorItem(
                                                            lineNum: Int,
                                                            columnNum: Int,
                                                            view: View,
                                                            valueType: ValueTypeInfo
                                                        ) {

                                                        }

                                                        override fun onClickSectionIndexError(
                                                            lineNum: Int,
                                                            columnNum: Int,
                                                            view: View,
                                                            sectionName: String
                                                        ) {

                                                        }

                                                        override fun onClickResourceErrorItem(
                                                            lineNum: Int,
                                                            columnNum: Int,
                                                            view: View,
                                                            resourceFile: File
                                                        ) {

                                                        }

                                                        override fun onClickSectionErrorItem(
                                                            lineNum: Int,
                                                            view: View,
                                                            displaySectionName: String
                                                        ) {

                                                        }

                                                        override fun onClickSynchronizationGame(
                                                            lineNum: Int,
                                                            columnNum: Int,
                                                            view: View
                                                        ) {

                                                        }

                                                        override fun onClickSectionNameErrorItem(
                                                            lineNum: Int,
                                                            columnNum: Int,
                                                            view: View,
                                                            sectionName: String,
                                                            symbolIndex: Int?,
                                                            needName: Boolean
                                                        ) {

                                                        }

                                                        override fun onClickCodeIndexErrorItem(
                                                            lineNum: Int,
                                                            view: View,
                                                            sectionName: String
                                                        ) {

                                                        }

                                                        override fun onShowCompilationResult(code: String): Boolean {
                                                            return true
                                                        }

                                                    }
                                                )
                                            }

                                        })
                                }
                                return true
                            }

                            override fun whenFindFolder(folder: File): Boolean {
                                return true
                            }

                        })
                        val a = fileFinder2.onStart()
                        FileOperator.writeFile(
                            File(outputFolder.absolutePath + "/list.json"),
                            gson.toJson(modErrorReport)
                        )
                        handler.post {
                            dialog?.message(R.string.generate_error_report_ok)
                        }
                    }
                }
                else -> {
                    if (modConfigurationData != null) {
                        val title = modConfigurationData!!.updateTitle
                        if ((title == itemName)) {
                            val type = modConfigurationData!!.updateType
                            if ((type == ModConfigurationManager.qqGroupType)) {
                                try {
                                    val integer = Integer.valueOf(
                                        modConfigurationData!!.updateLink
                                    )
                                    val result = AppOperator.openQQGroupCard(
                                        mContext, integer
                                    )
                                    if (!result) {
                                        Toast.makeText(
                                            mContext,
                                            R.string.open_qq_group_card_error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: NumberFormatException) {
                                    Toast.makeText(
                                        mContext,
                                        R.string.qq_group_error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else if ((type == ModConfigurationManager.webLinkType)) {
                                val result = AppOperator.useBrowserAccessWebPage(
                                    mContext, modConfigurationData!!.updateLink
                                )
                                if (!result) {
                                    Toast.makeText(
                                        mContext,
                                        R.string.not_found_activity,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    mContext,
                                    R.string.update_type_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(mContext, "没有设置事件", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return dialogView.root
    }


    /**
     * 打开作品首页
     */
    private fun openHomePage() {
        val modId = modConfigurationData?.modId
        if (modId != null) {
            val bundle = Bundle()
            bundle.putString("modId", modId)
            bundle.putString("modName", modId)
            val intent = Intent(mContext, WebModInfoActivity::class.java)
            intent.putExtra("data", bundle)
            mContext.startActivity(intent)
        }
    }

    /**
     * 点击了发布
     */
    private fun releaseItem() {
        val releaseBundle = Bundle()
        val modId = modConfigurationData?.modId
        releaseBundle.putString("modPath", mModpath)
        if (modId == null) {
            releaseBundle.putString("mode", "firstReleaseMode")
        } else {
            releaseBundle.putString("mode", "loadMode")
            releaseBundle.putString("modId", modId)
        }
        val intent = Intent(mContext, ReleaseModActivity::class.java)
        intent.putExtra("data", releaseBundle)
        mContext.startActivity(intent)
    }

    /**
     * 点击了重命名
     */
    private fun renameItem() {
        val mod_file = File(mModpath)
        val oldname = ModClass(mod_file).modName
        InputDialog(mContext).setInputCanBeEmpty(false).setTitle(R.string.rename).setMaxNumber(255)
            .setHint(R.string.file_name).setText(oldname)
            .setPositiveButton(R.string.dialog_ok) { string ->
                if (string.isNotEmpty() && string != oldname) {
                    val newFile = File(FileOperator.getSuperDirectory(mod_file) + "/" + string)
                    mod_file.renameTo(newFile)
                    mModFragment.loadMods()
                }
                true
            }.setNegativeButton(R.string.dialog_cancel) {

            }.setCancelable(false).show()
    }

    /**
     * 点击了编辑信息
     */
    private fun editInfoItem() {
        val fileBundle = Bundle()
        val infoIntent = Intent(mContext, EditModInfoActivity::class.java)
//        val modClass = ModClass(File(mModpath))
        //fileBundle.putString("infoPath", modClass.getInfoFile().getAbsolutePath());
        fileBundle.putString("modPath", mModpath)
        infoIntent.putExtra("data", fileBundle)
        mContext.startActivity(infoIntent)
    }

    /**
     * 点击了解压
     */
    private fun unzipItem() {
        val uzipThread = UnzipThread()
        uzipThread.start()
    }

    /**
     * 点击编辑单位
     */
    private fun editUnitItem() {
        val bundle = Bundle()
        val uintent = Intent(mContext, UnitsActivity::class.java)
        bundle.putString("path", mModpath)
        uintent.putExtra("data", bundle)
        mContext.startActivity(uintent)
    }

    /**
     * 点击了管理文件
     */
    private fun managesFileItem() {
        val managesIntent = Intent(mContext, FileManagerActivity::class.java)
        val configurationBundle = Bundle()
        managesIntent.putExtra("data", configurationBundle)
        configurationBundle.putString("type", "default")
        configurationBundle.putString("path", mModpath)
        mContext.startActivity(managesIntent)
    }

    /**
     * 点击了打包
     */
    private fun packmodItem() {
        val packIntent = Intent(mContext, PackActivity::class.java)
        val packData = Bundle()
        packIntent.putExtra("data", packData)
        packData.putString("modPath", mModpath)
        mContext.startActivity(packIntent)
    }

    /**
     * 点击了优化
     */
    private fun optimizationItem() {
        val oBundle = Bundle()
        oBundle.putString("modPath", mModpath)
        val intent = Intent(mContext, OptimizeActivity::class.java)
        intent.putExtra("data", oBundle)
        mContext.startActivity(intent)
    }

    /**
     * 点击修复项目时
     */
    private fun repairItem() {
        val repairThread = RepairThread()
        repairThread.start()
    }

    /**
     * 点击分享项目时
     */
    private fun shareItem() {
        val file = File(mModpath)
        if (file.isDirectory) {
            val appSettings = AppSettings.getInstance(mContext)
            val needShowTip = appSettings.getValue(AppSettings.Setting.ShareTip, true)
            if (needShowTip) {
                val materialDialog = MaterialDialog(mContext, MaterialDialog.DEFAULT_BEHAVIOR)
                materialDialog.title(R.string.packmod, null)
                materialDialog.message(R.string.share_tip, null, null)
                materialDialog.positiveButton(
                    R.string.dialog_ok,
                    null
                ) { materialDialog: MaterialDialog? ->
                    packShare(file)
                    null
                }
                materialDialog.negativeButton(
                    R.string.no_longer_prompt,
                    null
                ) { materialDialog: MaterialDialog? ->
                    appSettings.setValue(AppSettings.Setting.ShareTip, false)
                    packShare(file)
                    null
                }
                materialDialog.show()
            } else {
                packShare(file)
            }
        } else {
            FileOperator.shareFile(mContext, file)
        }
    }

    /**
     * 打包分享事件（对话框）
     *
     * @param file 文件夹
     */
    private fun packShare(file: File) {
        val modClass = ModClass(file)
        val materialDialog = MaterialDialog(mContext)
        Thread(object : Runnable {
            override fun run() {
                handler.post {
                    materialDialog.title(R.string.packmod).message(
                        text =
                        String.format(
                            mContext.getString(R.string.dialog_packing),
                            modClass.modName
                        )
                    ).cancelable(false).positiveButton(R.string.dialog_close2) {
                        Snackbar.make(
                            (mModFragment.view)!!,
                            R.string.dialog_close_tip3,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }.show()
                }
                val cacheDirectory = File(mContext.cacheDir.absolutePath + "/share/mod")
                if (!cacheDirectory.exists()) {
                    cacheDirectory.mkdirs()
                }
                val toFile =
                    File(cacheDirectory.absolutePath + "/" + file.name + ".rwmod")
                val compressionManager = CompressionManager.instance
                compressionManager.compression(file, toFile, object : CompressionListener {
                    override fun whenCompressionFile(file: File): Boolean {
                        handler.post {
                            materialDialog.message(
                                text =
                                String.format(
                                    mContext.getString(R.string.dialog_packing),
                                    file.name
                                )
                            )
                        }
                        return true
                    }

                    override fun whenCompressionFolder(folder: File): Boolean {
                        handler.post {
                            materialDialog.message(
                                text =
                                String.format(
                                    mContext.getString(R.string.dialog_packing),
                                    folder.name
                                )
                            )
                        }
                        return true
                    }

                    override fun whenCompressionComplete(result: Boolean) {
                        handler.post {
                            if (result) {
                                materialDialog.title(R.string.share_mod).message(
                                    text =
                                    String.format(
                                        mContext.getString(R.string.pack_success),
                                        modClass.modName
                                    )
                                ).clearPositiveListeners().positiveButton(R.string.share_mod) {
                                    FileOperator.shareFile(
                                        mContext, toFile
                                    )
                                }.negativeButton(R.string.dialog_cancel) {
                                    toFile.delete()
                                }.show()
                            } else {
                                materialDialog.dismiss()
                                Snackbar.make(
                                    (mModFragment.view)!!,
                                    R.string.pack_failed,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }, null)
            }
        }).start()
    }

    internal inner class RepairThread() : Thread(), Runnable {
        private var modpath = mModpath
        lateinit var materialDialog: MaterialDialog

        //设置模组路径
        fun setModpath(modpath: String) {
            this.modpath = modpath
        }

        override fun run() {
            super.run()
            val modClass = ModClass(File(modpath))
            handler.post {
                materialDialog = MaterialDialog(mContext).title(R.string.dialog_title3).message(
                    text = String.format(
                        mContext.getString(R.string.dialog_repair), modClass.modName
                    )
                ).cancelable(false).positiveButton(R.string.dialog_close2) {
                    Snackbar.make(
                        (mModFragment.view)!!,
                        R.string.dialog_close_tip2,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                materialDialog.show()
            }
            val fileFinder = FileFinder2(modClass.modFile)
            var targetFile: File? = null
            fileFinder.setFinderListener(object : FileFinderListener {
                override fun whenFindFile(file: File): Boolean {
                    handler.post {
                        materialDialog.message(
                            text = String.format(
                                mContext.getString(R.string.dialog_search), file.name
                            )
                        )
                    }
                    val fileName = file.name.lowercase()
                    val result = fileName == modClass.INFOFILENAME
                    if (result) {
                        targetFile = file
                    }
                    return !result
                }

                override fun whenFindFolder(folder: File): Boolean {
                    return true
                }

            })
            fileFinder.onStart()
            val findFile = targetFile?.parentFile
            if (findFile != null) {
                val modFile = modClass.modFile
                val ok =
                    FileOperator.removeFiles(findFile, modFile, object : RemoveAndCopyListener {
                        override fun whenOperatorFile(file: File) {
                            handler.post {
                                materialDialog.message(
                                    text = String.format(
                                        mContext.getString(R.string.dialog_remove), file.name
                                    )
                                )
                            }
                        }

                    })
                if (ok) {
                    handler.post {
                        materialDialog.dismiss()
                        mModFragment.loadMods()
                        Snackbar.make(
                            (mModFragment.view)!!,
                            R.string.repair_complete,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    handler.post {
                        materialDialog.dismiss()
                        Snackbar.make(
                            (mModFragment.view)!!,
                            R.string.repair_complete,
                            Snackbar.LENGTH_SHORT
                        ).setAction(R.string.create) {
                            createInfoFile(modClass, false)
                        }.show()
                    }
                }
            } else {
                createInfoFile(modClass)
            }
        }

        /**
         * 创建信息文件并显示修复完成
         * @param modClass ModClass
         * @param needShowSnackbar 是否要提示
         */
        fun createInfoFile(modClass: ModClass, needShowSnackbar: Boolean = true) {
            FileOperator.writeFile(
                modClass.infoFile,
                "[mod]\ntitle: " + modClass.modName + "\ndescription: " + mContext.getText(R.string.repair_complete)
            )
            handler.post {
                materialDialog.dismiss()
                mModFragment.loadMods()
                if (needShowSnackbar) {
                    Snackbar.make(
                        (mModFragment.view)!!,
                        R.string.repair_complete,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    internal inner class UnzipThread() : Thread(), Runnable {
        lateinit var materialDialog: MaterialDialog
        override fun run() {
            super.run()
            val main_path =
                AppSettings.getInstance(mContext).getValue(AppSettings.Setting.ModFolder, "")
            val unzip_path = File(
                main_path + FileOperator.getPrefixName(
                    File(
                        mModpath
                    )
                )
            )

            //如果模组存在
            if (unzip_path.exists()) {
                handler.post {
                    Snackbar.make(
                        (mModFragment.view)!!,
                        mContext.resources.getText(R.string.directory_error),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                return
            } else {
                handler.post {
                    materialDialog =
                        MaterialDialog(mContext).title(R.string.dialog_title2).message(
                            text = String.format(
                                (mContext.resources.getText(R.string.dialog_unziping) as String),
                                ModClass(
                                    File(
                                        mModpath
                                    )
                                ).modName
                            )
                        ).cancelable(false).positiveButton(R.string.dialog_close2) {
                            Snackbar.make(
                                (mModFragment.view)!!,
                                mContext.resources.getText(R.string.dialog_close_tip),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    materialDialog.show()
                }
                unzip_path.mkdirs()
            }
            val compressionManager = CompressionManager.instance
            compressionManager.unzip(File(mModpath), unzip_path, object : UnzipListener {

                override fun whenUnzipFile(zipEntry: ZipEntry, file: File): Boolean {
                    handler.post {
                        materialDialog.message(
                            text = String.format(
                                (mContext.resources.getText(R.string.dialog_unziping) as String),
                                file.name
                            )
                        )
                    }
                    return true
                }

                override fun whenUnzipFolder(zipEntry: ZipEntry, folder: File): Boolean {
                    handler.post {
                        materialDialog.message(
                            text =
                            String.format(
                                (mContext.resources.getText(R.string.dialog_unziping) as String),
                                folder.name
                            )
                        )
                    }
                    return true
                }

                override fun whenUnzipComplete(result: Boolean) {
                    if (result) {
                        val appSettings = AppSettings.getInstance(mContext)
                        val keepFile = appSettings.getValue(AppSettings.Setting.KeepRwmodFile, true)
                        if (!keepFile) {
                            val modFile = File(mModpath)
                            val needRecycling = appSettings.getValue(
                                AppSettings.Setting.EnableRecoveryStation,
                                true
                            )
                            if (needRecycling) {
                                val removeFile: File
                                val removePath: String = appSettings.getValue(
                                    AppSettings.Setting.RecoveryStationFolder,
                                    mContext.filesDir.absolutePath + "/backup/"
                                ).toString() + modFile.name
                                removeFile = File(removePath)
                                FileOperator.removeFiles(modFile, removeFile)
                                handler.post {
                                    Snackbar.make(
                                        (mModFragment.view)!!, String.format(
                                            mContext.getString(R.string.recovery_prompt),
                                            modFile.name
                                        ), Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                FileOperator.delete_files(modFile)
                            }
                        }
                        handler.post {
                            materialDialog.dismiss()
                            val modClass = ModClass(unzip_path)
                            if (!modClass.hasInfo()) {
                                val repairThread = RepairThread()
                                repairThread.setModpath(unzip_path.absolutePath)
                                repairThread.start()
                            }
                            mModFragment.loadMods()
                        }
                    } else {
                        handler.post {
                            materialDialog.dismiss()
                            Snackbar.make(
                                (mModFragment.view)!!,
                                mContext.resources.getText(R.string.import_error1),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        }
    }
}