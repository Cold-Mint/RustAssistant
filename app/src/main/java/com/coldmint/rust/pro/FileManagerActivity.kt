package com.coldmint.rust.pro

import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.tool.BookmarkManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar
import android.provider.MediaStore
import android.provider.DocumentsContract
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.adapters.FileAdapter
import com.coldmint.rust.pro.databinding.ActivityFileBinding
import com.coldmint.rust.pro.interfaces.BookmarkListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class FileManagerActivity : BaseActivity<ActivityFileBinding>() {
    private var directs = Environment.getExternalStorageDirectory()
    private var mRoot = directs
    var filePath = ""

    //哈希表映射(名称，路径)
    val bookmarkMap = HashMap<String, String>()
    val executorService = Executors.newSingleThreadExecutor()

    //type可为默认default，选择文件夹selectDirectents，选择文件selectFile
    var mStartType: String? = "default"
    private var mFileAdapter: FileAdapter? = null
    private var mProcessFiles = false
    private val bookmarkManager: BookmarkManager by lazy { BookmarkManager(this) }
    private var additionalData: String? = null
    private fun initView() {
        viewBinding.toolbar.setTitle(R.string.file_manager)
        setSupportActionBar(viewBinding.toolbar)
        setReturnButton()
        viewBinding.fileList.layoutManager = LinearLayoutManager(this@FileManagerActivity)
        val intent = intent
        val bundle = intent.getBundleExtra("data")
        if (bundle == null) {
            showError("无效的请求。")
            finish()
        } else {
            mStartType = bundle.getString("type")
            when (mStartType) {
                "default" -> {
                }
                "selectDirectents" -> {
                    setTitle(R.string.select_directents)
                    viewBinding.fab.setIconResource(R.drawable.complete)
                    viewBinding.fab.postDelayed({
                        viewBinding.fab.text = getString(R.string.select_directents)
                        viewBinding.fab.extend()
                    }, 300)
                }
                "exportFile" -> {
                    setTitle(R.string.export_file)
                    val additional = bundle.getString("additionalData")
                    if (additional == null) {
                        showError("请输入 additionalData")
                        return
                    } else {
                        viewBinding.fab.setIconResource(R.drawable.complete)
                        viewBinding.fab.postDelayed({
                            viewBinding.fab.text = getString(R.string.export_this)
                            viewBinding.fab.extend()
                        }, 300)
                    }
                }
                "selectFile" -> {
                    setTitle(R.string.select_file)
                    viewBinding.fab.setIconResource(R.drawable.complete)
                    viewBinding.fab.hide()
                }
                else -> {
                    Toast.makeText(this, "意外的请求", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            if (bundle.containsKey("path")) {
                directs = File(bundle.getString("path"))
            }
            if (bundle.containsKey("rootpath")) {
                mRoot = File(bundle.getString("rootpath"))
            }
            if (bundle.containsKey("additionalData")) {
                additionalData = bundle.getString("additionalData")
            }
        }
        loadFiles(directs)
    }

    private fun tryOpenFile(file: File?) {
        if (file == null) {
            returnDirects()
        } else {
            if (file.isDirectory) {
                loadFiles(file)
            } else {
                when (mStartType) {
                    "default" -> {
                        when (FileOperator.getFileType(file)) {
                            "ini", "txt", "template" -> {
                                val bundle = Bundle()
                                bundle.putString("path", file.absolutePath)
                                bundle.putString("modPath", FileOperator.getSuperDirectory(file))
                                val intent =
                                    Intent(this@FileManagerActivity, EditActivity::class.java)
                                intent.putExtra("data", bundle)
                                this@FileManagerActivity.startActivity(intent)
                            }
                            "json" -> {
                                val openList = listOf<String>(
                                    getString(R.string.edit_template), getString(
                                        R.string.open_action1
                                    )
                                )
                                MaterialDialog(this).title(R.string.open_type)
                                    .listItems(items = openList) { dialog, index, text ->
                                        when (text) {
                                            getString(R.string.edit_template) -> {
                                                editTemplate(file)
                                            }
                                            getString(R.string.open_action1) -> {
                                                editText(file)
                                            }
                                        }
                                    }.show()

                            }
                            "zip", "rwmod", "rar" -> {
                                Toast.makeText(
                                    this@FileManagerActivity,
                                    "点击了压缩文件。",
                                    Toast.LENGTH_SHORT
                                ).show()
                                FileOperator.openFile(this@FileManagerActivity, file)
                            }
                            else -> {
                                val ints = intArrayOf(
                                    R.string.open_action1,
                                    R.string.open_action2,
                                    R.string.open_action3,
                                    R.string.open_action4
                                )
                                val items = FileAdapter.conversionSymbol(
                                    this@FileManagerActivity,
                                    ints
                                )

                                MaterialAlertDialogBuilder(this).setItems(
                                    items
                                ) { dialog, which ->
                                    when (ints[which]) {
                                        R.string.open_action1 -> {
                                            editText(file)
                                        }
                                        R.string.open_action4 -> FileOperator.openFile(
                                            this@FileManagerActivity,
                                            file
                                        )
                                    }
                                }.show()
                            }
                        }
                        viewBinding.fab.show()
                        filePath = file.absolutePath
                    }
                    "selectFile" -> {
                        viewBinding.fab.show()
                        viewBinding.fab.postDelayed({
                            filePath = file.absolutePath
                            viewBinding.fab.text =
                                String.format(getString(R.string.select_file_ok), file.name)
                            viewBinding.fab.extend()
                        }, 300)

                    }
                }
            }
        }
    }


    /**
     * 编辑模板
     * @param file File
     */
    fun editTemplate(file: File) {
        val intent = Intent(this, TemplateMakerActivity::class.java)
        val bundle = Bundle()
        intent.putExtra("data", bundle)
        bundle.putString("path", file.absolutePath)
        bundle.putBoolean("loadTemplate", true)
        bundle.putString("templatePath", additionalData)
        startActivity(intent)
    }

    /**
     * 编辑文本
     * @param file File
     */
    fun editText(file: File) {
        val bundle = Bundle()
        bundle.putString("path", file.absolutePath)
        bundle.putString(
            "modPath",
            FileOperator.getSuperDirectory(file)
        )
        val intent = Intent(
            this@FileManagerActivity,
            EditActivity::class.java
        )
        intent.putExtra("data", bundle)
        this@FileManagerActivity.startActivity(intent)
    }

    //加载文件
    fun loadFiles(file: File) {
        executorService.submit {
            if (file.isDirectory) {
                val files = file.listFiles()
                if (files == null || !file.exists()) {
                    runOnUiThread {
                        viewBinding.fileList.isVisible = false
                        viewBinding.progressBar.isVisible = false
                        viewBinding.fileError.isVisible = true
                        viewBinding.fileError.setText(R.string.unable_to_open_this_directory)
                        viewBinding.fab.hide()
                    }
                    return@submit
                }
                directs = file
                val fileArrayList: ArrayList<File?> = ArrayList(listOf(*files))
                if (file.absolutePath != mRoot.absolutePath) {
                    fileArrayList.add(0, null)
                }
                val finalFileAdapter: FileAdapter =
                    if (mFileAdapter == null) {
                        mFileAdapter = FileAdapter(this@FileManagerActivity, fileArrayList)
                        mFileAdapter!!
                    } else {
                        mFileAdapter?.setNewDataList(fileArrayList)
                        mFileAdapter!!
                    }
                finalFileAdapter.setSortType(FileAdapter.SortType.FileName)
                finalFileAdapter.setItemEvent { i, fileItemBinding, viewHolder, itemFile ->
                    fileItemBinding.root.setOnClickListener {
                        tryOpenFile(itemFile)
                    }
                    fileItemBinding.more.setOnClickListener {
                        if (itemFile == null) {
                            return@setOnClickListener
                        }
                        val popupMenu =
                            PopupMenu(this@FileManagerActivity, fileItemBinding.more)
                        val cutBoardMenu =
                            popupMenu.menu.addSubMenu(R.string.cut_board_operation)
                        val fileMenu = popupMenu.menu.addSubMenu(R.string.file_operation)
                        val bookmarksMenu =
                            popupMenu.menu.addSubMenu(R.string.bookmarks_operation)
                        cutBoardMenu.add(R.string.copy_file_name)
                        cutBoardMenu.add(R.string.copy_file_path)
                        fileMenu.add(R.string.copy)
                        fileMenu.add(R.string.cut_off)
                        fileMenu.add(R.string.mod_action9)
                        fileMenu.add(R.string.delete_title)
                        if (bookmarkManager.contains(file)) {
                            bookmarksMenu.add(R.string.remove_bookmark)
                        } else {
                            bookmarksMenu.add(R.string.add_bookmark)
                        }
                        bookmarksMenu.add(R.string.bookmark_manager)
                        addJumpBookMenu(bookmarksMenu)
                        popupMenu.setOnMenuItemClickListener { item ->
                            val title = item.title
                            if (title == getText(R.string.copy_file_name)) {
                                val name = itemFile.name
                                GlobalMethod.copyText(
                                    this@FileManagerActivity,
                                    name,
                                    viewBinding.fab
                                )
                            } else if (title == getText(R.string.copy_file_path)) {
                                val path = itemFile.absolutePath
                                GlobalMethod.copyText(
                                    this@FileManagerActivity,
                                    path,
                                    viewBinding.fab
                                )
                            } else if (title == getText(R.string.delete_title)) {
                                executorService.submit {
                                    FileOperator.delete_files(itemFile)
                                    runOnUiThread {
                                        loadFiles(directs)
                                    }
                                }
                            } else if (title == getText(R.string.copy)) {
                                finalFileAdapter.setSelectPath(itemFile.absolutePath, true)
                            } else if (title == getText(R.string.cut_off)) {
                                finalFileAdapter.setSelectPath(itemFile.absolutePath, false)
                            } else if (title == getText(R.string.mod_action9)) {
                                val oldName = itemFile.name
                                MaterialDialog(this@FileManagerActivity).show {
                                    title(R.string.mod_action9)
                                    input(
                                        maxLength = 255,
                                        waitForPositiveButton = false, prefill = oldName
                                    ) { dialog, text ->
                                        if (text.length in 1..255) {
                                            dialog.setActionButtonEnabled(
                                                WhichButton.POSITIVE,
                                                true
                                            )
                                        }
                                    }.positiveButton(R.string.dialog_ok, null) { dialog ->
                                        val newName = dialog.getInputField().text.toString()
                                        if (!newName.isEmpty() || newName != oldName) {
                                            val reNameFile = File("$directs/$newName")
                                            itemFile.renameTo(reNameFile)
                                            loadFiles(directs)
                                        }
                                    }.negativeButton(R.string.dialog_close)
                                }
                            } else if (title == getString(R.string.remove_bookmark)) {
                                val removeBookmark =
                                    bookmarkManager.removeBookmark(itemFile.absolutePath)
                                if (removeBookmark) {
                                    Snackbar.make(
                                        viewBinding.fab,
                                        R.string.remove_bookmark_success,
                                        Snackbar.LENGTH_SHORT
                                    ).setAction(R.string.symbol10) {
                                        bookmarkManager.addBookmark(
                                            itemFile.absolutePath,
                                            FileOperator.getPrefixName(file)
                                        )
                                    }
                                        .show()
                                } else {
                                    Snackbar.make(
                                        viewBinding.fab,
                                        R.string.remove_bookmark_fail,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            } else if (title == getString(R.string.add_bookmark)) {
                                val addBookmark = bookmarkManager.addBookmark(
                                    itemFile.absolutePath,
                                    FileOperator.getPrefixName(file)
                                )
                                if (addBookmark) {
                                    Snackbar.make(
                                        viewBinding.fab,
                                        R.string.add_bookmark_success,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Snackbar.make(
                                        viewBinding.fab,
                                        R.string.add_bookmark_fail,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            } else if (title == getString(R.string.bookmark_manager)) {
                                bookmarkManager.save()
                                startActivity(
                                    Intent(
                                        this@FileManagerActivity,
                                        BookmarkManagerActivity::class.java
                                    )
                                )
                            } else {
                                loadBook(title)
                            }
                            false
                        }
                        popupMenu.show()
                    }
                }
                runOnUiThread {
                    viewBinding.fileList.adapter = finalFileAdapter
                    viewBinding.fileList.isVisible = true
                    viewBinding.fileError.isVisible = false
                    viewBinding.progressBar.isVisible = false
                }
            }
        }
    }


    /**
     * 添加书签菜单
     */
    fun addJumpBookMenu(menu: Menu) {
        val bookmarkContent: SubMenu? = if (bookmarkManager.size > 0) {
            menu.addSubMenu(R.string.jump_a_bookmark)
        } else {
            null
        }
        bookmarkMap.clear()
        bookmarkManager.fromList(object : BookmarkListener {
            override fun find(path: String, name: String) {
                bookmarkMap[name] = path
                bookmarkContent!!.add(name)
            }

        })
    }

    /**
     * 点击书签项目
     */
    fun loadBook(menuTitle: CharSequence) {
        if (bookmarkMap.containsKey(menuTitle)) {
            val path = bookmarkMap[menuTitle]
            if (path != null) {
                val rootPath = mRoot.absolutePath
                if (path.startsWith(rootPath)) {
                    val newFile = File(path)
                    if (newFile.exists()) {
                        if (newFile.isDirectory) {
                            loadFiles(newFile)
                        } else {
                            tryOpenFile(newFile)
                        }
                    } else {
                        Snackbar.make(
                            viewBinding.fab,
                            R.string.bookmark_jump_failed,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Snackbar.make(
                        viewBinding.fab,
                        R.string.cannot_be_accessed_this_directory,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }


    override fun onPause() {
        bookmarkManager.save()
        super.onPause()
    }

    override fun onResume() {
        bookmarkManager.load()
        super.onResume()
    }



    /**
     * 解析文件路径
     *
     * @param context 上下文环境
     * @param intent  意图
     * @return 成功返回文件路径，失败返回null
     */
    private fun parseFilePath(context: Context, intent: Intent?): String? {
        return try {
            if (intent != null) {
                val uri = intent.data
                var chooseFilePath: String? = null
                if ("file".equals(uri!!.scheme, ignoreCase = true)) { //使用第三方应用打开
                    chooseFilePath = uri.path
                    Toast.makeText(context, chooseFilePath, Toast.LENGTH_SHORT).show()
                    return chooseFilePath
                }
                chooseFilePath = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) { //4.4以后
                    getPath(context, uri)
                } else { //4.4以下下系统调用方法
                    getRealPathFromURI(context, uri)
                }
                return chooseFilePath
            }
            null
        } catch (e: Exception) {
            Snackbar.make(viewBinding.fab, R.string.parse_file_exception, Snackbar.LENGTH_SHORT)
                .show()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (mStartType == "selectFile" && requestCode == 1) {
                val path = FileOperator.parsePicturePath(this@FileManagerActivity, data)
                if (path != null) {
                    val intent = Intent()
                    intent.putExtra("File", path)
                    setResult(RESULT_OK, intent)
                    bookmarkManager.save()
                    finish()
                }
            } else if (mStartType == "selectFile" && requestCode == 2) {
                val path = parseFilePath(this@FileManagerActivity, data)
                if (path != null) {
                    val intent = Intent()
                    intent.putExtra("File", path)
                    setResult(RESULT_OK, intent)
                    bookmarkManager.save()
                    finish()
                }
            } else if (requestCode == 3) {
                //新建源文件
                loadFiles(directs)
            } else if (requestCode == 4) {
                val file = File(data!!.getStringExtra("File"))
                val copyResult =
                    FileOperator.copyFile(file, File(directs.toString() + "/" + file.name))
                if (!copyResult) {
                    Snackbar.make(
                        viewBinding.fab,
                        getText(R.string.copy_file_error),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    loadFiles(directs)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                bookmarkManager.save()
                finish()
                return true
            }
            R.id.reloadFile -> {
                loadFiles(directs)
                return true
            }
            R.id.photo_album -> {
                this@FileManagerActivity.startActivityForResult(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ), 1
                )
                return true
            }
            R.id.system_file_manager -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                this@FileManagerActivity.startActivityForResult(intent, 2)
                return true
            }
            R.id.creteFolder -> {
                createFolderAction()
                return true
            }
        }
        loadBook(item.title)
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (directs.absolutePath == mRoot.absolutePath) {
                bookmarkManager.save()
                finish()
                true
            } else {
                returnDirects()
                false
            }
        } else super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_files, menu)
        if (mStartType != "selectFile") {
            menu.removeItem(R.id.selectFile)
        }
        addJumpBookMenu(menu)
        return true
    }

    //加载上级文件夹
    fun returnDirects() {
        val file = File(FileOperator.getSuperDirectory(directs, mRoot))
        directs = file
        loadFiles(file)
    }

    fun initAction() {
        viewBinding.fab.setOnClickListener {
            val intent = Intent()
            when (mStartType) {
                "exportFile" -> {
                    val oldFile = File(additionalData)
                    val result = FileOperator.copyFile(
                        oldFile,
                        File(directs.absolutePath + "/" + oldFile.name)
                    )
                    if (result) {
                        setResult(RESULT_OK)
                    }
                    finish()
                }
                "selectDirectents" -> {
                    intent.putExtra("Directents", directs.absolutePath)
                    setResult(RESULT_OK, intent)
                    bookmarkManager.save()
                    finish()
                }
                "selectFile" -> {
                    intent.putExtra("File", filePath)
                    setResult(RESULT_OK, intent)
                    bookmarkManager.save()
                    finish()
                }
                "default" -> {
                    val popupMenu = PopupMenu(this@FileManagerActivity, viewBinding.fab)
                    if (mFileAdapter != null) {
                        val selectPath = mFileAdapter!!.selectPath
                        if (selectPath != null && mProcessFiles == false) {
                            if (mFileAdapter!!.isCopyFile) {
                                popupMenu.menu.add(R.string.copy_to_this)
                            } else {
                                popupMenu.menu.add(R.string.cut_to_this)
                            }
                        }
                    }
                    popupMenu.menu.add(R.string.create_unit)
                    popupMenu.menu.add(R.string.create_folder)
                    popupMenu.menu.add(R.string.select_file)
                    popupMenu.setOnMenuItemClickListener { item ->
                        val title = item.title
                        val handler = Handler(Looper.getMainLooper())
                        when (title) {
                            getText(R.string.create_unit) -> {
                                val intent =
                                    Intent(this@FileManagerActivity, CreateUnitActivity::class.java)
                                val bundle = Bundle()
                                bundle.putString("modPath", directs.absolutePath)
                                bundle.putString("createPath", directs.absolutePath)
                                intent.putExtra("data", bundle)
                                startActivityForResult(intent, 3)
                            }
                            getText(R.string.select_file) -> {
                                val bundle = Bundle()
                                val intent =
                                    Intent(
                                        this@FileManagerActivity,
                                        FileManagerActivity::class.java
                                    )
                                bundle.putString("type", "selectFile")
                                //bundle.putString("path", modClass.getModFile().getAbsolutePath());
                                intent.putExtra("data", bundle)
                                startActivityForResult(intent, 4)
                            }
                            getText(R.string.create_folder) -> {
                                createFolderAction()
                            }
                            getText(R.string.copy_to_this) -> {
                                Thread {
                                    mProcessFiles = true
                                    val oldFile = File(mFileAdapter!!.selectPath)
                                    val newFile = File(directs.absolutePath + "/" + oldFile.name)
                                    if (FileOperator.copyFiles(oldFile, newFile)) {
                                        handler.post {
                                            loadFiles(directs)
                                            mFileAdapter!!.cleanSelectPath()
                                            mProcessFiles = false
                                        }
                                    } else {
                                        handler.post {
                                            Toast.makeText(
                                                this@FileManagerActivity,
                                                getText(R.string.copy_failed),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            mProcessFiles = false
                                        }
                                    }
                                }.start()
                            }
                            getText(R.string.cut_to_this) -> {
                                Thread {
                                    mProcessFiles = true
                                    val oldFile = File(mFileAdapter!!.selectPath)
                                    val newFile = File(directs.absolutePath + "/" + oldFile.name)
                                    if (FileOperator.removeFiles(oldFile, newFile)) {
                                        handler.post {
                                            loadFiles(directs)
                                            mFileAdapter!!.cleanSelectPath()
                                            mProcessFiles = false
                                        }
                                    } else {
                                        handler.post {
                                            Toast.makeText(
                                                this@FileManagerActivity,
                                                getText(R.string.cut_failed),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            mProcessFiles = false
                                        }
                                    }
                                }.start()
                            }
                        }
                        false
                    }
                    popupMenu.show()
                }
            }
        }
    }

    /**
     * 创建文件夹活动
     */
    private fun createFolderAction() {
        MaterialDialog(this).show {
            title(R.string.create_folder)
            input(
                maxLength = 255,
                waitForPositiveButton = false
            ) { dialog, text ->
                if (text.length in 1..255) {
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                }
            }.positiveButton(R.string.dialog_ok, null) { dialog ->
                val string = dialog.getInputField().text.toString()
                val file = File("$directs/$string")
                if (file.exists()) {
                    Toast.makeText(
                        this@FileManagerActivity,
                        R.string.folder_error,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    file.mkdirs()
                    loadFiles(directs)
                }
            }.negativeButton(R.string.dialog_close)
        }
    }


    /**
     * 获取uri的绝对路径
     *
     * @param context    上下文环境
     * @param contentUri uri
     * @return 文件路径
     */
    private fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
        if (null != cursor && cursor.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
            cursor.close()
        }
        return res
    }

    private fun getPath(context: Context, uri: Uri?): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            uri.path
        }
        return null
    }

    private fun isMediaDocument(uri: Uri?): Boolean {
        return "com.android.providers.media.documents" == uri!!.authority
    }

    private fun isExternalStorageDocument(uri: Uri?): Boolean {
        return "com.android.externalstorage.documents" == uri!!.authority
    }

    private fun isDownloadsDocument(uri: Uri?): Boolean {
        return "com.android.providers.downloads.documents" == uri!!.authority
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    override fun getViewBindingObject(): ActivityFileBinding {
        return ActivityFileBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            initView()
            initAction()
        }
    }
}