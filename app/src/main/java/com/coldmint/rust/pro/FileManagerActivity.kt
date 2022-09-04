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
import android.widget.Toast.makeText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.coldmint.dialog.CoreDialog
import com.coldmint.dialog.InputDialog
import com.coldmint.rust.core.tool.DebugHelper
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors
import javax.sql.CommonDataSource
import kotlin.collections.ArrayList

class FileManagerActivity : BaseActivity<ActivityFileBinding>() {


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

//    override fun onPause() {
//        viewModel.getBookmarkManager().save()
//        super.onPause()
//    }

    override fun onResume() {
        viewModel.getBookmarkManager().load()
        loadMineBookmarksMenu()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (viewModel.startTypeData == FileManagerViewModel.StartType.SELECT_FILE && requestCode == 1) {
//                val path = FileOperator.parsePicturePath(this@FileManagerActivity, data)
//                if (path != null) {
//                    val intent = Intent()
//                    intent.putExtra("File", path)
//                    setResult(RESULT_OK, intent)
//                    finish()
//                }
            } else if (viewModel.startTypeData == FileManagerViewModel.StartType.SELECT_FILE && requestCode == 2) {
//                val path = viewModel.parseFilePath(this@FileManagerActivity, data)
//                if (path != null) {
//                    val intent = Intent()
//                    intent.putExtra("File", path)
//                    setResult(RESULT_OK, intent)
////                    bookmarkManager.save()
//                    finish()
//                }
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

    lateinit var photoAlbumResultLauncher: ActivityResultLauncher<Intent>

    lateinit var systemFileManagerResultLauncher: ActivityResultLauncher<String>


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
//            R.id.photo_album -> {
//                this@FileManagerActivity.startActivityForResult(
//                    Intent(
//                        Intent.ACTION_PICK,
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                    ), 1
//                )
//                return true
//            }
//            R.id.system_file_manager -> {
//                val intent = Intent(Intent.ACTION_GET_CONTENT)
//                intent.type = "*/*"
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                this@FileManagerActivity.startActivityForResult(intent, 2)
//                return true
//            }
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
                finish()
                true
            } else {
                viewModel.returnDirects()
                false
            }
        } else super.onKeyDown(keyCode, event)
    }

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

//                FileManagerViewModel.StartType.SELECT_FILE -> {
//
//                }
                FileManagerViewModel.StartType.DEFAULT, FileManagerViewModel.StartType.SELECT_FILE, FileManagerViewModel.StartType.SELECT_DIRECTORY -> {
                    val popupMenu = PopupMenu(this@FileManagerActivity, viewBinding.fab)
                    if (adapter != null) {
                        val selectPath = adapter!!.selectPath
                        if (selectPath != null) {
                            if (adapter!!.isCopyFile) {
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
                            getText(R.string.copy_to_this) -> {
                                val job = Job()
                                val handler = Handler(Looper.getMainLooper())
                                val scope = CoroutineScope(job)
                                scope.launch {
                                    val oldFile = File(adapter!!.selectPath)
                                    val newFile =
                                        File(viewModel.getCurrentPath() + "/" + oldFile.name)
                                    DebugHelper.printLog(
                                        "文件管理器",
                                        "复制文件 旧文件${oldFile.absolutePath} 新文件${newFile.absolutePath}"
                                    )
                                    adapter!!.cleanSelectPath()
                                    if (FileOperator.copyFiles(oldFile, newFile)) {
                                        handler.post {
                                            viewModel.loadFiles(viewModel.getCurrentPath())
                                        }
                                    } else {

                                        handler.post {
                                            Snackbar.make(
                                                viewBinding.fab,
                                                getText(R.string.copy_failed),
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                            getText(R.string.cut_to_this) -> {
                                val job = Job()
                                val handler = Handler(Looper.getMainLooper())
                                val scope = CoroutineScope(job)
                                scope.launch {
                                    val oldFile = File(adapter!!.selectPath)
                                    val newFile =
                                        File(viewModel.getCurrentPath() + "/" + oldFile.name)
                                    DebugHelper.printLog(
                                        "文件管理器",
                                        "移动文件 旧文件${oldFile.absolutePath} 新文件${newFile.absolutePath}"
                                    )
                                    adapter!!.cleanSelectPath()
                                    if (FileOperator.removeFiles(oldFile, newFile)) {
                                        handler.post {
                                            viewModel.loadFiles(viewModel.getCurrentPath())
                                        }
                                    } else {
                                        handler.post {
                                            Snackbar.make(
                                                viewBinding.fab,
                                                getText(R.string.cut_failed),
                                                Snackbar.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
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


    /**
     * 使用系统文件选择器选择回调
     * @param uri Uri
     */
    fun selectFileCallback(uri: Uri?) {
        val path = viewModel.parseFilePath(this, uri)
        if (path != null) {
            CoreDialog(this).setTitle(R.string.system_file_manager).setMessage(path)
                .setPositiveButton(R.string.select_file) {
                    setResultAndFinish(path)
                }.setNegativeButton(R.string.dialog_cancel) {

                }.setCancelable(false).show()

        } else {
            Snackbar.make(
                viewBinding.fab,
                R.string.bad_file_type,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

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
            photoAlbumResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    selectFileCallback(it?.data?.data)
                }
            systemFileManagerResultLauncher =
                registerForActivityResult(ActivityResultContracts.GetContent()) {
                    selectFileCallback(it)
                }
            FastScrollerBuilder(viewBinding.recyclerView).useMd2Style()
                .setPopupTextProvider(adapter).build()
        } else {
            val bundle = intent.getBundleExtra("data")
            if (bundle != null) {
                if (bundle.containsKey("path")) {
                    viewModel.currentPathLiveData.value = bundle.getString("path")
                }
                viewModel.additionalData = bundle.getString("additionalData")
                if (bundle.containsKey("type")) {
                    val type = bundle.getString("type")
                    viewModel.startTypeData = when (type) {
                        "selectDirectents" -> {
                            Snackbar.make(
                                viewBinding.fab,
                                R.string.select_directents,
                                Snackbar.LENGTH_INDEFINITE
                            ).setAction(R.string.dialog_ok) {
                                intent.putExtra("Directents", viewModel.getCurrentPath())
                                setResult(RESULT_OK, intent)
                                finish()
                            }.setGestureInsetBottomIgnored(true).show()
                            FileManagerViewModel.StartType.SELECT_DIRECTORY
                        }
                        "exportFile" -> {
                            Snackbar.make(
                                viewBinding.fab,
                                R.string.export_file,
                                Snackbar.LENGTH_INDEFINITE
                            ).setAction(R.string.dialog_ok) {
                                val oldFile = File(viewModel.additionalData)
                                val result = FileOperator.copyFile(
                                    oldFile,
                                    File(viewModel.getCurrentPath() + "/" + oldFile.name)
                                )
                                if (result) {
                                    setResult(RESULT_OK)
                                }
                                finish()
                            }.setGestureInsetBottomIgnored(true).show()
                            FileManagerViewModel.StartType.EXPORT_FILE
                        }
                        "selectFile" -> {
                            FileManagerViewModel.StartType.SELECT_FILE
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
        menuBinding.systemFileManagerItem.setOnMenuItemClickListener {
            systemFileManagerResultLauncher.launch("*/*")
            true
        }
        menuBinding.photoAlbumItem.setOnMenuItemClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            photoAlbumResultLauncher.launch(intent)
            true
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
     * 设置结果并结束界面
     */
    fun setResultAndFinish(path: String) {
        val temIntent = Intent()
        temIntent.putExtra("File", path)
        setResult(RESULT_OK, temIntent)
        finish()
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
                            } else {
                                //文件点击事件
                                if (viewModel.startTypeData == FileManagerViewModel.StartType.SELECT_FILE) {
                                    Snackbar.make(
                                        viewBinding.fab,
                                        R.string.select_file,
                                        Snackbar.LENGTH_SHORT
                                    ).setAction(R.string.dialog_ok) {
                                        setResultAndFinish(file.absolutePath)
                                    }.setGestureInsetBottomIgnored(true).show()
                                } else if (viewModel.startTypeData == FileManagerViewModel.StartType.DEFAULT) {
                                    val type = FileOperator.getFileType(file)
                                    if (type == "ini" || type == "txt") {
                                        val intent = Intent(this, EditActivity::class.java)
                                        val bundle = Bundle()
                                        bundle.putString("path", file.absolutePath)
                                        bundle.putString(
                                            "modPath",
                                            FileOperator.getSuperDirectory(file)
                                        )
                                        intent.putExtra("data", bundle)
                                        startActivity(intent)
                                    } else {
                                        Snackbar.make(
                                            viewBinding.fab, String.format(
                                                getString(R.string.an_unsupported_file_type),
                                                type
                                            ), Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
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
                                R.id.copyAction -> {
                                    val finalFile =
                                        adapter!!.getItemData(viewHolder.absoluteAdapterPosition)
                                    adapter!!.setSelectPath(finalFile!!.absolutePath, true)
                                }
                                R.id.cutOffAction -> {
                                    val finalFile =
                                        adapter!!.getItemData(viewHolder.absoluteAdapterPosition)
                                    adapter!!.setSelectPath(finalFile!!.absolutePath, false)
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
            if (it == null) {
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