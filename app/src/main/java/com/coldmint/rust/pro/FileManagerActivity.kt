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
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.coldmint.dialog.CoreDialog
import com.coldmint.dialog.InputDialog
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.adapters.FileAdapter
import com.coldmint.rust.pro.adapters.FileTabAdapter
import com.coldmint.rust.pro.databean.FileTab
import com.coldmint.rust.pro.databinding.ActivityFileBinding
import com.coldmint.rust.pro.interfaces.BookmarkListener
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.viewmodel.FileManagerViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.SnackbarContentLayout
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class FileManagerActivity : BaseActivity<ActivityFileBinding>() {
    //    private var directs = Environment.getExternalStorageDirectory()
//    private var mRoot = directs
//    var filePath = ""
//

    //    val executorService = Executors.newSingleThreadExecutor()
//
//    //type可为默认default，选择文件夹selectDirectents，选择文件selectFile
//    var mStartType: String? = "default"
//    private var mFileAdapter: FileAdapter? = null
//    private var mProcessFiles = false
//    private var additionalData: String? = null
//    private fun initView() {
//        title = getString(R.string.file_manager)
//        setReturnButton()
//        viewBinding.fileList.layoutManager = LinearLayoutManager(this@FileManagerActivity)
//        val intent = intent
//        val bundle = intent.getBundleExtra("data")
//        if (bundle == null) {
//            showError("无效的请求。")
//            finish()
//        } else {
//            mStartType = bundle.getString("type")
//            when (mStartType) {
//                "default" -> {
//                }
//                "selectDirectents" -> {
//                    setTitle(R.string.select_directents)
//                    viewBinding.fab.setIconResource(R.drawable.complete)
//                    viewBinding.fab.postDelayed({
//                        viewBinding.fab.text = getString(R.string.select_directents)
//                        viewBinding.fab.extend()
//                    }, 300)
//                }
//                "exportFile" -> {
//                    setTitle(R.string.export_file)
//                    val additional = bundle.getString("additionalData")
//                    if (additional == null) {
//                        showError("请输入 additionalData")
//                        return
//                    } else {
//                        viewBinding.fab.setIconResource(R.drawable.complete)
//                        viewBinding.fab.postDelayed({
//                            viewBinding.fab.text = getString(R.string.export_this)
//                            viewBinding.fab.extend()
//                        }, 300)
//                    }
//                }
//                "selectFile" -> {
//                    setTitle(R.string.select_file)
//                    viewBinding.fab.setIconResource(R.drawable.complete)
//                    viewBinding.fab.hide()
//                }
//                else -> {
//                    Toast.makeText(this, "意外的请求", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//            if (bundle.containsKey("path")) {
//                directs = File(bundle.getString("path"))
//            }
//            if (bundle.containsKey("rootpath")) {
//                mRoot = File(bundle.getString("rootpath"))
//            }
//            if (bundle.containsKey("additionalData")) {
//                additionalData = bundle.getString("additionalData")
//            }
//        }
//        loadFiles(directs)
//    }
//
//    private fun tryOpenFile(file: File?) {
//        if (file == null) {
//            returnDirects()
//        } else {
//            if (file.isDirectory) {
//                loadFiles(file)
//            } else {
//                when (mStartType) {
//                    "default" -> {
//                        when (FileOperator.getFileType(file)) {
//                            "ini", "txt", "template" -> {
//                                val bundle = Bundle()
//                                bundle.putString("path", file.absolutePath)
//                                bundle.putString("modPath", FileOperator.getSuperDirectory(file))
//                                val intent =
//                                    Intent(this@FileManagerActivity, EditActivity::class.java)
//                                intent.putExtra("data", bundle)
//                                this@FileManagerActivity.startActivity(intent)
//                            }
//                            "json" -> {
//                                val openList = listOf<String>(
//                                    getString(R.string.edit_template), getString(
//                                        R.string.open_action1
//                                    )
//                                )
//                                MaterialDialog(this).title(R.string.open_type)
//                                    .listItems(items = openList) { dialog, index, text ->
//                                        when (text) {
//                                            getString(R.string.edit_template) -> {
//                                                editTemplate(file)
//                                            }
//                                            getString(R.string.open_action1) -> {
//                                                editText(file)
//                                            }
//                                        }
//                                    }.show()
//
//                            }
//                            "zip", "rwmod", "rar" -> {
//                                Toast.makeText(
//                                    this@FileManagerActivity,
//                                    "点击了压缩文件。",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                FileOperator.openFile(this@FileManagerActivity, file)
//                            }
//                            else -> {
//                                val ints = intArrayOf(
//                                    R.string.open_action1,
//                                    R.string.open_action4
//                                )
//                                val items = FileAdapter.conversionSymbol(
//                                    this@FileManagerActivity,
//                                    ints
//                                )
//
//                                MaterialAlertDialogBuilder(this).setItems(
//                                    items
//                                ) { dialog, which ->
//                                    when (ints[which]) {
//                                        R.string.open_action1 -> {
//                                            editText(file)
//                                        }
//                                        R.string.open_action4 -> FileOperator.openFile(
//                                            this@FileManagerActivity,
//                                            file
//                                        )
//                                    }
//                                }.show()
//                            }
//                        }
//                        viewBinding.fab.show()
//                        filePath = file.absolutePath
//                    }
//                    "selectFile" -> {
//                        viewBinding.fab.show()
//                        viewBinding.fab.postDelayed({
//                            filePath = file.absolutePath
//                            viewBinding.fab.text =
//                                String.format(getString(R.string.select_file_ok), file.name)
//                            viewBinding.fab.extend()
//                        }, 300)
//
//                    }
//                }
//            }
//        }
//    }
//
//
//    /**
//     * 编辑模板
//     * @param file File
//     */
//    fun editTemplate(file: File) {
//        val intent = Intent(this, TemplateMakerActivity::class.java)
//        val bundle = Bundle()
//        intent.putExtra("data", bundle)
//        bundle.putString("path", file.absolutePath)
//        bundle.putBoolean("loadTemplate", true)
//        bundle.putString("templatePath", additionalData)
//        startActivity(intent)
//    }
//
//    /**
//     * 编辑文本
//     * @param file File
//     */
//    fun editText(file: File) {
//        val bundle = Bundle()
//        bundle.putString("path", file.absolutePath)
//        bundle.putString(
//            "modPath",
//            FileOperator.getSuperDirectory(file)
//        )
//        val intent = Intent(
//            this@FileManagerActivity,
//            EditActivity::class.java
//        )
//        intent.putExtra("data", bundle)
//        this@FileManagerActivity.startActivity(intent)
//    }
//
//    //加载文件
//    fun loadFiles(file: File) {
//        executorService.submit {
//            if (!file.exists()) {
//                runOnUiThread {
//                    viewBinding.fileList.isVisible = false
//                    viewBinding.progressBar.isVisible = false
//                    viewBinding.fileError.isVisible = true
//                    viewBinding.fileError.setText(R.string.unable_to_open_this_directory)
//                    viewBinding.fab.hide()
//                }
//                return@submit
//            }
//
//            if (file.isDirectory) {
//                val files = file.listFiles()
//                directs = file
//                val fileArrayList: ArrayList<File?> = ArrayList(listOf(*files))
//                if (file.absolutePath != mRoot.absolutePath) {
//                    fileArrayList.add(0, null)
//                }
//                val finalFileAdapter: FileAdapter =
//                    if (mFileAdapter == null) {
//                        mFileAdapter = FileAdapter(this@FileManagerActivity, fileArrayList)
//                        mFileAdapter!!
//                    } else {
//                        mFileAdapter?.setNewDataList(fileArrayList)
//                        mFileAdapter!!
//                    }
//                finalFileAdapter.setSortType(FileAdapter.SortType.FileName)
//                finalFileAdapter.setItemEvent { i, fileItemBinding, viewHolder, itemFile ->
//                    fileItemBinding.root.setOnClickListener {
//                        tryOpenFile(itemFile)
//                    }
//                    fileItemBinding.more.setOnClickListener {
//                        if (itemFile == null) {
//                            return@setOnClickListener
//                        }
//                        val popupMenu =
//                            PopupMenu(this@FileManagerActivity, fileItemBinding.more)
//                        val cutBoardMenu =
//                            popupMenu.menu.addSubMenu(R.string.cut_board_operation)
//                        val fileMenu = popupMenu.menu.addSubMenu(R.string.file_operation)
//                        val bookmarksMenu =
//                            popupMenu.menu.addSubMenu(R.string.bookmarks_operation)
//                        cutBoardMenu.add(R.string.copy_file_name)
//                        cutBoardMenu.add(R.string.copy_file_path)
//                        fileMenu.add(R.string.copy)
//                        fileMenu.add(R.string.cut_off)
//                        fileMenu.add(R.string.mod_action9)
//                        fileMenu.add(R.string.delete_title)
//                        if (bookmarkManager.contains(file)) {
//                            bookmarksMenu.add(R.string.remove_bookmark)
//                        } else {
//                            bookmarksMenu.add(R.string.add_bookmark)
//                        }
//                        bookmarksMenu.add(R.string.bookmark_manager)
//                        addJumpBookMenu(bookmarksMenu)
//                        popupMenu.setOnMenuItemClickListener { item ->
//                            val title = item.title
//                            if (title == getText(R.string.copy_file_name)) {
//                                val name = itemFile.name
//                                GlobalMethod.copyText(
//                                    this@FileManagerActivity,
//                                    name,
//                                    viewBinding.fab
//                                )
//                            } else if (title == getText(R.string.copy_file_path)) {
//                                val path = itemFile.absolutePath
//                                GlobalMethod.copyText(
//                                    this@FileManagerActivity,
//                                    path,
//                                    viewBinding.fab
//                                )
//                            } else if (title == getText(R.string.delete_title)) {
//                                executorService.submit {
//                                    FileOperator.delete_files(itemFile)
//                                    runOnUiThread {
//                                        loadFiles(directs)
//                                    }
//                                }
//                            } else if (title == getText(R.string.copy)) {
//                                finalFileAdapter.setSelectPath(itemFile.absolutePath, true)
//                            } else if (title == getText(R.string.cut_off)) {
//                                finalFileAdapter.setSelectPath(itemFile.absolutePath, false)
//                            } else if (title == getText(R.string.mod_action9)) {
//                                val oldName = itemFile.name
//                                MaterialDialog(this@FileManagerActivity).show {
//                                    title(R.string.mod_action9)
//                                    input(
//                                        maxLength = 255,
//                                        waitForPositiveButton = false, prefill = oldName
//                                    ) { dialog, text ->
//                                        if (text.length in 1..255) {
//                                            dialog.setActionButtonEnabled(
//                                                WhichButton.POSITIVE,
//                                                true
//                                            )
//                                        }
//                                    }.positiveButton(R.string.dialog_ok, null) { dialog ->
//                                        val newName = dialog.getInputField().text.toString()
//                                        if (!newName.isEmpty() || newName != oldName) {
//                                            val reNameFile = File("$directs/$newName")
//                                            itemFile.renameTo(reNameFile)
//                                            loadFiles(directs)
//                                        }
//                                    }.negativeButton(R.string.dialog_close)
//                                }
//                            } else if (title == getString(R.string.remove_bookmark)) {
//                                val removeBookmark =
//                                    bookmarkManager.removeBookmark(itemFile.absolutePath)
//                                if (removeBookmark) {
//                                    Snackbar.make(
//                                        viewBinding.fab,
//                                        R.string.remove_bookmark_success,
//                                        Snackbar.LENGTH_SHORT
//                                    ).setAction(R.string.symbol10) {
//                                        bookmarkManager.addBookmark(
//                                            itemFile.absolutePath,
//                                            FileOperator.getPrefixName(file)
//                                        )
//                                    }
//                                        .show()
//                                } else {
//                                    Snackbar.make(
//                                        viewBinding.fab,
//                                        R.string.remove_bookmark_fail,
//                                        Snackbar.LENGTH_SHORT
//                                    ).show()
//                                }
//                            } else if (title == getString(R.string.add_bookmark)) {
//                                val addBookmark = bookmarkManager.addBookmark(
//                                    itemFile.absolutePath,
//                                    FileOperator.getPrefixName(file)
//                                )
//                                if (addBookmark) {
//                                    Snackbar.make(
//                                        viewBinding.fab,
//                                        R.string.add_bookmark_success,
//                                        Snackbar.LENGTH_SHORT
//                                    ).show()
//                                } else {
//                                    Snackbar.make(
//                                        viewBinding.fab,
//                                        R.string.add_bookmark_fail,
//                                        Snackbar.LENGTH_SHORT
//                                    ).show()
//                                }
//                            } else if (title == getString(R.string.bookmark_manager)) {
//                                bookmarkManager.save()
//                                startActivity(
//                                    Intent(
//                                        this@FileManagerActivity,
//                                        BookmarkManagerActivity::class.java
//                                    )
//                                )
//                            } else {
//                                loadBook(title)
//                            }
//                            false
//                        }
//                        popupMenu.show()
//                    }
//                }
//                runOnUiThread {
//                    viewBinding.fileList.adapter = finalFileAdapter
//                    viewBinding.fileList.isVisible = true
//                    viewBinding.fileError.isVisible = false
//                    viewBinding.progressBar.isVisible = false
//                }
//            }
//        }
//    }
//
//

    //    /**
//     * 点击书签项目
//     */
//    fun loadBook(menuTitle: CharSequence) {
//        if (bookmarkMap.containsKey(menuTitle)) {
//            val path = bookmarkMap[menuTitle]
//            if (path != null) {
//                val rootPath = viewModel.getRootPath()
//                if (path.startsWith(rootPath)) {
//                    val newFile = File(path)
//                    if (newFile.exists()) {
//                        if (newFile.isDirectory) {
//                            viewModel.loadFiles(newFile.absolutePath)
//                        } else {
////                            tryOpenFile(newFile)
//                        }
//                    } else {
//                        Snackbar.make(
//                            viewBinding.fab,
//                            R.string.bookmark_jump_failed,
//                            Snackbar.LENGTH_SHORT
//                        ).show()
//                    }
//                } else {
//                    Snackbar.make(
//                        viewBinding.fab,
//                        R.string.cannot_be_accessed_this_directory,
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                }
//            }
//
//        }
//    }
//
//
//    override fun onPause() {
//        viewModel.getBookmarkManager().save()
//        super.onPause()
//    }

    override fun onResume() {
        viewModel.getBookmarkManager().load()
        loadMineBookmarksMenu()
        super.onResume()
    }
//
//

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (viewModel.startTypeData == FileManagerViewModel.StartType.SELECT_FILE && requestCode == 1) {
                val path = FileOperator.parsePicturePath(this@FileManagerActivity, data)
                if (path != null) {
                    val intent = Intent()
                    intent.putExtra("File", path)
                    setResult(RESULT_OK, intent)
//                    bookmarkManager.save()
                    finish()
                }
            } else if (viewModel.startTypeData == FileManagerViewModel.StartType.SELECT_FILE && requestCode == 2) {
                val path = viewModel.parseFilePath(this@FileManagerActivity, data)
                if (path != null) {
                    val intent = Intent()
                    intent.putExtra("File", path)
                    setResult(RESULT_OK, intent)
//                    bookmarkManager.save()
                    finish()
                }
            } else {
                Toast.makeText(this, "未设置的操作", Toast.LENGTH_SHORT).show()
            }
            //            else if (requestCode == 3) {
            //新建源文件
//                loadFiles(directs)
//            } else if (requestCode == 4) {
//                val file = File(data!!.getStringExtra("File"))
//                val copyResult =
//                    FileOperator.copyFile(file, File(directs.toString() + "/" + file.name))
//                if (!copyResult) {
//                    Snackbar.make(
//                        viewBinding.fab,
//                        getText(R.string.copy_file_error),
//                        Snackbar.LENGTH_SHORT
//                    ).show()
//                } else {
//                    loadFiles(directs)
//                }
//            }
        }
    }

    //
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
//                bookmarkManager.save()
                finish()
                return true
            }
            R.id.reloadFile -> {
                viewModel.loadFiles(viewModel.getCurrentPath())
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
//                createFolderAction()
                return true
            }
        }
//        loadBook(item.title)
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (viewModel.getCurrentPath() == viewModel.getRootPath()) {
//                bookmarkManager.save()
                finish()
                true
            } else {
                viewModel.returnDirects()
                false
            }
        } else super.onKeyDown(keyCode, event)
    }

    //
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.menu_files, menu)
//        if (mStartType != "selectFile") {
//            menu.removeItem(R.id.selectFile)
//        }
//        addJumpBookMenu(menu)
//        return true
//    }
//
//    //加载上级文件夹
//    fun returnDirects() {
//        val file = File(FileOperator.getSuperDirectory(directs, mRoot))
//        directs = file
//        loadFiles(file)
//    }
//
    fun initAction() {
        viewBinding.fab.setOnClickListener {
            val intent = Intent()
            val startType = viewModel.startTypeData
            when (startType) {
//                "exportFile" -> {
//                    val oldFile = File(additionalData)
//                    val result = FileOperator.copyFile(
//                        oldFile,
//                        File(directs.absolutePath + "/" + oldFile.name)
//                    )
//                    if (result) {
//                        setResult(RESULT_OK)
//                    }
//                    finish()
//                }
//                "selectDirectents" -> {
//                    intent.putExtra("Directents", directs.absolutePath)
//                    setResult(RESULT_OK, intent)
//                    bookmarkManager.save()
//                    finish()
//                }
//                "selectFile" -> {
//                    intent.putExtra("File", filePath)
//                    setResult(RESULT_OK, intent)
//                    bookmarkManager.save()
//                    finish()
//                }
                FileManagerViewModel.StartType.SELECT_FILE -> {

                }
                FileManagerViewModel.StartType.DEFAULT -> {
                    val popupMenu = PopupMenu(this@FileManagerActivity, viewBinding.fab)
//                    if (mFileAdapter != null) {
//                        val selectPath = mFileAdapter!!.selectPath
//                        if (selectPath != null && mProcessFiles == false) {
//                            if (mFileAdapter!!.isCopyFile) {
//                                popupMenu.menu.add(R.string.copy_to_this)
//                            } else {
//                                popupMenu.menu.add(R.string.cut_to_this)
//                            }
//                        }
//                    }
                    popupMenu.menu.add(R.string.create_unit)
                    popupMenu.menu.add(R.string.create_folder)
                    popupMenu.menu.add(R.string.select_file)
                    popupMenu.setOnMenuItemClickListener { item ->
                        val title = item.title
//                        val handler = Handler(Looper.getMainLooper())
                        when (title) {
                            getText(R.string.create_unit) -> {
                                val intent =
                                    Intent(this@FileManagerActivity, CreateUnitActivity::class.java)
                                val bundle = Bundle()
                                bundle.putString("modPath", viewModel.getCurrentPath())
                                bundle.putString("createPath", viewModel.getCurrentPath())
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
//                            getText(R.string.copy_to_this) -> {
//                                Thread {
//                                    mProcessFiles = true
//                                    val oldFile = File(mFileAdapter!!.selectPath)
//                                    val newFile = File(directs.absolutePath + "/" + oldFile.name)
//                                    if (FileOperator.copyFiles(oldFile, newFile)) {
//                                        handler.post {
//                                            loadFiles(directs)
//                                            mFileAdapter!!.cleanSelectPath()
//                                            mProcessFiles = false
//                                        }
//                                    } else {
//                                        handler.post {
//                                            Toast.makeText(
//                                                this@FileManagerActivity,
//                                                getText(R.string.copy_failed),
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                            mProcessFiles = false
//                                        }
//                                    }
//                                }.start()
//                            }
//                            getText(R.string.cut_to_this) -> {
//                                Thread {
//                                    mProcessFiles = true
//                                    val oldFile = File(mFileAdapter!!.selectPath)
//                                    val newFile = File(directs.absolutePath + "/" + oldFile.name)
//                                    if (FileOperator.removeFiles(oldFile, newFile)) {
//                                        handler.post {
//                                            loadFiles(directs)
//                                            mFileAdapter!!.cleanSelectPath()
//                                            mProcessFiles = false
//                                        }
//                                    } else {
//                                        handler.post {
//                                            Toast.makeText(
//                                                this@FileManagerActivity,
//                                                getText(R.string.cut_failed),
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                            mProcessFiles = false
//                                        }
//                                    }
//                                }.start()
//                            }
                        }
                        false
                    }
                    popupMenu.show()
                }
                else -> {

                }
            }
        }
    }
//
//    /**
//     * 创建文件夹活动
//     */
//    private fun createFolderAction() {
//        MaterialDialog(this).show {
//            title(R.string.create_folder)
//            input(
//                maxLength = 255,
//                waitForPositiveButton = false
//            ) { dialog, text ->
//                if (text.length in 1..255) {
//                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
//                }
//            }.positiveButton(R.string.dialog_ok, null) { dialog ->
//                val string = dialog.getInputField().text.toString()
//                val file = File("$directs/$string")
//                if (file.exists()) {
//                    Toast.makeText(
//                        this@FileManagerActivity,
//                        R.string.folder_error,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    file.mkdirs()
//                    loadFiles(directs)
//                }
//            }.negativeButton(R.string.dialog_close)
//        }
//    }
//
//

    //
//    private fun getPath(context: Context, uri: Uri?): String? {
//        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
//
//        // DocumentProvider
//        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                val docId = DocumentsContract.getDocumentId(uri)
//                val split = docId.split(":").toTypedArray()
//                val type = split[0]
//                if ("primary".equals(type, ignoreCase = true)) {
//                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
//                }
//            } else if (isDownloadsDocument(uri)) {
//                val id = DocumentsContract.getDocumentId(uri)
//                val contentUri = ContentUris.withAppendedId(
//                    Uri.parse("content://downloads/public_downloads"),
//                    java.lang.Long.valueOf(id)
//                )
//                return getDataColumn(context, contentUri, null, null)
//            } else if (isMediaDocument(uri)) {
//                val docId = DocumentsContract.getDocumentId(uri)
//                val split = docId.split(":").toTypedArray()
//                val type = split[0]
//                var contentUri: Uri? = null
//                if ("image" == type) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                } else if ("video" == type) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                } else if ("audio" == type) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                }
//                val selection = "_id=?"
//                val selectionArgs = arrayOf(split[1])
//                return getDataColumn(context, contentUri, selection, selectionArgs)
//            }
//        } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
//            return getDataColumn(context, uri, null, null)
//        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
//            uri.path
//        }
//        return null
//    }
//
//    private fun isMediaDocument(uri: Uri?): Boolean {
//        return "com.android.providers.media.documents" == uri!!.authority
//    }
//
//    private fun isExternalStorageDocument(uri: Uri?): Boolean {
//        return "com.android.externalstorage.documents" == uri!!.authority
//    }
//
//    private fun isDownloadsDocument(uri: Uri?): Boolean {
//        return "com.android.providers.downloads.documents" == uri!!.authority
//    }
//
//    private fun getDataColumn(
//        context: Context,
//        uri: Uri?,
//        selection: String?,
//        selectionArgs: Array<String>?
//    ): String? {
//        var cursor: Cursor? = null
//        val column = "_data"
//        val projection = arrayOf(column)
//        try {
//            cursor = context.contentResolver.query(
//                uri!!, projection, selection, selectionArgs,
//                null
//            )
//            if (cursor != null && cursor.moveToFirst()) {
//                val column_index = cursor.getColumnIndexOrThrow(column)
//                return cursor.getString(column_index)
//            }
//        } finally {
//            cursor?.close()
//        }
//        return null
//    }
//
//    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityFileBinding {
//        return ActivityFileBinding.inflate(layoutInflater)
//    }
//
//    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
//        if (canUseView) {
//            initView()
//            initAction()
//        }
//    }
    private lateinit var menuBinding: MenuBinding
    private val viewModel: FileManagerViewModel by lazy {
        ViewModelProvider(this).get(FileManagerViewModel::class.java)
    }


    /**
     * 创建文件夹活动
     */
    fun createFolderAction() {
        InputDialog(this).setTitle(R.string.create_folder).setHint(R.string.file_name)
            .setCancelable(false).setInputCanBeEmpty(false).setMaxNumber(255)
            .setErrorTip { s, textInputLayout ->
                val newFolder = File(viewModel.getCurrentPath() + "/" + s)
                if (newFolder.exists()) {
                    textInputLayout.error = getString(R.string.folder_error)
                } else {
                    textInputLayout.isErrorEnabled = false
                }
            }.setPositiveButton(R.string.dialog_ok) { i ->
                val newFolder = File(viewModel.getCurrentPath() + "/" + i)
                val res = newFolder.mkdirs()
                adapter?.addItem(newFolder)
                res
            }.setNegativeButton(R.string.dialog_cancel) {

            }.show()
    }


    private var adapter: FileAdapter? = null

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this@FileManagerActivity)
            val linearLayoutManager = LinearLayoutManager(this)
            linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            viewBinding.fileTabNav.layoutManager = linearLayoutManager
            loadTitle()
            loadObserve()
            initAction()
            viewModel.initBookmarkManager(this)
            viewModel.loadFiles()
            viewModel.loadSortType(this)
            viewBinding.swipeRefreshLayout.setOnRefreshListener {
                viewModel.loadFiles(viewModel.getCurrentPath())
                viewBinding.swipeRefreshLayout.isRefreshing = false
            }
            FastScrollerBuilder(viewBinding.recyclerView).useMd2Style()
                .setPopupTextProvider(adapter).build()
        } else {
            val bundle = intent.getBundleExtra("data")
            if (bundle == null) {
                showError("无效的请求。")
                finish()
                return
            }
            if (bundle.containsKey("path")) {
                viewModel.currentPathLiveData.value = bundle.getString("path")
            }
            if (bundle.containsKey("type")) {
                val type = bundle.getString("type")
                viewModel.startTypeData = when (type) {
                    "selectDirectents" -> {
                        FileManagerViewModel.StartType.SELECT_DIRECTORY
                    }
                    else -> {
                        FileManagerViewModel.StartType.DEFAULT
                    }
                }
            }
            if (bundle.containsKey("rootpath")) {
                viewModel.setRootPath(bundle.getString("rootpath"))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuBinding = MenuBinding.inflate(menu, menuInflater)
        val value = viewModel.sortTypeLiveData.value
        if (value != null) {
            setSortType(value)
        }
        if (viewModel.startTypeData != FileManagerViewModel.StartType.SELECT_FILE) {
            menu.removeItem(R.id.selectFile)
        }
        menuBinding.actionSortByType.setOnMenuItemClickListener {
            viewModel.sortTypeLiveData.value = FileManagerViewModel.SortType.BY_TYPE
            true
        }
        menuBinding.actionSortByName.setOnMenuItemClickListener {
            viewModel.sortTypeLiveData.value = FileManagerViewModel.SortType.BY_NAME
            true
        }
        menuBinding.actionSortBySize.setOnMenuItemClickListener {
            viewModel.sortTypeLiveData.value = FileManagerViewModel.SortType.BY_SIZE
            true
        }
        menuBinding.actionSortByLastModified.setOnMenuItemClickListener {
            viewModel.sortTypeLiveData.value = FileManagerViewModel.SortType.BY_LAST_MODIFIED
            true
        }
        loadMineBookmarksMenu()
        menuBinding.bookmarkManagerItem.setOnMenuItemClickListener {
            val intent = Intent(this, BookmarkManagerActivity::class.java)
            startActivity(intent)
            true
        }
        return true
    }


    /**
     * 加载我的书签列表
     */
    fun loadMineBookmarksMenu() {
        if (this::menuBinding.isInitialized) {
            menuBinding.mineBookmarksMenu.subMenu.clear()
            viewModel.getBookmarkManager().fromList(object : BookmarkListener {
                override fun find(path: String, name: String) {
                    val item = menuBinding.mineBookmarksMenu.subMenu.add(name)
                    item.setOnMenuItemClickListener {
                        viewModel.currentPathLiveData.value = path
                        true
                    }
                }
            })
        }
    }


    /**
     * 加载观察者
     */
    fun loadObserve() {
        viewModel.loadStateLiveData.observe(this) {
            viewBinding.fileTabNav.isVisible = !it
            viewBinding.swipeRefreshLayout.isVisible = !it
            viewBinding.fileError.isVisible = it
            viewBinding.progressBar.isVisible = it
        }
        viewModel.sortTypeLiveData.observe(this) {
            setSortType(it)
        }
        viewModel.fileListLiveData.observe(this) {
            if (adapter == null) {
                //创建实例（设置适配器）
                adapter = FileAdapter(this, it)
                adapter?.setItemEvent { i, fileItemBinding, viewHolder, file ->
                    fileItemBinding.root.setOnClickListener {
                        if (file == null) {
                            viewModel.returnDirects()
                        } else {
                            if (file.isDirectory) {
                                viewModel.currentPathLiveData.value = file.absolutePath
                            }
                        }
                    }
                    fileItemBinding.more.setOnClickListener {
                        if (file == null) {
                            return@setOnClickListener
                        }
                        val finalFile = file
                        val popupMenu = PopupMenu(this, fileItemBinding.more)
                        popupMenu.inflate(R.menu.menu_files_actions)
                        val bookAction = popupMenu.menu.findItem(R.id.bookmarkAction)
                        bookAction.title = if (viewModel.getBookmarkManager().contains(finalFile)) {
                            getString(R.string.remove_bookmark)
                        } else {
                            getString(R.string.add_bookmark)
                        }
                        popupMenu.show()
                        popupMenu.setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.bookmarkAction -> {
                                    if (viewModel.getBookmarkManager().contains(finalFile)) {
                                        val remove =
                                            viewModel.getBookmarkManager()
                                                .removeBookmark(finalFile.absolutePath)
                                        if (remove) {
                                            Snackbar.make(
                                                viewBinding.fab,
                                                R.string.remove_bookmark_success,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Snackbar.make(
                                                viewBinding.fab,
                                                R.string.remove_bookmark_fail,
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        val add =
                                            viewModel.getBookmarkManager()
                                                .addBookmark(finalFile.absolutePath, finalFile.name)
                                        if (add) {
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
                                    }
                                    viewModel.getBookmarkManager().save()
                                    loadMineBookmarksMenu()
                                }
                                R.id.renameAction -> {
                                    val finalFile =
                                        adapter!!.getItemData(viewHolder.absoluteAdapterPosition)
                                    val oldname = finalFile!!.name
                                    InputDialog(this).setInputCanBeEmpty(false)
                                        .setTitle(R.string.rename).setMaxNumber(255)
                                        .setHint(R.string.file_name).setText(oldname)
                                        .setPositiveButton(R.string.dialog_ok) { string ->
                                            if (string.isNotEmpty() && string != oldname) {
                                                val newFile =
                                                    File(FileOperator.getSuperDirectory(finalFile) + "/" + string)
                                                finalFile.renameTo(newFile)
                                                adapter?.replaceItem(
                                                    newFile,
                                                    viewHolder.absoluteAdapterPosition
                                                )
                                            }
                                            true
                                        }.setNegativeButton(R.string.dialog_cancel) {

                                        }.setCancelable(false).show()
                                }
                                R.id.deleteAction -> {
                                    val finalFile =
                                        adapter!!.getItemData(viewHolder.absoluteAdapterPosition)
                                    val tip = String.format(
                                        getString(R.string.delete_prompt),
                                        finalFile!!.name
                                    )
                                    CoreDialog(this).setTitle(R.string.delete_title)
                                        .setMessage(tip)
                                        .setPositiveButton(R.string.delete_title) {
                                            val delete = FileOperator.delete_files(finalFile)
                                            if (delete) {
                                                adapter?.removeItem(i)
                                            }
                                        }.setNegativeButton(R.string.dialog_cancel) {

                                        }.show()
                                }
                            }
                            true
                        }
                    }
                }
                viewBinding.recyclerView.adapter = adapter
            } else {
                adapter?.setNewDataList(it)
            }
        }
        viewModel.currentPathLiveData.observe(this) {
            if (it==null)
            {
                return@observe
            }
            val root = getString(R.string.root_path)
            val path = root + it.substring(viewModel.getRootPath().length)
            val lineParser = LineParser(path)
            lineParser.symbol = "/"
            lineParser.parserSymbol = true
            val fileTabList = ArrayList<FileTab>()
            val stringBuilder = StringBuilder()
            lineParser.analyse { lineNum, lineData, isEnd ->
                stringBuilder.append(lineData)
                if (lineData.isNotBlank() && lineData != lineParser.symbol) {
                    val tab = FileTab(
                        lineData,
                        viewModel.getRootPath() + stringBuilder.toString().substring(root.length)
                    )
                    fileTabList.add(tab)
                }
                true
            }
            val adapter = FileTabAdapter(this, fileTabList)
            adapter.setItemEvent { i, itemFileTabBinding, viewHolder, fileTab ->
                itemFileTabBinding.button.setOnClickListener {
                    viewModel.currentPathLiveData.value = fileTab.path
                }
            }
            val manager = viewBinding.fileTabNav.layoutManager as LinearLayoutManager
            manager.scrollToPosition(fileTabList.size - 1)
            viewBinding.fileTabNav.adapter = adapter
            viewModel.loadFiles(it)
        }
    }

    /**
     * 设置排序方式
     * @param sortType SortType
     */
    fun setSortType(sortType: FileManagerViewModel.SortType) {
        if (this::menuBinding.isInitialized) {
            when (sortType) {
                FileManagerViewModel.SortType.BY_NAME -> {
                    menuBinding.actionSortByName.isChecked = true
                }
                FileManagerViewModel.SortType.BY_SIZE -> {
                    menuBinding.actionSortBySize.isChecked = true
                }
                FileManagerViewModel.SortType.BY_LAST_MODIFIED -> {
                    menuBinding.actionSortByLastModified.isChecked = true
                }
                FileManagerViewModel.SortType.BY_TYPE -> {
                    menuBinding.actionSortByType.isChecked = true
                }
                else -> {
                    menuBinding.actionSortByName.isChecked = true
                }
            }
            adapter?.setSort(sortType)
            viewModel.loadFiles(viewModel.getCurrentPath())
            viewModel.saveSortType(this)
        }
    }

    /**
     * 加载页面标题
     */
    fun loadTitle() {
        title = when (viewModel.startTypeData) {
            FileManagerViewModel.StartType.DEFAULT -> {
                getString(R.string.file_manager)
            }
            else -> {
                getString(R.string.file_manager)
            }
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityFileBinding {
        return ActivityFileBinding.inflate(layoutInflater)
    }


    class MenuBinding private constructor(
        val menu: Menu,
        val reloadFileItem: MenuItem,
        val photoAlbumItem: MenuItem,
        val systemFileManagerItem: MenuItem,
        val actionSortByName: MenuItem,
        val actionSortByType: MenuItem,
        val actionSortBySize: MenuItem,
        val actionSortByLastModified: MenuItem,
        val bookmarkItem: MenuItem,
        val bookmarkManagerItem: MenuItem, val mineBookmarksMenu: MenuItem
    ) {
        companion object {
            //填充
            fun inflate(menu: Menu, inflater: MenuInflater): MenuBinding {
                inflater.inflate(R.menu.menu_files, menu)
                return MenuBinding(
                    menu,
                    menu.findItem(R.id.reloadFile),
                    menu.findItem(R.id.photo_album),
                    menu.findItem(R.id.system_file_manager),
                    menu.findItem(R.id.action_sort_by_name),
                    menu.findItem(R.id.action_sort_by_type),
                    menu.findItem(R.id.action_sort_by_size),
                    menu.findItem(R.id.action_sort_by_last_modified),
                    menu.findItem(R.id.action_bookmark),
                    menu.findItem(R.id.bookmark_manager),
                    menu.findItem(R.id.mine_bookmarks)
                )
            }
        }
    }
}