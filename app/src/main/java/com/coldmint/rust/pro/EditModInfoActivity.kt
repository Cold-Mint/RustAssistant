package com.coldmint.rust.pro


import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.core.ModClass
import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import android.content.res.ColorStateList
import com.bumptech.glide.Glide
import android.graphics.BitmapFactory
import com.yalantis.ucrop.UCrop

import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Build
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.adapters.MapAndMusicAdapter
import com.coldmint.rust.pro.databinding.ActivityEditModInfoBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import java.io.File
import java.lang.StringBuilder
import java.util.ArrayList

class EditModInfoActivity : BaseActivity<ActivityEditModInfoBinding>() {
    private var mExpandMusicList = false
    private var mExpandMapList = false
    private var mNeedIcon = false
    private lateinit var mModClass: ModClass
    private lateinit var dataBaseFiles: DataBaseFiles

    data class DataBaseFiles(
        private val oldFile: File,
        private val oldShmFile: File,
        private val oldWalFile: File
    ) {
        /**
         * 重命名数据库
         * @param dataBaseFiles DataBaseFiles
         */
        fun renameTo(dataBaseFiles: DataBaseFiles) {
            oldFile.renameTo(dataBaseFiles.oldFile)
            oldShmFile.renameTo(dataBaseFiles.oldShmFile)
            oldWalFile.renameTo(dataBaseFiles.oldWalFile)
        }
    }


    /**
     * 创建文件数据库
     * @param name
     */
    fun createDataBaseFiles(name: String): DataBaseFiles {
        val dataBasePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationContext.dataDir.absolutePath + "/databases/"
        } else {
            FileOperator.getSuperDirectory(
                cacheDir
            ) + "/databases/"
        }
        return DataBaseFiles(
            File(dataBasePath + name),
            File(dataBasePath + name + "-shm"),
            File(dataBasePath + name + "-wal")
        )
    }


    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getText(R.string.mod_action2)
            setReturnButton()
            val intent = intent
            val bundle = intent.getBundleExtra("data")
            if (bundle == null) {
                showError("无效的文件")
                return
            }
            val modPath = bundle.getString("modPath")
            mModClass = ModClass(File(modPath))
            dataBaseFiles = createDataBaseFiles(mModClass.modName)
            initData()
            initAction()
        }
    }

    /**
     * 加载默认图像
     */
    fun loadDefaultImage() {
        val drawable = getDrawable(R.drawable.image)
        viewBinding.iconView.setImageDrawable(
            GlobalMethod.tintDrawable(
                drawable,
                ColorStateList.valueOf(GlobalMethod.getColorPrimary(this))
            )
        )
    }


    fun initData() {
        val name = mModClass.readValueFromInfoSection("title", "mod")
        if (name != null) {
            viewBinding.modNameEdit.setText(name)
        }
        val description = mModClass.readValueFromInfoSection("description", "mod")
        if (description != null) {
            viewBinding.modDescribeEdit.setText(description)
        }
        val modIcon = mModClass.modIcon
        if (modIcon == null) {
            loadDefaultImage()
        } else {
            Glide.with(this@EditModInfoActivity).load(modIcon)
                .apply(GlobalMethod.getRequestOptions()).into(viewBinding.iconView)
            mNeedIcon = true
        }
        val musicSourceFolder = mModClass.readValueFromInfoSection("sourceFolder", "music")
        if (musicSourceFolder == null) {
            enableMusic(false)
        } else {
            enableMusic(true)
            val playExclusivelyData = mModClass.readValueFromInfoSection(
                "whenUsingUnitsFromThisMod_playExclusively",
                "music"
            )
            if (playExclusivelyData == null) {
                viewBinding.playExclusively.isChecked = false
            } else {
                viewBinding.playExclusively.isChecked = playExclusivelyData == "true"
            }
        }
        val mapSourceFolder = mModClass.readValueFromInfoSection("sourceFolder", "maps")
        if (mapSourceFolder == null) {
            enableMap(false)
        } else {
            enableMap(true)
            val addExtraMapsForPath =
                mModClass.readValueFromInfoSection("addExtraMapsForPath", "maps")
            if (addExtraMapsForPath == null) {
                viewBinding.addExtraMapsForPathSwitch.isChecked = false
            } else {
                viewBinding.addExtraMapsForPathSwitch.isChecked = addExtraMapsForPath == "true"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (saveInfoFile()) {
                    finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (saveInfoFile()) {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 保存信息文件
     *
     * @return true保存成功, false保存失败。
     */
    private fun saveInfoFile(): Boolean {
        val resultBuilder = StringBuilder()
        resultBuilder.append("[mod]\ntitle:")
        val modName = viewBinding.modNameEdit.text.toString()
        if (modName.isBlank()) {
            setErrorAndInput(
                viewBinding.modNameEdit,
                getString(R.string.name_error),
                viewBinding.modNameInputLayout
            )
            return false
        }
        resultBuilder.append(modName)
        resultBuilder.append("\ndescription:")
        val description = viewBinding.modDescribeEdit.text.toString()
        if (description.isBlank()) {
            setErrorAndInput(
                viewBinding.modDescribeEdit,
                getString(R.string.describe_error),
                viewBinding.modDescribeInputLayout
            )
            return false
        }
        if (description.contains("\n")) {
            setErrorAndInput(
                viewBinding.modDescribeEdit,
                getString(R.string.describe_error2),
                viewBinding.modDescribeInputLayout
            )
            return false
        }
        resultBuilder.append(description)
        if (mNeedIcon) {
            var iconName = mModClass.readValueFromInfoSection("thumbnail", "mod")
            if (iconName == null) {
                iconName = "icon.png"
            }
            resultBuilder.append("\nthumbnail:")
            resultBuilder.append(iconName)
        }
        val enabledMusicData = viewBinding.enabledMusic.text.toString()
        if (enabledMusicData == getString(R.string.disabled)) {
            var sourceFolder = mModClass.readValueFromInfoSection("sourceFolder", "music")
            if (sourceFolder == null) {
                sourceFolder = "music/"
            }
            resultBuilder.append("\n[music]\nsourceFolder:")
            resultBuilder.append(sourceFolder)
            if (viewBinding.playExclusively.isChecked) {
                resultBuilder.append("\nwhenUsingUnitsFromThisMod_playExclusively:true")
            }
        }
        val enabledMapData = viewBinding.enabledMap.text.toString()
        if (enabledMapData == getString(R.string.disabled)) {
            var sourceFolder = mModClass.readValueFromInfoSection("sourceFolder", "maps")
            if (sourceFolder == null) {
                sourceFolder = "maps/"
            }
            resultBuilder.append("\n[maps]\nsourceFolder:")
            resultBuilder.append(sourceFolder)
            if (viewBinding.addExtraMapsForPathSwitch.isChecked) {
                resultBuilder.append("\naddExtraMapsForPath:true")
            }
        }

        //更新缓存目录
        val newDataBase = createDataBaseFiles(modName)
        dataBaseFiles.renameTo(newDataBase)
        return FileOperator.writeFile(mModClass.infoFile, resultBuilder.toString())
    }

    fun initAction() {
        viewBinding.modNameEdit.addTextChangedListener {
            viewBinding.modNameInputLayout.isErrorEnabled = false
        }

        viewBinding.modDescribeEdit.addTextChangedListener {
            viewBinding.modDescribeInputLayout.isErrorEnabled = false
        }

        viewBinding.iconView.setOnClickListener {
            val popupMenu = GlobalMethod.createPopMenu(it)
            if (mNeedIcon) {
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
                        Intent(this@EditModInfoActivity, FileManagerActivity::class.java)
                    val fileBundle = Bundle()
                    fileBundle.putString("type", "selectFile")
                    fileBundle.putString("path", mModClass.modFile.absolutePath)
                    startIntent.putExtra("data", fileBundle)
                    startActivityForResult(startIntent, 3)
                } else {
                    val iconFile = iconPath
                    if (iconFile.exists()) {
                        iconFile.delete()
                        mNeedIcon = false
                    }
                    loadDefaultImage()
                }
                false
            }
            popupMenu.show()
        }
        viewBinding.expandMusicList.setOnClickListener { showMusicConfigurationView(mExpandMusicList) }
        viewBinding.expandMapList.setOnClickListener { showMapConfigurationView(mExpandMapList) }
        viewBinding.enabledMusic.setOnClickListener {
            val type = viewBinding.enabledMusic.text.toString()
            enableMusic(type == getString(R.string.enabled))
        }
        viewBinding.enabledMap.setOnClickListener {
            val type = viewBinding.enabledMap.text.toString()
            enableMap(type == getString(R.string.enabled))
        }
        viewBinding.addMusic.setOnClickListener { //选择文件
            val startIntent = Intent(this@EditModInfoActivity, FileManagerActivity::class.java)
            val fileBundle = Bundle()
            fileBundle.putString("type", "selectFile")
            fileBundle.putString("path", mModClass.modFile.absolutePath)
            startIntent.putExtra("data", fileBundle)
            startActivityForResult(startIntent, 1)
        }
        viewBinding.addMap.setOnClickListener { //选择文件
            val startIntent = Intent(this@EditModInfoActivity, FileManagerActivity::class.java)
            val fileBundle = Bundle()
            fileBundle.putString("type", "selectFile")
            fileBundle.putString("path", mModClass.modFile.absolutePath)
            startIntent.putExtra("data", fileBundle)
            startActivityForResult(startIntent, 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1) {
                val filePath = data.getStringExtra("File") ?: return
                val from = File(filePath)
                if (FileOperator.getFileType(from) == "ogg") {
                    val musicFolder = musicFolder
                    val to = File(musicFolder.absolutePath + "/" + from.name)
                    if (FileOperator.copyFile(from, to)) {
                        showMusicConfigurationView(false)
                    }
                } else {
                    Toast.makeText(
                        this@EditModInfoActivity,
                        R.string.bad_file_type,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (requestCode == 2) {
                val filePath = data.getStringExtra("File") ?: return
                val from = File(filePath)
                if (FileOperator.getFileType(from) == "tmx") {
                    val fromPath = from.absolutePath
                    val symbol = fromPath.indexOf(".tmx")
                    val mapFolder = mapFolder
                    val iconFile = File(fromPath.substring(0, symbol) + "_map.png")
                    if (iconFile.exists()) {
                        val newIcon = File(mapFolder.absolutePath + "/" + iconFile.name)
                        FileOperator.copyFile(iconFile, newIcon)
                    }
                    val to = File(mapFolder.absolutePath + "/" + from.name)
                    if (FileOperator.copyFile(from, to)) {
                        showMapConfigurationView(false)
                    }
                } else {
                    Toast.makeText(
                        this@EditModInfoActivity,
                        R.string.bad_file_type,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (requestCode == 3) {
                val filePath = data.getStringExtra("File") ?: return
                val newIconFile = File(filePath)
                val iconFile = iconPath
                if (newIconFile.absolutePath != iconFile.absolutePath) {
                    if (iconFile.exists()) {
                        iconFile.delete()
                    }
                    val bitmap = BitmapFactory.decodeFile(newIconFile.absolutePath)
                    if (bitmap != null && bitmap.height == bitmap.width) {
                        if (FileOperator.copyFile(newIconFile, iconFile)) {
                            Glide.with(this@EditModInfoActivity).load(newIconFile)
                                .apply(GlobalMethod.getRequestOptions())
                                .into(viewBinding.iconView)
                            mNeedIcon = true
                        }
                    } else {
                        UCrop.of(
                            Uri.parse(newIconFile.toURI().toString()),
                            Uri.parse(iconFile.toURI().toString())
                        ).withAspectRatio(1f, 1f).start(this@EditModInfoActivity)
                    }
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                val resultUri = UCrop.getOutput(data)
                Glide.with(this@EditModInfoActivity).load(resultUri)
                    .apply(GlobalMethod.getRequestOptions()).into(viewBinding.iconView)
                mNeedIcon = true
            }
        }
    }

    /**
     * 启用背景音乐
     *
     * @param enable 是否启用
     */
    fun enableMusic(enable: Boolean) {
        if (enable) {
            viewBinding.musicListView.isVisible = true
            viewBinding.addMusic.isVisible = true
            viewBinding.musicPathView.isVisible = false
            viewBinding.expandMusicList.isVisible = true
            viewBinding.enabledMusic.text = getString(R.string.disabled)
        } else {
            showMusicConfigurationView(true)
            viewBinding.musicListView.isVisible = false
            viewBinding.addMusic.isVisible = false
            viewBinding.expandMusicList.isVisible = false
            viewBinding.enabledMusic.text = getText(R.string.enabled)
            viewBinding.musicPathView.text = getString(R.string.no_enabled)
            viewBinding.musicPathView.isVisible = true
        }
    }

    /**
     * 获取音乐目录
     *
     * @return 音乐目录
     */
    private val musicFolder: File
        private get() {
            var sourceFolder = mModClass.readValueFromInfoSection("sourceFolder", "music")
            if (sourceFolder == null) {
                sourceFolder = "music/"
            }
            return File(mModClass.modFile.absolutePath + "/" + sourceFolder)
        }

    /**
     * 获取图标路径
     *
     * @return 图标路径
     */
    private val iconPath: File
        private get() {
            var iconName = mModClass.readValueFromInfoSection("thumbnail", "mod")
            if (iconName == null) {
                iconName = "icon.png"
            }
            return File(mModClass.modFile.absolutePath + "/" + iconName)
        }

    /**
     * 展示音乐配置视图
     *
     * @param hide 隐藏视图
     */
    @SuppressLint("StringFormatMatches")
    fun showMusicConfigurationView(hide: Boolean) {
        if (hide) {
            viewBinding.expandMusicList.setImageResource(R.drawable.animator_expand_off)
            mExpandMusicList = false
            viewBinding.musicOperation.isVisible = false
            viewBinding.musicPathView.isVisible = false
        } else {
            viewBinding.expandMusicList.setImageResource(R.drawable.animator_expand_on)
            mExpandMusicList = true
            viewBinding.musicOperation.isVisible = true
            val musicFolder = musicFolder
            if (!musicFolder.exists()) {
                musicFolder.mkdirs()
            }
            val files = ArrayList<File>()
            val fileArray = musicFolder.listFiles()
            if (fileArray.isNotEmpty()) {
                for (f in fileArray) {
                    if (FileOperator.getFileType(f) == "ogg") {
                        files.add(f)
                    }
                }
            }
            val mapAndMusicAdapter = MapAndMusicAdapter(this, files, true)
            val layoutManager = LinearLayoutManager(this@EditModInfoActivity)
            mapAndMusicAdapter.setItemChangeEvent { changeType, i, file, i2 ->
                viewBinding.musicPathView.text =
                    String.format(getString(R.string.filenum), i2)
            }

            viewBinding.musicListView.layoutManager = layoutManager
            viewBinding.musicListView.adapter = mapAndMusicAdapter
            viewBinding.musicPathView.isVisible = true
            viewBinding.musicPathView.text = String.format(getString(R.string.filenum), files.size)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (viewBinding.expandMusicList.drawable as AnimatedVectorDrawable).start()
        } else {
            (viewBinding.expandMusicList.drawable as AnimatedVectorDrawableCompat).start()
        }
    }

    /**
     * 启用地图
     *
     * @param enable 是否启用
     */
    fun enableMap(enable: Boolean) {
        if (enable) {
            viewBinding.expandMapList.isVisible = true
            viewBinding.addMap.isVisible = true
            viewBinding.mapPathView.isVisible = false
            viewBinding.enabledMap.text = getString(R.string.disabled)
        } else {
            showMapConfigurationView(true)
            viewBinding.expandMapList.isVisible = false
            viewBinding.addMap.isVisible = false
            viewBinding.enabledMap.text = getText(R.string.enabled)
            viewBinding.mapPathView.text = getString(R.string.no_enabled)
            viewBinding.mapPathView.isVisible = true
        }
    }

    /**
     * 获取地图目录
     *
     * @return 地图目录
     */
    private val mapFolder: File
        private get() {
            var sourceFolder = mModClass.readValueFromInfoSection("sourceFolder", "map")
            if (sourceFolder == null) {
                sourceFolder = "maps/"
            }
            return File(mModClass.modFile.absolutePath + "/" + sourceFolder)
        }

    /**
     * 展示地图配置视图
     *
     * @param hide 隐藏视图
     */
    @SuppressLint("StringFormatMatches")
    fun showMapConfigurationView(hide: Boolean) {
        if (hide) {
            viewBinding.expandMapList.setImageResource(R.drawable.animator_expand_off)
            mExpandMapList = false
            viewBinding.mapOperation.isVisible = false
            viewBinding.mapPathView.isVisible = false
        } else {
            viewBinding.expandMapList.setImageResource(R.drawable.animator_expand_on)
            mExpandMapList = true
            viewBinding.mapOperation.isVisible = true
            val mapFolder = mapFolder
            if (!mapFolder.exists()) {
                mapFolder.mkdirs()
            }
            val files = ArrayList<File>()
            val fileArray = mapFolder.listFiles()
            if (fileArray.isNotEmpty()) {
                for (f in fileArray) {
                    if (FileOperator.getFileType(f) == "tmx") {
                        files.add(f)
                    }
                }
            }
            val mapAndMapAdapter = MapAndMusicAdapter(this, files, false)
            val layoutManager = LinearLayoutManager(this@EditModInfoActivity)
            mapAndMapAdapter.setItemChangeEvent { changeType, i, file, i2 ->
                viewBinding.mapPathView.text =
                    String.format(getString(R.string.filenum), i2)
            }
            viewBinding.mapListView.layoutManager = layoutManager
            viewBinding.mapListView.adapter = mapAndMapAdapter
            viewBinding.mapPathView.isVisible = true
            viewBinding.mapPathView.text = String.format(getString(R.string.filenum), files.size)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (viewBinding.expandMapList.drawable as AnimatedVectorDrawable).start()
        } else {
            (viewBinding.expandMapList.drawable as AnimatedVectorDrawableCompat).start()
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityEditModInfoBinding {
        return ActivityEditModInfoBinding.inflate(layoutInflater)
    }

}