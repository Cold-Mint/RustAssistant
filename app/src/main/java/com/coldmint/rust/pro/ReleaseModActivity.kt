package com.coldmint.rust.pro

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coldmint.dialog.CoreDialog
import com.coldmint.dialog.InputDialog
import com.coldmint.rust.core.CompressionManager
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.ModConfigurationManager
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.WebModInfoData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.ProgressListener
import com.coldmint.rust.core.interfaces.UnzipListener
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.web.Dynamic
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.adapters.ScreenshotAdapter
import com.coldmint.rust.pro.databinding.ActivityReleaseModBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.LoadFileLayoutBinding
import com.github.promeg.pinyinhelper.Pinyin
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.gyf.immersionbar.ktx.fitsStatusBarView
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import kotlin.collections.ArrayList
import kotlin.text.StringBuilder

class ReleaseModActivity : BaseActivity<ActivityReleaseModBinding>() {
    private val lineParser by lazy {
        LineParser()
    }
    private val list by lazy {
        ArrayList<String>()
    }

    //    private val tags by lazy {
//        ArrayList<String>()
//    }
    lateinit var screenshotAdapter: ScreenshotAdapter
    private var modClass: ModClass? = null
    private var iconLink: String? = null
    private var unitnum = 0
    private var modFile: File? = null
    lateinit var tip: String
    var needIcon = false
    lateinit var iconCacheFile: File
    lateinit var mode: String


    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getText(R.string.release)
            setReturnButton()
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = RecyclerView.HORIZONTAL
            viewBinding.screenshotRecyclerView.layoutManager = layoutManager
            screenshotAdapter = ScreenshotAdapter(this, list)
            viewBinding.screenshotRecyclerView.adapter = screenshotAdapter
            initData()
        }
    }


    private fun initData() {
        lineParser.symbol = ","
        val intent = intent
        val bundle = intent.getBundleExtra("data")
        if (bundle == null) {
            showError("参数不合法")
        } else {
            val temMode = bundle.getString("mode")
            if (temMode == null) {
                showError("请传入启动模式")
                return
            }
            val modPath = bundle.getString("modPath")
            mode = temMode
            when (temMode) {
                "loadMode" -> {
                    //如果提交了模组路径
                    if (modPath != null) {
                        val file = File(modPath)
                        if (file.exists() && file.isDirectory) {
                            modClass = ModClass(file)
                        } else {
                            showError("文件不存在，或不是目录。")
                            return
                        }
                    }
                    val modId = bundle.getString("modId")
                    if (modId == null) {
                        showError("模组id为空")
                        return
                    }
                    val token = AppSettings.getValue(AppSettings.Setting.Token, "")
                    if (token.isBlank()) {
                        showError(getString(R.string.please_login_first))
                        return
                    }
                    WebMod.instance.getInfo(token, modId, object : ApiCallBack<WebModInfoData> {
                        override fun onResponse(t: WebModInfoData) {
                            if (t.code == ServerConfiguration.Success_Code) {
                                loadLoadModeAction(t)
                            } else {
                                showError(t.message)
                            }
                        }

                        override fun onFailure(e: Exception) {
                            showInternetError(exception = e)
                        }

                    })
                }
                "firstReleaseMode" -> {
                    //首次发布必须提交mod路径
                    if (modPath == null) {
                        showError("模组路径为空")
                        return
                    }
                    val file = File(modPath)
                    if (file.exists() && file.isDirectory) {
                        modClass = ModClass(file)
                    } else {
                        showError("文件不存在，或不是目录。")
                        return
                    }
                    loadFirstReleaseModeAction()
                }
            }
            val temMod = modClass
            val type = if (temMod == null) {
                getString(R.string.select_file)
            } else {
                getString(R.string.packmod)
            }
            viewBinding.packModButton.text = type
            viewBinding.modPathView.text =
                String.format(getString(R.string.unable_load_mod_info), type)
            tip = getString(R.string.file_upload_progress)
        }
    }

    /**
     * 加载首次发布活动
     */
    private fun loadFirstReleaseModeAction() {
        loadConventionalAction()
        val temModClass = modClass
        if (temModClass != null) {
            viewBinding.modIdEdit.setText(
                Pinyin.toPinyin(temModClass.modName, "_").lowercase(Locale.getDefault())
            )
            viewBinding.modNameEdit.setText(temModClass.modName)
            val description = temModClass.readValueFromInfoSection("description", "mod")
            if (description != null) {
                viewBinding.modDescribeEdit.setText(description)
            }
            val finalLink = temModClass.readResourceFromInfo("thumbnail")?.absolutePath
            if (finalLink != null) {
                loadIcon(finalLink)
            }
        }
    }

    /**
     * 加载“加载模式”活动
     * @param t WebModInfoData
     */
    private fun loadLoadModeAction(t: WebModInfoData) {
        loadConventionalAction()
        viewBinding.modNameEdit.setText(t.data.name)
        viewBinding.modIdEdit.setText(t.data.id)
        viewBinding.modIdEdit.isEnabled = false
        viewBinding.modUpdateInputLayout.isVisible = true
        viewBinding.versionNameEdit.setText(t.data.versionName)
        viewBinding.modDescribeEdit.setText(t.data.describe)
        val outputTags: StringBuilder = StringBuilder()
        lineParser.text = t.data.tags
        lineParser.parserSymbol = true
        lineParser.analyse { lineNum, lineData, isEnd ->
            if (lineData == lineParser.symbol) {
                outputTags.append(lineData)
            } else {
                outputTags.append(
                    lineData.substring(
                        1,
                        lineData.length - 1
                    )
                )
            }
            true
        }
        lineParser.parserSymbol = false
        viewBinding.modTagEdit.setText(outputTags.toString())
        val icon = t.data.icon
        if (icon != null && icon.isNotBlank()) {
            loadIcon(ServerConfiguration.getRealLink(icon))
        }

        val screenshots = t.data.screenshots
        if (screenshots != null && screenshots.isNotBlank()) {
            lineParser.text = screenshots
            lineParser.analyse { lineNum, lineData, isEnd ->
                screenshotAdapter.addItem(ServerConfiguration.getRealLink(lineData))
                true
            }

        }
    }

    /**
     * 加载常规活动
     */
    private fun loadConventionalAction() {
        viewBinding.modIdEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val modId = s.toString()
                checkModId(modId)
            }

        })

        viewBinding.modNameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                checkModName(name)
            }

        })

        initIconView()

        viewBinding.versionNameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                checkVersionName(name)
            }

        })

        viewBinding.addScreenshotButton.setOnClickListener {
            val fromUrl = getString(R.string.from_url)
            val selectImage = getString(R.string.select_image)
            val array = resources.getStringArray(R.array.screenshot_addType)
            MaterialAlertDialogBuilder(this).setTitle(R.string.add).setItems(array) { i, i2 ->
                val text = array[i2]
                when (text) {
                    fromUrl -> {
                        InputDialog(this).setTitle(R.string.from_url)
                            .setMessage(R.string.from_url_tip).setErrorTip { s, textInputLayout ->
                                if (s.matches(Regex("^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$|^https://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"))) {
                                    textInputLayout.error = getString(R.string.from_url_tip)
                                } else {
                                    textInputLayout.isErrorEnabled = false
                                }
                            }.setPositiveButton(R.string.dialog_ok) { input ->
                                screenshotAdapter.addItem(input)
                                true
                            }.setNegativeButton(R.string.dialog_close) {

                            }.show()
                    }
                    selectImage -> {
                        val startIntent =
                            Intent(this@ReleaseModActivity, FileManagerActivity::class.java)
                        val fileBundle = Bundle()
                        if (modClass != null) {
                            fileBundle.putString("path", modClass!!.modFile.absolutePath)
                        }
                        fileBundle.putString("type", "selectFile")
                        startIntent.putExtra("data", fileBundle)
                        startActivityForResult(startIntent, 2)
                    }
                    else -> {
                        Toast.makeText(this@ReleaseModActivity, "未知的操作", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            }.setPositiveButton(R.string.dialog_cancel) { i1, i2 ->
            }.show()
        }


        viewBinding.packModButton.setOnClickListener {
            val type = viewBinding.packModButton.text
            when (type) {
                getString(R.string.packmod) -> {
                    packModAction()
                }
                getString(R.string.select_file) -> {
                    selectModFile()
                }
            }

        }

        viewBinding.releaseButton.setOnClickListener {
            if (mode == "loadMode") {
                uploadAction(true)
            } else {
                uploadAction(false)
            }
        }


        viewBinding.modTagEdit.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val tag = s.toString()
                    checkModTag(tag)
                    lineParser.text = tag
                    var isNotEmpty = false
//                    tags.clear()
                    viewBinding.chipGroup.removeAllViews()
                    lineParser.analyse { lineNum, lineData, isEnd ->
                        isNotEmpty = true
                        if (lineData.isNotBlank()) {
//                            tags.add(lineData)
                            val chip = Chip(this@ReleaseModActivity)
                            chip.text = lineData
                            chip.setOnClickListener {
                                val bundle = Bundle()
                                bundle.putString("tag", lineData)
                                bundle.putString(
                                    "title",
                                    String.format(getString(R.string.tag_title), s)
                                )
                                bundle.putString("action", "tag")
                                val thisIntent =
                                    Intent(this@ReleaseModActivity, TagActivity::class.java)
                                thisIntent.putExtra("data", bundle)
                                startActivity(thisIntent)
                            }
                            viewBinding.chipGroup.addView(chip)
                        }
                        true
                    }
                    viewBinding.chipGroup.isVisible = isNotEmpty
                }

            })

        viewBinding.modDescribeEdit.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    checkModDescribe(text)
                }

            })

        viewBinding.modTagEdit.setText(
            String.format(
                getString(R.string.default_tags),
                AppSettings.getValue(AppSettings.Setting.Account, "未知用户")
            )
        )

    }

    private fun selectModFile() {
        val startIntent =
            Intent(this@ReleaseModActivity, FileManagerActivity::class.java)
        val fileBundle = Bundle()
        if (modClass != null) {
            fileBundle.putString("path", modClass!!.modFile.absolutePath)
        }
        fileBundle.putString("type", "selectFile")
        startIntent.putExtra("data", fileBundle)
        startActivityForResult(startIntent, 4)
    }


    private fun initIconView() {
        viewBinding.iconView.setOnClickListener {
            val popupMenu = GlobalMethod.createPopMenu(it)
            popupMenu.menu.add(R.string.from_url)
            if (needIcon) {
                popupMenu.menu.add(R.string.change_image)
                popupMenu.menu.add(R.string.del_image)
            } else {
                popupMenu.menu.add(R.string.select_image)
            }
            popupMenu.setOnMenuItemClickListener { item ->
                val title = item.title.toString()
                if (title == getString(R.string.change_image) || title == getString(R.string.select_image)) {
                    //选择文件
                    val startIntent =
                        Intent(this, FileManagerActivity::class.java)
                    val fileBundle = Bundle()
                    fileBundle.putString("type", "selectFile")
                    startIntent.putExtra("data", fileBundle)
                    startActivityForResult(startIntent, 3)
                } else if (title == getString(R.string.from_url)) {
                    InputDialog(this).setTitle(R.string.from_url).setMessage(R.string.from_url_tip)
                        .setErrorTip { s, textInputLayout ->
                            if (s.matches(Regex("^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$|^https://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"))) {
                                textInputLayout.error = getString(R.string.from_url_tip)
                            } else {
                                textInputLayout.isErrorEnabled = false
                            }
                        }.setPositiveButton(R.string.dialog_ok) { input ->
                            loadIcon(input)
                            true
                        }.setNegativeButton(R.string.dialog_close) {

                        }.show()
                } else {
                    val link = iconLink
                    if (link != null) {
                        needIcon = false
                    }
                    viewBinding.iconView.setImageResource(R.drawable.image)
                }
                false
            }
            popupMenu.show()
        }
    }


    /**
     * 加载图像
     * @param iconPath String
     */
    fun loadIcon(iconPath: String) {
        needIcon = true
        iconLink = iconPath
        Glide.with(this).load(iconPath).apply(GlobalMethod.getRequestOptions())
            .into(viewBinding.iconView)
    }

    /**
     * 打包活动
     */
    private fun packModAction() {
        val temModeClass = modClass
        if (temModeClass == null) {
            showToast("无法打包模组路径为空")
        } else {
            val packIntent = Intent(this, PackActivity::class.java)
            val packData = Bundle()
            packIntent.putExtra("data", packData)
            packData.putString("modPath", temModeClass.modFile.absolutePath)
            packData.putBoolean("needReturn", true)
            this.startActivityForResult(packIntent, 1)
        }
    }


    /**
     * 检查模组Id
     */
    fun checkModId(modId: String): Boolean {
        if (modId.isBlank()) {
            setErrorAndInput(
                viewBinding.modIdEdit,
                String.format(
                    getString(R.string.please_input_value),
                    viewBinding.modIdInputLayout.hint.toString()
                ),
                viewBinding.modIdInputLayout
            )
            return false
        } else if (modId.matches(Regex("^[A-Za-z0-9_]+\$"))) {
            viewBinding.modIdInputLayout.isErrorEnabled = false
            return true
        } else {
            setErrorAndInput(
                viewBinding.modIdEdit,
                getString(R.string.mod_id_error2),
                viewBinding.modIdInputLayout, false
            )
            return false
        }
    }

    fun checkVersionName(versionName: String): Boolean {
        return if (versionName.isBlank()) {
            setErrorAndInput(
                viewBinding.versionNameEdit,
                String.format(
                    getString(R.string.please_input_value),
                    viewBinding.versionNameInputLayout.hint.toString()
                ),
                viewBinding.versionNameInputLayout
            )
            false
        } else {
            viewBinding.versionNameInputLayout.isErrorEnabled = false
            true
        }
    }

    fun checkModDescribe(modDescribe: String): Boolean {
        if (modDescribe.isBlank()) {
            setErrorAndInput(
                viewBinding.modDescribeEdit,
                getString(R.string.describe_error),
                viewBinding.modDescribeInputLayout
            )
            return false
        } else {
            viewBinding.modDescribeInputLayout.isErrorEnabled = false
            val index = modDescribe.indexOf('\n')
            if (index > -1) {
                val show = modDescribe.subSequence(0, index)
                if (show.length > WebMod.maxDescribeLength) {
                    viewBinding.modDescribeInputLayout.helperText =
                        String.format(
                            getString(R.string.describe_tip2),
                            show.subSequence(0, WebMod.maxDescribeLength)
                        )
                } else {
                    viewBinding.modDescribeInputLayout.helperText =
                        String.format(getString(R.string.describe_tip2), show)
                }
            } else {
                if (modDescribe.length > WebMod.maxDescribeLength) {
                    viewBinding.modDescribeInputLayout.helperText =
                        String.format(
                            getString(R.string.describe_tip2),
                            modDescribe.subSequence(0, WebMod.maxDescribeLength)
                        )
                } else {
                    viewBinding.modDescribeInputLayout.helperText =
                        getString(R.string.describe_tip)
                }

            }
            return true
        }
    }

    /**
     * 检查更新日志
     * @param updateLog String
     * @return Boolean
     */
    fun checkUpdateLog(updateLog: String): Boolean {
        return if (updateLog.isBlank()) {
            setErrorAndInput(
                viewBinding.modUpdateEdit,
                String.format(
                    getString(R.string.please_input_value),
                    viewBinding.modUpdateInputLayout.hint.toString()
                ), viewBinding.modUpdateInputLayout
            )
            false
        } else {
            viewBinding.modUpdateInputLayout.isErrorEnabled = false
            true
        }
    }

    /**
     * 检查模组标签
     * @param tags String
     * @return Boolean
     */
    fun checkModTag(tags: String): Boolean {
        return if (tags.isBlank()) {
            setErrorAndInput(
                viewBinding.modTagEdit,
                getString(R.string.tags_error),
                viewBinding.modTagInputLayout
            )
            false
        } else {
            viewBinding.modTagInputLayout.isErrorEnabled = false
            true
        }
    }

    /**
     * 检查模组名
     * @param modName String
     * @return Boolean
     */
    fun checkModName(modName: String): Boolean {
        if (modName.isBlank()) {
            setErrorAndInput(
                viewBinding.modNameEdit,
                getString(R.string.name_error),
                viewBinding.modNameInputLayout
            )
            return false
        } else {
            viewBinding.modNameInputLayout.isErrorEnabled = false
            return true
        }
    }

    /**
     * 数据上传活动
     */
    private fun uploadAction(isUpdateMode: Boolean) {
        val modId = viewBinding.modIdEdit.text.toString()
        if (!checkModId(modId)) {
            return
        }

        val modName = viewBinding.modNameEdit.text.toString()
        if (!checkModName(modName)) {
            return
        }

        val modDescribe = viewBinding.modDescribeEdit.text.toString()
        if (!checkModDescribe(modDescribe)) {
            return
        }

        val tags = viewBinding.modTagEdit.text.toString()
        if (!checkModTag(tags)) {
            return
        }

        val versionName = viewBinding.versionNameEdit.text.toString()
        if (!checkVersionName(versionName)) {
            return
        }
        var updateLog: String = viewBinding.modUpdateEdit.text.toString()
        if (isUpdateMode) {
            if (!checkUpdateLog(updateLog)) {
                return
            }
        }
        val tagsBuilder = StringBuilder()
        val lineParser = LineParser(tags)
        lineParser.symbol = ","
        lineParser.parserSymbol = true
        lineParser.analyse { lineNum, lineData, isEnd ->
            if (lineData == lineParser.symbol) {
                tagsBuilder.append(lineParser.symbol)
            } else {
                tagsBuilder.append("[")
                tagsBuilder.append(lineData)
                tagsBuilder.append("]")
            }
            true
        }


        val account = AppSettings.getValue(AppSettings.Setting.Account, "")
        if (account.isBlank()) {
            Snackbar.make(
                viewBinding.releaseButton,
                R.string.please_login_first,
                Snackbar.LENGTH_SHORT
            ).setAction(R.string.login) {
                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )
            }.show()
            return
        }
        val file = modFile
        if (file == null) {
            if (modClass == null) {
                selectModFile()
            } else {
                packModAction()
            }
        } else {
            when (AppOperator.getNetworkType(this)) {
                AppOperator.NetWorkType.NetWorkType_Moble -> {
                    val useMobileNetWork =
                        AppSettings.getValue(AppSettings.Setting.UseMobileNetwork, false)
                    if (useMobileNetWork) {
                        if (isUpdateMode) {
                            updateModWork(
                                modId,
                                account,
                                modName,
                                modDescribe,
                                updateLog,
                                versionName,
                                tagsBuilder,
                                file
                            )
                        } else {
                            releaseModWork(
                                modId, modName, modDescribe, versionName,
                                tagsBuilder, file
                            )
                        }
                    } else {
                        CoreDialog(this).setTitle(R.string.using_mobile_networks)
                            .setMessage(R.string.using_mobile_networks_msg)
                            .setPositiveButton(R.string.only_one) {
                                if (isUpdateMode) {
                                    updateModWork(
                                        modId,
                                        account,
                                        modName,
                                        modDescribe,
                                        updateLog,
                                        versionName,
                                        tagsBuilder,
                                        file
                                    )
                                } else {
                                    releaseModWork(
                                        modId, modName, modDescribe, versionName,
                                        tagsBuilder, file
                                    )
                                }
                            }.setNegativeButton(R.string.always_allow) {
                                AppSettings.setValue(AppSettings.Setting.UseMobileNetwork, true)
                                if (isUpdateMode) {
                                    updateModWork(
                                        modId,
                                        account,
                                        modName,
                                        modDescribe,
                                        updateLog,
                                        versionName,
                                        tagsBuilder,
                                        file
                                    )
                                } else {
                                    releaseModWork(
                                        modId, modName, modDescribe, versionName,
                                        tagsBuilder, file
                                    )
                                }
                            }.setNeutralButton(R.string.dialog_cancel) {

                            }.show()
                    }
                }
                AppOperator.NetWorkType.NetWorkType_Wifi -> {
                    if (isUpdateMode) {
                        updateModWork(
                            modId,
                            account,
                            modName,
                            modDescribe,
                            updateLog,
                            versionName,
                            tagsBuilder,
                            file
                        )
                    } else {
                        releaseModWork(
                            modId, modName, modDescribe, versionName,
                            tagsBuilder, file
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * 发布模组任务
     * @param modId String
     * @param account String
     * @param modName String
     * @param modDescribe String
     * @param tagsBuilder StringBuilder
     */
    fun releaseModWork(
        modId: String,
        modName: String,
        modDescribe: String,
        versionName: String,
        tagsBuilder: StringBuilder, file: File
    ) {
        val loadFileLayoutBinding = LoadFileLayoutBinding.inflate(layoutInflater)
        loadFileLayoutBinding.LinearProgressIndicator.max = 100
        val dialog = MaterialAlertDialogBuilder(this).setTitle(R.string.release)
            .setView(loadFileLayoutBinding.root).setPositiveButton(R.string.dialog_ok) { i1, i2 ->
            }.setCancelable(false).show()

        WebMod.instance.releaseMod(AppSettings.getValue(AppSettings.Setting.AppID, ""), modId,
            AppSettings.getValue(AppSettings.Setting.Token, ""),
            modName,
            modDescribe,
            tagsBuilder.toString(),
            unitnum,
            iconLink = iconLink, file = file, screenshotList = list, versionName = versionName,
            apiCallBack = object : ApiCallBack<ApiResponse> {
                override fun onResponse(t: ApiResponse) {
                    dialog.dismiss()
                    if (t.code == ServerConfiguration.Success_Code) {
                        val temModClass = modClass
                        if (temModClass != null) {
                            val manager = temModClass.modConfigurationManager
                            if (manager != null) {
                                val configurationData = manager.readData()
                                configurationData?.modId = modId
                                manager.saveData(configurationData)
                            }
                        }
                        //发布动态
                        val check = viewBinding.checkbox.isChecked
                        if (check) {
                            val token = AppSettings.getValue(AppSettings.Setting.Token, "")
                            Dynamic.instance.send(
                                token,
                                String.format(getString(R.string.auto_send), modId),
                                object : ApiCallBack<ApiResponse> {
                                    override fun onResponse(t: ApiResponse) {

                                    }

                                    override fun onFailure(e: Exception) {

                                    }

                                })
                        }
                        CoreDialog(this@ReleaseModActivity).setTitle(R.string.release)
                            .setMessage(t.message).setCancelable(false)
                            .setPositiveButton(R.string.dialog_ok) {
                                finish()
                            }.show()
                    } else {
                        handleEvent(t)
                    }
                }

                override fun onFailure(e: Exception) {
                    dialog.dismiss()
                    showInternetError(viewBinding.releaseButton, e)
                }

            }, progressListener = object : ProgressListener {
                override fun onProgress(totalLength: Long, currentLength: Long) {
                    val numberFormat = NumberFormat.getNumberInstance()
                    numberFormat.maximumFractionDigits = 2
                    val progress =
                        numberFormat.format(currentLength.toDouble() / totalLength.toDouble() * 100)
                    val processNum = progress.toFloat().toInt()
                    runOnUiThread {
                        loadFileLayoutBinding.LinearProgressIndicator.progress = processNum
                        if (processNum == 100) {
                            loadFileLayoutBinding.tipView.setText(R.string.file_upload_ok)
                        } else {
                            loadFileLayoutBinding.tipView.text = String.format(tip, progress)
                        }
                    }

                }

            })
    }


    /**
     * 更新模组任务
     * @param modId String
     * @param account String
     * @param modName String
     * @param modDescribe String
     * @param tagsBuilder StringBuilder
     */
    fun updateModWork(
        modId: String,
        account: String,
        modName: String,
        modDescribe: String,
        updateLog: String,
        versionName: String,
        tagsBuilder: StringBuilder, file: File
    ) {
        val loadFileLayoutBinding = LoadFileLayoutBinding.inflate(layoutInflater)
        loadFileLayoutBinding.LinearProgressIndicator.max = 100

        val dialog = MaterialAlertDialogBuilder(this).setTitle(R.string.release)
            .setView(loadFileLayoutBinding.root).setPositiveButton(R.string.dialog_ok) { i1, i2 ->
            }.setCancelable(false).show()


        WebMod.instance.updateMod(AppSettings.getValue(AppSettings.Setting.AppID, ""), modId,
            account,
            modName,
            modDescribe,
            updateLog,
            tagsBuilder.toString(),
            unitnum,
            iconLink = iconLink, file = file, screenshotList = list, versionName = versionName,
            apiCallBack = object : ApiCallBack<ApiResponse> {
                override fun onResponse(t: ApiResponse) {
                    dialog.dismiss()
                    if (t.code == ServerConfiguration.Success_Code) {
                        val temModClass = modClass
                        if (temModClass != null) {
                            val manager = temModClass.modConfigurationManager
                            if (manager != null) {
                                val configurationData = manager.readData()
                                configurationData?.modId = modId
                                manager.saveData(configurationData)
                            }
                        }
                        //发布动态
                        val check = viewBinding.checkbox.isChecked
                        if (check) {
                            val token = AppSettings.getValue(AppSettings.Setting.Token, "")
                            Dynamic.instance.send(
                                token,
                                String.format(
                                    getString(R.string.auto_send2),
                                    modId,
                                    versionName,
                                    updateLog
                                ),
                                object : ApiCallBack<ApiResponse> {
                                    override fun onResponse(t: ApiResponse) {

                                    }

                                    override fun onFailure(e: Exception) {

                                    }

                                })
                        }

                        CoreDialog(this@ReleaseModActivity).setTitle(R.string.release).setMessage(
                            t.message
                        ).setPositiveButton(R.string.dialog_ok) {
                            finish()
                        }.setCancelable(false).show()
                    } else {
                        handleEvent(t)
                    }
                }

                override fun onFailure(e: Exception) {
                    dialog.dismiss()
                    showInternetError(viewBinding.releaseButton, e)
                }

            }, progressListener = object : ProgressListener {
                override fun onProgress(totalLength: Long, currentLength: Long) {
                    val numberFormat = NumberFormat.getNumberInstance()
                    numberFormat.maximumFractionDigits = 2
                    val progress =
                        numberFormat.format(currentLength.toDouble() / totalLength.toDouble() * 100)
                    val processNum = progress.toFloat().toInt()
                    runOnUiThread {
                        loadFileLayoutBinding.LinearProgressIndicator.progress = processNum
                        if (processNum == 100) {
                            loadFileLayoutBinding.tipView.setText(R.string.file_upload_ok)
                        } else {
                            loadFileLayoutBinding.tipView.text = String.format(tip, progress)
                        }
                    }

                }

            })
    }

    /**
     * 处理错误事件
     * @param t ApiResponse
     */
    fun handleEvent(t: ApiResponse) {
        val data = t.data
        if (data != null && ServerConfiguration.isEvent(data)) {
            when (data) {
                "@event:模组名占用" -> {
                    setErrorAndInput(
                        viewBinding.modNameEdit,
                        getString(R.string.mod_name_error),
                        viewBinding.modNameInputLayout
                    )
                }
                "@event:Id占用" -> {
                    setErrorAndInput(
                        viewBinding.modIdEdit,
                        getString(R.string.mod_id_error3),
                        viewBinding.modIdInputLayout
                    )
                }
                "@event:版本名占用" -> {
                    setErrorAndInput(
                        viewBinding.versionNameEdit,
                        String.format(
                            getString(R.string.version_name_error2),
                            viewBinding.versionNameEdit.text.toString(),
                            viewBinding.versionNameInputLayout
                        )
                    )
                }
                else -> {
                    Snackbar.make(
                        viewBinding.releaseButton,
                        t.message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Snackbar.make(
                viewBinding.releaseButton,
                t.message,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        if (resultCode == RESULT_OK) {
            //打包返回
            when (requestCode) {
                1 -> {
                    val path = data.getStringExtra("path")
                    val num = data.getIntExtra("num", 0)
                    unitnum = num
                    modFile = File(path)
                    val pathTip = String.format(getString(R.string.load_mod_file), path)
                    val numTip = String.format(getString(R.string.unit_num), num)
                    viewBinding.modPathView.text = pathTip
                    viewBinding.unitNumberView.isVisible = true
                    viewBinding.unitNumberView.text = numTip
                    viewBinding.packModButton.isVisible = false
                }
                2 -> {
                    //选择截图返回
                    val path = data.getStringExtra("File")
                    if (path != null) {
                        val file = File(path)
                        when (FileOperator.getFileType(file)) {
                            "png", "jpg" -> {
                                screenshotAdapter.addItem(path)
                            }
                            else -> {
                                Snackbar.make(
                                    viewBinding.releaseButton,
                                    R.string.bad_file_type,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                3 -> {
                    //选择图标返回
                    val filePath = data.getStringExtra("File") ?: return
                    val newIconFile = File(filePath)
                    val type = FileOperator.getFileType(newIconFile)
                    if (type == "png" || type == "jpg") {
                        val bitmap = BitmapFactory.decodeFile(newIconFile.absolutePath)
                        if (bitmap.height == bitmap.width) {
                            loadIcon(filePath)
                        } else {
                            val cacheFolder = File(cacheDir.absolutePath + "/System/Images")
                            if (!cacheFolder.exists()) {
                                cacheFolder.mkdirs()
                            }
                            iconCacheFile = File(cacheFolder.absolutePath + "/" + newIconFile.name)
                            UCrop.of(
                                Uri.parse(newIconFile.toURI().toString()),
                                Uri.parse(iconCacheFile.toURI().toString())
                            ).withAspectRatio(1f, 1f).start(this)
                        }
                    } else {
                        Snackbar.make(
                            viewBinding.releaseButton,
                            getString(R.string.bad_file_type),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                4 -> {
//选择压缩文件返回
                    val executorService = Executors.newSingleThreadExecutor()
                    executorService.submit {
                        val filePath = data.getStringExtra("File") ?: return@submit
                        val file = File(filePath)
                        val type = FileOperator.getFileType(file)
                        if (type == "zip" || type == "rwmod") {
                            val cacheFolder =
                                File(cacheDir.toString() + "/mod/" + FileOperator.getPrefixName(file))
                            if (!cacheFolder.exists()) {
                                cacheFolder.mkdirs()
                            }
                            var num = 0
                            runOnUiThread {
                                viewBinding.modPathView.text = getString(R.string.calculating)
                                viewBinding.packModButton.isVisible = false
                            }
                            val compressionManager = CompressionManager.instance
                            compressionManager.unzip(file, cacheFolder, object : UnzipListener {
                                override fun whenUnzipFile(
                                    zipEntry: ZipEntry,
                                    file: File
                                ): Boolean {
                                    if (file.name.matches(Regex(".+\\.ini|.+\\.template"))) {
                                        num++
                                    }
                                    return true
                                }

                                override fun whenUnzipFolder(
                                    zipEntry: ZipEntry,
                                    folder: File
                                ): Boolean {
                                    return true
                                }

                                override fun whenUnzipComplete(result: Boolean) {
                                    runOnUiThread {
                                        unitnum = num
                                        modFile = file
                                        val pathTip = String.format(
                                            getString(R.string.load_mod_file),
                                            filePath
                                        )
                                        val numTip =
                                            String.format(getString(R.string.unit_num), num)
                                        viewBinding.modPathView.text = pathTip
                                        viewBinding.unitNumberView.isVisible = true
                                        viewBinding.unitNumberView.text = numTip
                                    }
                                }

                            })
                        } else {
                            runOnUiThread {
                                Snackbar.make(
                                    viewBinding.releaseButton,
                                    getString(R.string.bad_file_type),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                UCrop.REQUEST_CROP -> {
                    //val resultUri = UCrop.getOutput(data)
                    loadIcon(iconCacheFile.absolutePath)
                }
            }
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityReleaseModBinding {
        return ActivityReleaseModBinding.inflate(layoutInflater)
    }


}