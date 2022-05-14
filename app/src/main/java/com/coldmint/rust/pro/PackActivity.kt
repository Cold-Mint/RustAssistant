package com.coldmint.rust.pro

import android.content.Intent
import android.os.Bundle
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.core.dataBean.ModConfigurationData
import android.os.Handler
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import android.os.Looper
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.coldmint.rust.core.*
import com.coldmint.rust.core.interfaces.CompressionInterceptor
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.interfaces.CompressionListener
import com.coldmint.rust.core.interfaces.LineParserEvent
import com.coldmint.rust.core.tool.FileFinder2
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.databinding.ActivityPackBinding
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Cold Mint
 */
class PackActivity : BaseActivity<ActivityPackBinding>() {
    private val executorService: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }
    private lateinit var item: Array<String>
    private lateinit var modFolder: File
    private var modName: String? = null
    private var outputFolder: String? = null
    private var outputFile: File? = null
    private var configurationManager: ModConfigurationManager? = null
    private var configurationData: ModConfigurationData? = null
    private var needRecyclingFile = false
    private var recyclePath: String? = null
    private var newFileBuilder: StringBuilder? = null
    private var sourceFileNum = 0

    //是否需要返回上级界面结果?
    private var needReturn: Boolean = false

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView)
        {
            viewBinding.toolbar.title = getText(R.string.packmod)
            setReturnButton()
            initData()
            initAction()
        }
    }


    private fun initData() {
        val intent = intent
        val bundle = intent.getBundleExtra("data")
        if (bundle == null) {
            showError("参数不合法")
        } else {
            needReturn = bundle.getBoolean("needReturn", false)
            if (needReturn) {
                viewBinding.formattingCode.isChecked = true
                viewBinding.removeEmptyLines.isChecked = true
                viewBinding.removeAnnotation.isChecked = true
                viewBinding.removeEmptyFileAndFolder.isChecked = true
                viewBinding.deleteSourceFile.isVisible = false
            }
            val modPath = bundle.getString("modPath")
            modFolder = File(modPath)
            val modClass = ModClass(modFolder)
            modName = modClass.modName
            item = resources.getStringArray(R.array.update_type_entries)
            needRecyclingFile =
                appSettings.getValue(AppSettings.Setting.EnableRecoveryStation, true)
            if (needRecyclingFile) {
                val recoveryStationFolder = StringBuilder(
                    appSettings.getValue(
                        AppSettings.Setting.RecoveryStationFolder,
                        this@PackActivity.filesDir.absolutePath + "/backup/"
                    )
                )
                if (!recoveryStationFolder.toString().endsWith("/")) {
                    recoveryStationFolder.append('/')
                }
                recoveryStationFolder.append(modName)
                recyclePath = recoveryStationFolder.toString()
                newFileBuilder = StringBuilder()
                viewBinding.deleteSourceFile.setText(R.string.recycle_source_file)
            }
            configurationManager = modClass.modConfigurationManager
            val manager = configurationManager
            val data = manager?.readData()
            if (data == null) {
                configurationData = null
            } else {
                configurationData = data
                loadInfoToView(data)
            }
            outputFolder = appSettings.getValue(
                AppSettings.Setting.PackDirectory,
                AppSettings.dataRootDirectory + "/bin/"
            )
            val folder = outputFolder
            val endValue = "/"
            if (folder != null && !folder.endsWith(endValue)) {
                outputFolder += endValue
            }
            val outputFolderObject = File(folder)
            if (!outputFolderObject.exists()) {
                outputFolderObject.mkdirs()
            }
            outputFile = File("$outputFolder$modName.rwmod")
        }

    }

    /**
     * 加载配置信息到视图
     *
     * @param data 数据
     */
    private fun loadInfoToView(data: ModConfigurationData) {
        viewBinding.filteringRules.setText(data.sourceFileFilteringRule)
        viewBinding.garbageFileFilteringRule.setText(data.garbageFileFilteringRule)
        viewBinding.updateLink.setText(data.updateLink)
        viewBinding.updateTitle.setText(data.updateTitle)
        val updateType = data.updateType
        if (updateType == ModConfigurationManager.qqGroupType) {
            viewBinding.updateSpinner.setSelection(1)
        }
    }

    private fun initAction() {
        viewBinding.packButton.setOnClickListener {
            val type = viewBinding.packButton.text.toString()
            if (type == getString(R.string.packmod)) {
                sourceFileNum = 0
                viewBinding.packCard.isVisible = true
                viewBinding.packButton.setBackgroundColor(
                    GlobalMethod.getThemeColor(
                        this@PackActivity,
                        R.attr.colorPrimaryVariant
                    )
                )
                viewBinding.packButton.setText(R.string.packing)
                viewBinding.packingTitle.setText(R.string.packmod)
                if (saveConfigurationData()) {
                    packMod()
                } else {
                    resetButton(false)
                }
            } else if (type == getString(R.string.share_mod)) {
                FileOperator.shareFile(this@PackActivity, outputFile)
            }
        }
    }

    /**
     * 重置按钮状态
     *
     * @param result 是否解压成功
     */
    private fun resetButton(result: Boolean) {
        viewBinding.packButton.setBackgroundColor(GlobalMethod.getColorPrimary(this@PackActivity))
        if (result) {
            viewBinding.packButton.setText(R.string.share_mod)
            if (needRecyclingFile && viewBinding.deleteSourceFile.isChecked) {
                val tip = String.format(getString(R.string.recovery_prompt), modName)
                viewBinding.packingState.text = tip
            } else {
                viewBinding.packingTitle.setText(R.string.packmod)
                viewBinding.packingState.setText(R.string.pack_file_success)
                viewBinding.packCard.postDelayed({
                    viewBinding.packCard.isVisible = false
                    if (needReturn) {
                        val result = Intent()
                        if (outputFile == null) {
                            setResult(RESULT_CANCELED, result)
                        } else {
                            result.putExtra("path", outputFile?.absolutePath)
                            result.putExtra("num", sourceFileNum)
                        }
                        setResult(RESULT_OK, result)
                        finish()
                    }
                }, 300)
            }
        } else {
            viewBinding.packButton.setText(R.string.packmod)
            viewBinding.packCard.isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (saveConfigurationData()) {
                    finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (saveConfigurationData()) {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 如果需要删除文件
     * 启用回收站则移动至回收站
     *
     * @param file 文件对象
     * @return 是否删除成功
     */
    private fun deleteFileIfNeed(file: File): Boolean {
        return if (needRecyclingFile) {
            newFileBuilder!!.delete(0, newFileBuilder!!.length)
            newFileBuilder!!.append(recyclePath)
            newFileBuilder!!.append(
                FileOperator.getRelativePath(
                    file,
                    modFolder
                )
            )
            if (file.isDirectory) {
                file.delete()
            } else {
                val newFile = File(newFileBuilder.toString())
                FileOperator.removeFile(file, newFile)
            }
        } else {
            file.delete()
        }
    }

    /**
     * 打包模组
     *
     * @return 打包状态
     */
    private fun packMod() {
        val handler = Handler(Looper.getMainLooper())
        executorService.execute {
            val deleteFile = viewBinding.deleteSourceFile.isChecked
            val removeEmptyFileAndFolderValue = viewBinding.removeEmptyFileAndFolder.isChecked
            val removeEmptyLinesValue = viewBinding.removeEmptyLines.isChecked
            val removeAnnotationValue = viewBinding.removeAnnotation.isChecked
            val formattingCodeValue = viewBinding.formattingCode.isChecked
            val targetFile = modFolder
            val outPutPath = outputFile ?: return@execute

            //如果需要回收文件，并且开启了文件删除
            if (needRecyclingFile && deleteFile) {
                val folder = File(recyclePath)
                if (!folder.exists()) {
                    folder.mkdirs()
                }
            }
            var temSourceFileRule = viewBinding.filteringRules.text.toString()
            if (temSourceFileRule.isEmpty()) {
                temSourceFileRule = ".+\\.ini|.+\\.template"
            }
            val sourceFileRule = temSourceFileRule
            val uselessFileRule = viewBinding.garbageFileFilteringRule.text.toString()
            val codeBuilder = StringBuilder()
            if (outputFile == null) {
                return@execute
            }
            val compressionManager = CompressionManager.instance
            val fileFinder = FileFinder2(targetFile)
            //fileFinder.isDuplicateCheckFolder = true
            compressionManager.fileFinder = fileFinder
            val packing = getString(R.string.dialog_packing)
            val lineParser = LineParser("")
            lineParser.needTrim = true

            //若移除空文件夹，则不扫描空文件夹
            fileFinder.isDetectingEmptyFolder = !removeEmptyFileAndFolderValue
            compressionManager.compression(targetFile, outPutPath, object : CompressionListener {
                override fun whenCompressionFile(file: File): Boolean {
                    if (deleteFile) {
                        deleteFileIfNeed(file)
                    }
                    val tip = String.format(packing, file.name)
                    handler.post { viewBinding.packingState.text = tip }
                    return true
                }

                override fun whenCompressionFolder(folder: File): Boolean {
                    if (deleteFile && folder.list().isEmpty()) {
                        deleteFileIfNeed(folder)
                    }
                    val tip = String.format(packing, folder.name)
                    handler.post { viewBinding.packingState.text = tip }
                    return true
                }

                override fun whenCompressionComplete(result: Boolean) {
                    handler.post { resetButton(result) }
                }
            }, object : CompressionInterceptor {
                override val sourceFileRule: String
                    get() = sourceFileRule
                override val uselessFileRule: String
                    get() = uselessFileRule

                override fun getSourceCode(file: File?): String? {
                    var code = FileOperator.readFile(file) ?: return null
                    if (code.isBlank()) {
                        return if (removeEmptyFileAndFolderValue) {
                            null
                        } else {
                            code
                        }
                    }
                    sourceFileNum++
                    //若移除注释或移除空行，其中任意一项为开启则分析代码
                    if (removeEmptyLinesValue || removeAnnotationValue) {
                        lineParser.text = code
                        codeBuilder.delete(0, codeBuilder.length)
                        lineParser.analyse(object : LineParserEvent {
                            override fun processingData(
                                lineNum: Int,
                                lineData: String,
                                isEnd: Boolean
                            ): Boolean {
                                if (lineData.isEmpty()) {
                                    if (!removeEmptyLinesValue) {
                                        codeBuilder.append(lineData)
                                    }
                                } else {
                                    if (codeBuilder.isNotEmpty()) {
                                        codeBuilder.append('\n')
                                    }
                                    if (lineData.startsWith("#")) {
                                        if (!removeAnnotationValue) {
                                            codeBuilder.append(lineData)
                                        }
                                    } else {
                                        codeBuilder.append(lineData)
                                    }
                                }
                                return true
                            }

                        })
                        code = codeBuilder.toString()
                    }
                    if (formattingCodeValue) {
                        code = CodeCompiler2.format(code).toString()
                    }
                    return code
                }
            })
        }
    }

    /**
     * 保存配置信息
     *
     * @return 是否保存成功
     */
    private fun saveConfigurationData(): Boolean {
        val deleteFile = viewBinding.deleteSourceFile.isChecked
        if (deleteFile) {
            //若删除了文件则保存失败，判断是否启用删除文件选择器退出
            return true
        }
        val updateTypeItem = item[viewBinding.updateSpinner.selectedItemPosition]
        val webLink = getString(R.string.web_link)
        val qqLink = getString(R.string.qq_group)
        val upTitle = viewBinding.updateTitle.text.toString()
        val upLink = viewBinding.updateLink.text.toString()
        var updateLinkType = ModConfigurationManager.webLinkType
        if (!upTitle.isEmpty()) {
            if (updateTypeItem == qqLink) {
                if (!upLink.matches(Regex("^\\d{8,10}"))) {
                    setErrorAndInput(viewBinding.updateLink, getString(R.string.qq_group_error))
                    return false
                }
                updateLinkType = ModConfigurationManager.qqGroupType
            } else {
                if (!upLink.matches(Regex("^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$|^https://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"))) {
                    setErrorAndInput(viewBinding.updateLink, getString(R.string.web_link_error))
                    return false
                }
            }
        }
        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())
        var modConfigurationData = configurationData
        if (modConfigurationData == null) {
            modConfigurationData = ModConfigurationData(
                buildDate = buildTime,
                sourceFileFilteringRule = viewBinding.filteringRules.text.toString(),
                garbageFileFilteringRule = viewBinding.garbageFileFilteringRule.text.toString(),
                updateLink = upLink,
                updateTitle = upTitle,
                updateType = updateLinkType
            )
        } else {
            modConfigurationData.buildDate = buildTime
            modConfigurationData.sourceFileFilteringRule =
                viewBinding.filteringRules.text.toString()
            modConfigurationData.garbageFileFilteringRule =
                viewBinding.garbageFileFilteringRule.text.toString()
            modConfigurationData.updateLink = upLink
            modConfigurationData.updateTitle = upTitle
            modConfigurationData.updateType = updateLinkType
        }
        return configurationManager!!.saveData(modConfigurationData)
    }


    override fun getViewBindingObject(): ActivityPackBinding {
        return ActivityPackBinding.inflate(layoutInflater)
    }


}