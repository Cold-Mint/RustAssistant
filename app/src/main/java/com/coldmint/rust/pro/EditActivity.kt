package com.coldmint.rust.pro

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.SpannableString
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.ItemListener
import com.afollestad.materialdialogs.list.listItems
import com.coldmint.rust.core.AnalysisResult
import com.coldmint.rust.core.CodeCompiler2
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.dataBean.CompileConfiguration
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.ValueTypeInfo
import com.coldmint.rust.core.database.file.FileDataBase
import com.coldmint.rust.core.interfaces.CodeCompilerListener
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.adapters.CompileLogAdapter
import com.coldmint.rust.pro.adapters.FileAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databean.ErrorInfo
import com.coldmint.rust.pro.databinding.ActivityEditBinding
import com.coldmint.rust.pro.databinding.EditEndBinding
import com.coldmint.rust.pro.databinding.EditStartBinding
import com.coldmint.rust.pro.edit.CodeToolAdapter
import com.coldmint.rust.pro.edit.RustCompletionAdapter
import com.coldmint.rust.pro.edit.RustLanguage
import com.coldmint.rust.pro.interfaces.BookmarkListener
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.CompletionItemConverter
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.viewmodel.EditEndViewModel
import com.coldmint.rust.pro.viewmodel.EditStartViewModel
import com.coldmint.rust.pro.viewmodel.EditViewModel
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.github.rosemoe.sora.data.CompletionItem
import java.io.File
import java.util.*
import io.github.rosemoe.sora.widget.EditorColorScheme
import kotlin.collections.ArrayList


class EditActivity : BaseActivity<ActivityEditBinding>() {
    private val viewModel by lazy {
        ViewModelProvider(this).get(EditViewModel::class.java)
    }

    private lateinit var rustLanguage: RustLanguage

    private var fileAdapter: FileAdapter? = null
    val symbolChannel by lazy {
        viewBinding.codeEditor.createNewSymbolChannel()
    }

    //是第一次启动嘛
    var isFirst = true

    /**
     * 编辑器左侧视图
     */
    private val editStartBinding: EditStartBinding by lazy {
        EditStartBinding.bind(viewBinding.root)
    }

    /**
     * 编辑器左侧视图模型
     */
    private val editStartViewModel: EditStartViewModel by lazy {
        ViewModelProvider(this).get(EditStartViewModel::class.java)
    }

    /**
     * 编辑器右侧视图模型
     */
    private val editEndViewModel: EditEndViewModel by lazy {
        ViewModelProvider(this).get(EditEndViewModel::class.java)
    }

    /**
     * 编辑器右侧视图
     */
    private val editEndBinding: EditEndBinding by lazy {
        EditEndBinding.bind(viewBinding.root)
    }

    /**
     * 加载主要的观察者
     */
    fun loadMainObserve() {
        viewModel.needSaveLiveData.observe(this) {
            if (it) {
                MaterialDialog(this).show {
                    title(R.string.edit_function).message(R.string.text_changed)
                        .positiveButton(R.string.edit_function) {
                            viewModel.saveAllFile(
                                viewBinding.tabLayout.selectedTabPosition,
                                viewBinding.codeEditor.text.toString()
                            ) {
                                viewModel.needCheckAutoSave = false
                                finish()
                            }
                        }.negativeButton(R.string.dialog_cancel)
                        .neutralButton(R.string.not_save_exit).neutralButton {
                            MaterialDialog(this@EditActivity).show {
                                title(R.string.not_save_exit).message(R.string.not_save_exit_tip)
                                    .negativeButton(R.string.dialog_cancel)
                                    .positiveButton(R.string.dialog_ok) {
                                        viewModel.needCheckAutoSave = false
                                        finish()
                                    }.cancelable(false)
                            }
                        }.cancelable(false)
                }
            }
        }

        viewModel.englishModeLiveData.observe(this) {
            rustLanguage.setEnglish(it)
        }

        viewBinding.codeEditor.setItemListener { i, completionItem ->
            viewModel.executorService.submit {
                val extrasData = completionItem.extrasData
                if (extrasData != null) {
                    val listData = extrasData.getString("list")
                    if (listData != null) {
                        val lineParser = LineParser(listData)
                        lineParser.symbol = ","
                        val list = ArrayList<CompletionItem>()
                        lineParser.analyse { lineNum, lineData, isEnd ->
                            val temCodeInfo =
                                CodeDataBase.getInstance(this).getCodeDao().findCodeByCode(lineData)
                            if (temCodeInfo == null) {

                            } else {
                                val completionItemConverter = CompletionItemConverter.instance
                                completionItemConverter.init(this)
                                list.add(
                                    completionItemConverter.codeInfoToCompletionItem(
                                        temCodeInfo
                                    )
                                )
                            }
                            true
                        }
                        if (list.isNotEmpty()) {
                            runOnUiThread {
                                viewBinding.codeEditor.createEditorAutoCompleteList(list)
                            }
                        }
                    }
                }
            }
            true
        }
        viewModel.openedSourceFileListLiveData.observe(this) {
            viewBinding.tabLayout.removeAllTabs()
            it.forEach {
                val openedSourceFile = it
                val tab = viewBinding.tabLayout.newTab()
                tab.text = if (openedSourceFile.isNeedSave()) {
                    String.format(
                        getString(R.string.need_save),
                        openedSourceFile.file.name
                    )
                } else {
                    openedSourceFile.file.name
                }
                tab.view.setOnClickListener { view ->
                    val path = it.file.absolutePath
                    if (viewModel.getNowOpenFilePath() != path) {
                        //更新Tab文本
                        val selectedTabPosition = viewBinding.tabLayout.selectedTabPosition
                        val oldTab = viewBinding.tabLayout.getTabAt(selectedTabPosition)
                        val oldOpenedSourceFile =
                            viewModel.openedSourceFileListLiveData.getOpenedSourceFile(
                                selectedTabPosition
                            )
                        val isChanged = oldOpenedSourceFile
                            .isChanged(viewBinding.codeEditor.text.toString())
                        if (isChanged) {
                            oldTab?.text = String.format(
                                getString(R.string.need_save),
                                oldOpenedSourceFile.file.name
                            )
                        }
                        viewModel.setNowOpenFilePath(path)
                        viewModel.codeLiveData.value = openedSourceFile.getEditText()
                    }
                }
                tab.view.setOnLongClickListener {
                    val popupMenu = PopupMenu(this@EditActivity, it)
                    popupMenu.menu.add(R.string.open_directory_of_file)
                    if (viewModel.openedSourceFileListLiveData.value.size > 1) {
                        popupMenu.menu.add(R.string.close)
                    }
                    popupMenu.setOnMenuItemClickListener {
                        when (it.title.toString()) {
                            getString(R.string.close) -> {
                                if (openedSourceFile.isNeedSave()) {
                                    MaterialDialog(this).show {
                                        title(R.string.edit_function).message(
                                            R.string.text_changed
                                        ).positiveButton(R.string.edit_function).positiveButton {
                                            viewModel.saveOneFile(openedSourceFile)
                                            viewModel.closeFile(openedSourceFile)
                                        }
                                            .negativeButton(R.string.dialog_cancel).negativeButton {
                                                viewModel.closeFile(openedSourceFile)
                                            }
                                    }
                                } else {
                                    viewModel.closeFile(openedSourceFile)
                                }
                            }
                            getString(R.string.open_directory_of_file) -> {
                                editStartViewModel.loadPathLiveData.value =
                                    FileOperator.getSuperDirectory(openedSourceFile.file)
                                viewBinding.editDrawerlayout.openDrawer(GravityCompat.START)
                            }
                        }
                        false
                    }
                    popupMenu.show()
                    true
                }
                viewBinding.tabLayout.addTab(tab)
                if (openedSourceFile.file.absolutePath == viewModel.getNowOpenFilePath()) {
                    viewBinding.tabLayout.selectTab(tab)
                    viewModel.codeLiveData.value = openedSourceFile.getEditText()
                }
            }
        }

        viewModel.codeLiveData.observe(this) {
            rustLanguage.autoCompleteProvider.setSourceFolder(
                FileOperator.getSuperDirectory(
                    viewModel.getNowOpenFilePath()
                )
            )
            //初始化加载路径数据（仅在第一次有效）
            editStartViewModel.initLoadPathLiveData(viewModel.getNowOpenFilePath())
            viewBinding.myProgressBar.isVisible = false
            viewBinding.codeEditor.isVisible = true
            viewBinding.codeEditor.setText(it)
        }
        viewModel.loadingLiveData.observe(
            this
        ) {
            if (it) {
                viewBinding.myProgressBar.isVisible = true
                viewBinding.codeEditor.isVisible = false
            } else {
                viewBinding.myProgressBar.isVisible = false
                viewBinding.codeEditor.isVisible = true
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                //在找不到资源选择文件
                1 -> {
                    val path = data?.getStringExtra("File") ?: return
                    val file = File(path)
                    val targetFile = viewModel.targetFile
                    if (targetFile != null) {
                        val targetType = FileOperator.getFileType(targetFile)
                        val nowType = FileOperator.getFileType(file)
                        if (targetType == nowType) {
                            val copyResult = FileOperator.copyFile(file, targetFile)
                            if (!copyResult) {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    getText(R.string.copy_file_error),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                viewModel.addFileInDataBase(targetFile)
                            }
                        } else {
                            Snackbar.make(
                                viewBinding.recyclerview,
                                getText(R.string.bad_file_type),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                2 -> {
                    //新建源文件
                    val path = data?.getStringExtra("File") ?: return
                    val file = File(path)
                    viewModel.openFile(path)
                    viewModel.addFileInDataBase(file)
                    editStartViewModel.loadPathLiveData.value = FileOperator.getSuperDirectory(file)
                }
                3, 4 -> {
//左侧选择文件(4为相册选择)
                    val path = if (requestCode == 3) {
                        data?.getStringExtra("File")
                    } else {
                        FileOperator.parsePicturePath(this, data)
                    }
                    if (path == null) {
                        return
                    }
                    val file = File(path)
                    val copyFile = File(editStartViewModel.loadPathLiveData.value + "/" + file.name)
                    val copyResult =
                        FileOperator.copyFile(file, copyFile)
                    if (!copyResult) {
                        Snackbar.make(
                            viewBinding.recyclerview,
                            getText(R.string.copy_file_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        viewModel.addFileInDataBase(copyFile)
                        editStartViewModel.reloadList()
                    }
                }
            }
        }
    }

    /**
     * 初始化侧滑视图
     */
    private fun initDrawerLayout() {
        viewBinding.editDrawerlayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
                viewBinding.editDrawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
    }

    /**
     * 加载右侧观察者
     */
    fun loadEndObserve() {
        editEndViewModel.loadStateLiveData.observe(this) {
            editEndBinding.imageview.isVisible = it
            editEndBinding.textview.isVisible = it
            editEndBinding.logView.isVisible = !it
        }

        editEndViewModel.analysisResultLiveData.observe(this) {
            val adapter = CompileLogAdapter(this, it.toMutableList())
            editEndBinding.logView.adapter = adapter
        }
    }

    /**
     * 初始化右侧视图
     */
    fun initEndView() {
        editEndBinding.logView.layoutManager = LinearLayoutManager(this)
    }

    //当用户切换到其他应用界面时
    override fun onPause() {
        if (viewModel.needCheckAutoSave) {
            val need = appSettings.getValue(AppSettings.Setting.AutoSave, true)
            if (need) {
                viewModel.saveAllFile(
                    viewBinding.tabLayout.selectedTabPosition,
                    viewBinding.codeEditor.text.toString(), {
                        Toast.makeText(this, R.string.auto_save_toast, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (isFirst) {
            isFirst = false
        } else {
            viewModel.needCheckAutoSave = true
        }
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setSupportActionBar(viewBinding.toolbar)
            setReturnButton()
            loadStartObserve()
            loadEndObserve()
            initDrawerLayout()
            initCodeEditor()
            initCodeToolbar()
            initStartView()
            initEndView()
        } else {
            val thisIntent = intent
            val bundle = thisIntent.getBundleExtra("data")
            if (bundle == null) {
                showError("请传入bundle")
                return
            }
            val path = bundle.getString("path")
            if (path == null) {
                showError("请传入路径")
                return
            }
            val modPath = bundle.getString("modPath")
            if (modPath == null) {
                showError("请传入模组路径")
                return
            }
            viewModel.modClass = ModClass(File(modPath))
            loadMainObserve()
            viewModel.loadData()
            viewModel.openFile(path)
        }
    }


    private fun initStartView() {
        editStartBinding.fileList.layoutManager = LinearLayoutManager(this)
        editStartBinding.fab.setOnClickListener {
            val popupMenu = PopupMenu(this@EditActivity, editStartBinding.fab)
            if (fileAdapter != null) {
                val selectPath = fileAdapter!!.selectPath
                if (selectPath != null) {
                    if (fileAdapter!!.isCopyFile && !viewModel.processFiles) {
                        popupMenu.menu.add(R.string.copy_to_this)
                    } else {
                        popupMenu.menu.add(R.string.cut_to_this)
                    }
                }
            }
            popupMenu.menu.add(R.string.create_unit)
            popupMenu.menu.add(R.string.create_folder)
            popupMenu.menu.add(R.string.select_file)
            popupMenu.menu.add(R.string.select_the_image_in_the_album)
            popupMenu.setOnMenuItemClickListener { item ->
                val title = item.title
                val handler = Handler(Looper.getMainLooper())
                when (title) {
                    getText(R.string.create_unit) -> {
                        viewModel.needCheckAutoSave = false
                        val intent = Intent(this@EditActivity, CreateUnitActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("modPath", viewModel.modClass?.modFile?.absolutePath)
                        bundle.putString("createPath", editStartViewModel.loadPathLiveData.value)
                        intent.putExtra("data", bundle)
                        startActivityForResult(intent, 2)
                    }
                    getString(R.string.select_the_image_in_the_album) -> {
                        this.startActivityForResult(
                            Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            ), 4
                        )
                    }
                    getText(R.string.select_file) -> {
                        viewModel.needCheckAutoSave = false
                        val bundle = Bundle()
                        val intent = Intent(this@EditActivity, FileManagerActivity::class.java)
                        bundle.putString("type", "selectFile")
                        //bundle.putString("path", modClass.getModFile().getAbsolutePath());
                        intent.putExtra("data", bundle)
                        startActivityForResult(intent, 3)
                    }
                    getText(R.string.create_folder) -> {
                        MaterialDialog(this).show {
                            title(R.string.create_folder)
                            input(maxLength = 255, waitForPositiveButton = false) { dialog, text ->
                                if (text.length in 1..255) {
                                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                                }
                            }.positiveButton(R.string.dialog_ok, null) { dialog ->
                                val string = dialog.getInputField().text.toString()
                                if (!string.isEmpty()) {
                                    val file =
                                        File(editStartViewModel.loadPathLiveData.value + "/" + string)
                                    if (file.exists()) {
                                        Toast.makeText(
                                            this@EditActivity,
                                            R.string.folder_error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        file.mkdirs()
                                        editStartViewModel.reloadList()
                                    }
                                }
                            }.negativeButton(R.string.dialog_close)
                        }
                    }
                    getText(R.string.copy_to_this) -> {
                        viewModel.executorService.submit {
                            viewModel.processFiles = true
                            val selectPath = fileAdapter?.selectPath ?: return@submit
                            val nowPath = editStartViewModel.loadPathLiveData.value
                            val oldFile = File(selectPath)
                            val newFile = File(nowPath + "/" + oldFile.name)
                            if (FileOperator.copyFiles(oldFile, newFile)) {
                                handler.post {
                                    fileAdapter?.cleanSelectPath()
                                    viewModel.processFiles = false
                                    editStartViewModel.reloadList()
                                }
                            } else {
                                handler.post {
                                    Toast.makeText(
                                        this@EditActivity,
                                        getText(R.string.copy_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.processFiles = false
                                }
                            }
                        }
                    }
                    getText(R.string.cut_to_this) -> {
                        viewModel.executorService.submit {
                            viewModel.processFiles = true
                            val selectPath = fileAdapter?.selectPath ?: return@submit
                            val nowPath = editStartViewModel.loadPathLiveData.value
                            val oldFile = File(selectPath)
                            val newFile = File(nowPath + "/" + oldFile.name)
                            if (FileOperator.removeFiles(oldFile, newFile)) {
                                handler.post {
                                    fileAdapter?.cleanSelectPath()
                                    editStartViewModel.reloadList()
                                    viewModel.processFiles = false
                                }
                            } else {
                                handler.post {
                                    Toast.makeText(
                                        this@EditActivity,
                                        getText(R.string.cut_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.processFiles = false
                                }
                            }
                        }
                    }
                }
                false
            }
            popupMenu.show()
        }
    }


    /**
     * 加载左侧观察者
     */
    private fun loadStartObserve() {
        //加载状态改变
        editStartViewModel.loadStatusLiveData.observe(this) {
            editStartBinding.fab.isVisible = it
            editStartBinding.unableOpenView.isVisible = !it
            editStartBinding.fileList.isVisible = it
        }


        //文件列表加载的路径改变
        editStartViewModel.loadPathLiveData.observe(this) {
            editStartViewModel.loadList(it)
        }

        //文件列表的数据改变
        editStartViewModel.fileListLiveData.observe(this) {
            val finalFileAdapter: FileAdapter =
                if (fileAdapter == null) {
                    fileAdapter = FileAdapter(this, it)
                    fileAdapter!!
                } else {
                    fileAdapter?.setNewDataList(it)
                    fileAdapter!!
                }
            finalFileAdapter.setItemEvent { i, fileItemBinding, viewHolder, file ->
                fileItemBinding.contentView.setOnClickListener {
                    if (file == null) {
                        editStartViewModel.loadPathLiveData.value = FileOperator.getSuperDirectory(
                            editStartViewModel.loadPathLiveData.value ?: ""
                        )
                    } else {
                        if (file.isDirectory) {
                            editStartViewModel.loadPathLiveData.value = file.absolutePath
                        } else {
                            viewModel.openFile(file.absolutePath)
                            viewBinding.editDrawerlayout.closeDrawer(GravityCompat.START)
                        }
                    }
                }

                fileItemBinding.more.setOnClickListener {
                    if (file == null) {
                        return@setOnClickListener
                    }
                    val popupMenu = PopupMenu(this@EditActivity, it)
                    val cutBoardMenu = popupMenu.menu.addSubMenu(R.string.cut_board_operation)
                    val fileMenu = popupMenu.menu.addSubMenu(R.string.file_operation)
                    val bookmarksMenu = popupMenu.menu.addSubMenu(R.string.bookmarks_operation)
                    cutBoardMenu.add(R.string.copy_file_name)
                    cutBoardMenu.add(R.string.copy_file_path)
                    cutBoardMenu.add(R.string.copy_file_absolutely_path)
                    fileMenu.add(R.string.copy)
                    fileMenu.add(R.string.cut_off)
                    fileMenu.add(R.string.mod_action9)
                    fileMenu.add(R.string.del_mod)
                    val bookmarkManager = editStartViewModel.bookmarkManager
                    if (bookmarkManager.contains(file)) {
                        bookmarksMenu.add(R.string.remove_bookmark)
                    } else {
                        bookmarksMenu.add(R.string.add_bookmark)
                    }
                    bookmarksMenu.add(R.string.bookmark_manager)
                    val bookmarkContent: SubMenu? =
                        if (bookmarkManager.size > 0) {
                            bookmarksMenu.addSubMenu(R.string.jump_a_bookmark)
                        } else {
                            null
                        }
                    //哈希表映射(名称，路径)
                    val bookmarkMap = HashMap<String, String?>()
                    bookmarkManager.fromList(object : BookmarkListener {
                        override fun find(path: String, name: String) {
                            bookmarkMap[name] = path
                            bookmarkContent!!.add(name)
                        }

                    })
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        val title = item.title
                        if (title == getText(R.string.copy_file_name)) {
                            viewBinding.editDrawerlayout.closeDrawer(GravityCompat.START)
                            val name = file.name
                            GlobalMethod.copyText(
                                this@EditActivity,
                                name,
                                viewBinding.recyclerview
                            )
                        } else if (title == getText(R.string.copy_file_path)) {
                            viewBinding.editDrawerlayout.closeDrawer(GravityCompat.START)
                            val path = file.absolutePath
                            GlobalMethod.copyText(
                                this@EditActivity,
                                path,
                                viewBinding.recyclerview
                            )
                        } else if (title == getText(R.string.copy_file_absolutely_path)) {
                            viewBinding.editDrawerlayout.closeDrawer(GravityCompat.START)
                            if (viewModel.modClass == null) {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    getText(R.string.copy_file_absolutely_path_error),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                return@OnMenuItemClickListener true
                            }
                            var relative =
                                FileOperator.getRelativePath(
                                    file,
                                    viewModel.modClass!!.modFile
                                )
                            if (relative == null || relative.isEmpty()) {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    getText(R.string.copy_file_absolutely_path_error),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                relative = "ROOT:" + relative.substring(1)
                                GlobalMethod.copyText(
                                    this@EditActivity,
                                    relative,
                                    viewBinding.recyclerview
                                )
                            }
                        } else if (title == getText(R.string.del_mod)) {
                            val absolutePath = file.absolutePath
                            var canDel = true
                            if (viewModel.openedSourceFileListLiveData.value.isNotEmpty()) {
                                for (openedFile in viewModel.openedSourceFileListLiveData.value) {
                                    val path = openedFile.file.absolutePath
                                    if (path.startsWith(absolutePath)) {
                                        canDel = false
                                    }
                                }
                            }
                            if (!canDel) {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    R.string.unable_del,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                return@OnMenuItemClickListener false
                            }
                            if (FileOperator.delete_files(file)) {
                                viewModel.removeFileInDataBase(file)
                                editStartViewModel.reloadList()
                            }
                        } else if (title == getText(R.string.copy)) {
                            fileAdapter?.setSelectPath(file.absolutePath, true)
                        } else if (title == getText(R.string.cut_off)) {
                            val absolutePath = file.absolutePath
                            var canCut = true
                            if (viewModel.openedSourceFileListLiveData.value.isNotEmpty()) {
                                for (openedFile in viewModel.openedSourceFileListLiveData.value) {
                                    val path = openedFile.file.absolutePath
                                    if (path.startsWith(absolutePath)) {
                                        canCut = false
                                    }
                                }
                            }
                            if (!canCut) {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    R.string.unable_cut,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                return@OnMenuItemClickListener false
                            }
                            fileAdapter?.setSelectPath(file.absolutePath, false)
                        } else if (title == getText(R.string.mod_action9)) {
                            val absolutePath = file.absolutePath
                            var canRename = true
                            if (viewModel.openedSourceFileListLiveData.value.isNotEmpty()) {
                                for (openedFile in viewModel.openedSourceFileListLiveData.value) {
                                    val path = openedFile.file.absolutePath
                                    if (path.startsWith(absolutePath)) {
                                        canRename = false
                                    }
                                }
                            }
                            if (!canRename) {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    R.string.unable_rename,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                return@OnMenuItemClickListener false
                            }
                            val oldName = file.name
                            MaterialDialog(this@EditActivity).show {
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
                                    if (newName != oldName) {
                                        val reNameFile =
                                            File(editStartViewModel.loadPathLiveData.value + "/" + newName)
                                        file.renameTo(reNameFile)
                                        editStartViewModel.reloadList()
                                    }
                                }.negativeButton(R.string.dialog_close)
                            }
                        } else if (title == getString(R.string.remove_bookmark)) {
                            viewBinding.editDrawerlayout.closeDrawer(GravityCompat.START)
                            val removeBookmark =
                                bookmarkManager.removeBookmark(file.absolutePath)
                            if (removeBookmark) {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    R.string.remove_bookmark_success,
                                    Snackbar.LENGTH_SHORT
                                ).setAction(R.string.symbol10) {
                                    bookmarkManager.addBookmark(
                                        file.absolutePath,
                                        FileOperator.getPrefixName(file)
                                    )
                                    viewBinding.editDrawerlayout.openDrawer(GravityCompat.START)
                                }.show()
                            } else {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    R.string.remove_bookmark_fail,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        } else if (title == getString(R.string.add_bookmark)) {
                            viewBinding.editDrawerlayout.closeDrawer(GravityCompat.START)
                            val addBookmark = bookmarkManager.addBookmark(
                                file.absolutePath,
                                FileOperator.getPrefixName(file)
                            )
                            if (addBookmark) {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    R.string.add_bookmark_success,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                Snackbar.make(
                                    viewBinding.recyclerview,
                                    R.string.add_bookmark_fail,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        } else if (title == getString(R.string.bookmark_manager)) {
                            bookmarkManager.save()
                            viewModel.needCheckAutoSave = false
                            startActivity(
                                Intent(
                                    this@EditActivity,
                                    BookmarkManagerActivity::class.java
                                )
                            )
                        } else {
                            if (bookmarkMap.containsKey(title)) {
                                val newFile = File(bookmarkMap[title])
                                if (newFile.exists()) {
                                    if (newFile.isDirectory) {
                                        editStartViewModel.loadList(newFile.absolutePath)
                                    } else {
                                        viewModel.openFile(newFile.absolutePath)
                                    }
                                } else {
                                    viewBinding.editDrawerlayout.closeDrawer(GravityCompat.START)
                                    Snackbar.make(
                                        viewBinding.recyclerview,
                                        R.string.bookmark_jump_failed,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        false
                    })
                    popupMenu.show()
                }

            }
            editStartBinding.fileList.adapter = fileAdapter
        }
    }

    /**
     * 初始化代码工具栏
     */
    fun initCodeToolbar() {
        val items = ArrayList<String>()
        items.add(getString(R.string.symbol1))
        items.add(getString(R.string.symbol9))
        items.add(getString(R.string.code_tip))
        items.add(getString(R.string.code_table))
//        items.add(getString(R.string.code_language_on))
        items.add(getString(R.string.symbol11))
        val customSymbol = appSettings.getValue(
            AppSettings.Setting.CustomSymbol,
            "[],:='*_$%@#{}()"
        )
        val chars = customSymbol.toCharArray()
        if (chars.isNotEmpty()) {
            for (c in chars) {
                items.add(c.toString())
            }
        }
        val codeToolAdapter = CodeToolAdapter(this, items)
        codeToolAdapter.setItemEvent { i, codeToolItemBinding, viewHolder, item ->
            codeToolItemBinding.root.setOnClickListener {
                if (item == getString(R.string.symbol11)) {
                    ColorPickerDialogBuilder
                        .with(this@EditActivity)
                        .setTitle(getString(R.string.choose_color))
                        .initialColor(Color.WHITE)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener {
                            //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                        }
                        .setPositiveButton(R.string.dialog_ok) { dialog, selectedColor, allColors ->
                            val r = Color.red(selectedColor)
                            val g = Color.green(selectedColor)
                            val b = Color.blue(selectedColor)
                            val builder = StringBuilder()
                            builder.append('#')
                            builder.append(viewModel.convertDigital(r))
                            builder.append(viewModel.convertDigital(g))
                            builder.append(viewModel.convertDigital(b))
                            symbolChannel.insertSymbol(builder.toString(), builder.length)
                        }
                        .setNegativeButton(R.string.dialog_cancel) { dialog, which -> }
                        .build()
                        .show()
                } else if (item == getString(R.string.code_table)) {
                    viewModel.needCheckAutoSave = false
                    startActivity(Intent(this@EditActivity, CodeTableActivity::class.java))
                } else if (item == getString(R.string.symbol1)) {
                    //关闭手势滑动
                    viewBinding.editDrawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    openDrawer(GravityCompat.START)
                } else if (item == getString(R.string.symbol9)) {
                    if (!viewBinding.codeEditor.formatCodeAsync()) {
                        Snackbar.make(
                            viewBinding.codeEditor,
                            R.string.format_failed,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else if (item == getString(R.string.code_tip)) {
                    viewModel.executorService.submit {
                        try {
                            val list = ArrayList<CompletionItem>()
                            //如果不包含:搜索键
                            val lineNumber = viewBinding.codeEditor.selectedLineNumber
                            val navigationList =
                                viewBinding.codeEditor.textAnalyzeResult.navigation
                            var section: String? = null
                            if (navigationList != null && navigationList.isNotEmpty()) {
                                for (navigation in navigationList) {
                                    if (navigation.line > lineNumber) {
                                        break
                                    } else {
                                        section = navigation.label
                                    }
                                }
                            }
                            //如果不在任何节内
                            if (section == null) {
                                runOnUiThread {
                                    runOnUiThread {
                                        Snackbar.make(
                                            viewBinding.recyclerview,
                                            this.getString(R.string.code_tip_error1),
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                return@submit
                            }
                            val trueSection =
                                rustLanguage.autoCompleteProvider.getSectionType(section)
                            val codeDataBase = CodeDataBase.getInstance(this)
                            val lineData =
                                viewBinding.codeEditor.text.getLine(lineNumber).toString()
                            if (lineData.startsWith('[') && lineData.endsWith(']')) {
                                runOnUiThread {
                                    Snackbar.make(
                                        viewBinding.recyclerview,
                                        R.string.code_tip_error2,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                                return@submit
                            }
                            val index = lineData.indexOf(':')
                            if (index > -1) {
                                //如果包含:
                                runOnUiThread {
                                    Snackbar.make(
                                        viewBinding.recyclerview,
                                        this.getString(R.string.can_not_tip_value),
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                                return@submit
                            } else {
                                val codeInfoList = if (lineData.isBlank()) {
                                    codeDataBase.getCodeDao().findCodeBySection(trueSection)
                                } else {
                                    val number = appSettings.getValue(
                                        AppSettings.Setting.IdentifiersPromptNumber,
                                        40
                                    )
                                    codeDataBase.getCodeDao()
                                        .findCodeByKeyFromSection(lineData, trueSection, number)
                                }
                                if (codeInfoList != null && codeInfoList.isNotEmpty()) {
                                    val completionItemConverter =
                                        CompletionItemConverter.instance.init(this)
                                    codeInfoList.forEach {
                                        list.add(
                                            completionItemConverter.codeInfoToCompletionItem(
                                                it
                                            )
                                        )
                                    }
                                } else {
                                    runOnUiThread {
                                        Snackbar.make(
                                            viewBinding.recyclerview,
                                            String.format(
                                                this.getString(R.string.code_tip_error3),
                                                SourceFile.getSectionType(section)
                                            ),
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            runOnUiThread {
                                viewBinding.codeEditor.createEditorAutoCompleteList(list)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            val info = ErrorInfo()
                            info.describe = "自动保存-代码提示异常"
                            info.allErrorDetails = e.toString()
                            info.save()
                        }

                    }
                } else {
                    try {
                        symbolChannel.insertSymbol(item, item.length)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val info = ErrorInfo()
                        info.describe = "自动保存-插入符号异常"
                        info.allErrorDetails = e.toString()
                        info.save()
                    }
                }
            }
        }
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        viewBinding.recyclerview.isVisible = true
        viewBinding.recyclerview.layoutManager = linearLayoutManager
        viewBinding.recyclerview.adapter = codeToolAdapter
    }

    //初始化编辑器
    fun initCodeEditor() {
        //CodEditor初始化
        viewBinding.codeEditor.isWordwrap = true
        val useFont = appSettings.getValue(AppSettings.Setting.UseJetBrainsMonoFont, true)
        if (useFont) {
            viewBinding.codeEditor.typefaceText = Typeface.createFromAsset(
                assets,
                "JetBrainsMono-Regular.ttf"
            )
        }
        val language =
            appSettings.getValue(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
        rustLanguage = RustLanguage(this)
        rustLanguage.setCodeDataBase(CodeDataBase.getInstance(this))
        rustLanguage.setFileDataBase(
            FileDataBase.getInstance(
                this,
                viewModel.modClass!!.modName
            )
        )
//        rustLanguage.setAnalyzerEnglishMode(viewModel.englishModeLiveData)
        rustLanguage.setCodeEditor(viewBinding.codeEditor)
        val night = appSettings.getValue(AppSettings.Setting.NightMode, false)
        val editorColorScheme = EditorColorScheme()
        if (night) {
            //代码（可识别的关键字）
            editorColorScheme.setColor(EditorColorScheme.KEYWORD, Color.rgb(178, 119, 49))
            //默认文本
            editorColorScheme.setColor(EditorColorScheme.TEXT_NORMAL, Color.rgb(169, 183, 198))
            //注释
            editorColorScheme.setColor(EditorColorScheme.COMMENT, Color.rgb(128, 128, 128))
            //节
            editorColorScheme.setColor(
                EditorColorScheme.FUNCTION_NAME,
                Color.rgb(152, 118, 170)
            )
            //错误
            editorColorScheme.setColor(
                EditorColorScheme.WHOLE_BACKGROUND,
                Color.rgb(60, 63, 65)
            )
            editorColorScheme.setColor(
                EditorColorScheme.LINE_NUMBER_BACKGROUND,
                Color.rgb(60, 63, 65)
            )
        } else {
            //代码（可识别的关键字）
            editorColorScheme.setColor(EditorColorScheme.KEYWORD, Color.rgb(42, 92, 170))
            //默认文本
            editorColorScheme.setColor(EditorColorScheme.TEXT_NORMAL, Color.BLACK)
            //注释
            editorColorScheme.setColor(EditorColorScheme.COMMENT, Color.rgb(128, 128, 128))
            //节
            editorColorScheme.setColor(EditorColorScheme.FUNCTION_NAME, Color.rgb(170, 33, 22))
            editorColorScheme.setColor(
                EditorColorScheme.WHOLE_BACKGROUND,
                Color.rgb(240, 240, 243)
            )

            //变量名
            editorColorScheme.setColor(EditorColorScheme.LITERAL, Color.rgb(153, 50, 204))
        }
        viewBinding.codeEditor.colorScheme = editorColorScheme
        viewBinding.codeEditor.setAutoCompletionItemAdapter(RustCompletionAdapter())
        viewBinding.codeEditor.isVerticalScrollBarEnabled = false
        val path = viewModel.modClass?.modFile?.absolutePath ?: ""
        rustLanguage.autoCompleteProvider.setConfigurationFileConversion(
            path,
            "ROOT",
            path
        )
        viewBinding.codeEditor.setEditorLanguage(rustLanguage)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_editer, menu)
        return true
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (viewModel.checkFilesIfNeedSave(
                    viewBinding.tabLayout.selectedTabPosition,
                    viewBinding.codeEditor.text.toString()
                )
            ) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                editStartViewModel.bookmarkManager.save()
                if (viewModel.checkFilesIfNeedSave(
                        viewBinding.tabLayout.selectedTabPosition,
                        viewBinding.codeEditor.text.toString()
                    )
                ) {
                    return true
                }
            }
            R.id.turret_design -> {
                val goIntent = Intent(this, TurretDesignActivity::class.java)
                val modPath = viewModel.modClass!!.modFile.absolutePath
                val filePath = viewModel.getNowOpenFilePath()
                goIntent.putExtra("modPath", modPath)
                goIntent.putExtra("filePath", filePath)
                startActivity(goIntent)
            }
            R.id.display_source_code -> {
                val file = File(viewModel.getNowOpenFilePath())
                val code = FileOperator.readFile(file)
                MaterialDialog(this, BottomSheet()).show {
                    title(text = file.name).message(text = code).negativeButton(R.string.dialog_ok)
                }
            }
            R.id.clear_code_cache -> {
                viewModel.codeCompiler2.clearCache()
                Snackbar.make(
                    viewBinding.recyclerview,
                    getString(R.string.clean_up_code_cache_complete),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            R.id.open_game_test -> {
                val packName = appSettings.getValue(
                    AppSettings.Setting.GamePackage,
                    GlobalMethod.DEFAULT_GAME_PACKAGE
                )
                if (AppOperator.isAppInstalled(this@EditActivity, packName)) {
                    AppOperator.openApp(this@EditActivity, packName)
                } else {
                    Snackbar.make(
                        viewBinding.recyclerview,
                        R.string.no_game_installed,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            R.id.code_navigation -> {
                viewModel.executorService.submit {
                    val labels = viewBinding.codeEditor.textAnalyzeResult.navigation
                    if (labels == null || labels.size == 0) {
                        runOnUiThread {
                            Snackbar.make(
                                viewBinding.recyclerview,
                                R.string.not_find_code_navigation,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val items = ArrayList<CharSequence>()
                        var i = 0
                        while (i < labels.size) {
                            items.add(labels[i].label)
                            i++
                        }
                        val tip = String.format(getString(R.string.navigation_tip), items.size)
                        runOnUiThread {
                            MaterialDialog(this).show {
                                title(R.string.code_navigation).positiveButton(R.string.dialog_cancel)
                                    .message(text = tip)
                                    .listItems(
                                        items = items,
                                        waitForPositiveButton = false,
                                        selection = object : ItemListener {
                                            override fun invoke(
                                                dialog: MaterialDialog,
                                                index: Int,
                                                text: CharSequence
                                            ) {
                                                viewBinding.codeEditor.jumpToLine(
                                                    labels[index].line
                                                )
                                                viewBinding.codeEditor.moveSelectionEnd()
                                                dialog.dismiss()
                                            }

                                        })
                            }
                        }
                    }
                }
            }
            R.id.save_text -> {
                val openedSourceFile =
                    viewModel.openedSourceFileListLiveData.getOpenedSourceFile(viewBinding.tabLayout.selectedTabPosition)
                val needSave =
                    openedSourceFile.isChanged(viewBinding.codeEditor.text.toString())
                if (needSave) {
                    viewModel.compilerFile(openedSourceFile, object : CodeCompilerListener {
                        override fun onCompilationComplete(
                            compileConfiguration: CompileConfiguration,
                            code: String
                        ) {
                            viewModel.openedSourceFileListLiveData.getOpenedSourceFile(
                                viewBinding.tabLayout.selectedTabPosition
                            )
                                .save(code)
                            editEndViewModel.analysisResultLiveData.value =
                                compileConfiguration.getAnalysisResult()
                            editEndViewModel.loadStateLiveData.value = false
                        }


                        override fun beforeCompilation() {
                            editEndViewModel.loadStateLiveData.value = true
                            openDrawer(GravityCompat.END)
                        }

                        override fun onClickKeyNotFoundItem(
                            lineNum: Int,
                            columnNum: Int,
                            view: View,
                            code: String,
                            section: String
                        ) {
                            val popupMenu = PopupMenu(this@EditActivity, view)
                            val gotoLine = getString(R.string.goto_line)
                            popupMenu.menu.add(gotoLine)
                            popupMenu.setOnMenuItemClickListener {
                                val title = it.title
                                when (title) {
                                    gotoLine -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        viewBinding.codeEditor.setSelection(lineNum, columnNum)
                                    }
                                }
                                false
                            }
                            popupMenu.show()
                        }


                        override fun onClickValueTypeErrorItem(
                            lineNum: Int,
                            columnNum: Int,
                            view: View,
                            valueType: ValueTypeInfo
                        ) {
                            val popupMenu = PopupMenu(this@EditActivity, view)
                            val gotoLine = getString(R.string.goto_line)
                            val information =
                                String.format(getString(R.string.type_information), valueType.name)
                            popupMenu.menu.add(gotoLine)
                            popupMenu.menu.add(information)
                            popupMenu.setOnMenuItemClickListener {
                                val title = it.title
                                when (title) {
                                    gotoLine -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        viewBinding.codeEditor.setSelection(lineNum, columnNum)
                                    }
                                    information -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        MaterialDialog(this@EditActivity).show {
                                            title(text = valueType.name).message(text = valueType.describe)
                                                .negativeButton(R.string.dialog_ok)
                                        }
                                    }
                                }
                                false
                            }
                            popupMenu.show()
                        }

                        override fun onClickSectionIndexError(
                            lineNum: Int,
                            columnNum: Int,
                            view: View,
                            sectionName: String
                        ) {
                            val popupMenu = PopupMenu(this@EditActivity, view)
                            val gotoLine = getString(R.string.goto_line)
                            popupMenu.menu.add(gotoLine)
                            popupMenu.setOnMenuItemClickListener {
                                val title = it.title
                                when (title) {
                                    gotoLine -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        viewBinding.codeEditor.setSelection(lineNum, columnNum)
                                    }
                                }
                                false
                            }
                            popupMenu.show()
                        }

                        override fun onClickResourceErrorItem(
                            lineNum: Int,
                            columnNum: Int,
                            view: View,
                            resourceFile: File
                        ) {
                            val popupMenu = PopupMenu(this@EditActivity, view)
                            val gotoLine = getString(R.string.goto_line)
                            val selectFile = getString(R.string.select_file)
                            val openDirectoryOfFile = getString(R.string.open_directory_of_file)
                            popupMenu.menu.add(gotoLine)
                            popupMenu.menu.add(selectFile)
                            popupMenu.menu.add(openDirectoryOfFile)
                            popupMenu.setOnMenuItemClickListener {
                                val title = it.title
                                when (title) {
                                    gotoLine -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        viewBinding.codeEditor.setSelection(lineNum, columnNum)
                                    }
                                    selectFile -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        val bundle = Bundle()
                                        val intent = Intent(
                                            this@EditActivity,
                                            FileManagerActivity::class.java
                                        )
                                        bundle.putString("type", "selectFile")
                                        //bundle.putString("path", modClass.getModFile().getAbsolutePath());
                                        intent.putExtra("data", bundle)
                                        viewModel.needCheckAutoSave = false
                                        viewModel.targetFile = resourceFile
                                        startActivityForResult(intent, 1)
                                    }
                                    openDirectoryOfFile -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        editStartViewModel.loadPathLiveData.value =
                                            FileOperator.getSuperDirectory(resourceFile)
                                        viewBinding.editDrawerlayout.openDrawer(GravityCompat.START)
                                    }
                                }
                                false
                            }
                            popupMenu.show()
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
                            val popupMenu = PopupMenu(this@EditActivity, view)
                            val gotoLine = getString(R.string.goto_line)
                            val synchronization = getString(R.string.game_data_and_synchronization)
                            popupMenu.menu.add(gotoLine)
                            popupMenu.menu.add(synchronization)
                            popupMenu.setOnMenuItemClickListener {
                                val title = it.title
                                when (title) {
                                    gotoLine -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        viewBinding.codeEditor.setSelection(lineNum, columnNum)
                                    }
                                    synchronization -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        viewModel.needCheckAutoSave = false
                                        val intent = Intent(
                                            this@EditActivity,
                                            ApplicationListActivity::class.java
                                        )
                                        startActivity(intent)
                                    }
                                }
                                false
                            }
                            popupMenu.show()
                        }

                        override fun onClickSectionNameErrorItem(
                            lineNum: Int,
                            columnNum: Int,
                            view: View,
                            sectionName: String,
                            symbolIndex: Int?,
                            needName: Boolean
                        ) {
                            val popupMenu = PopupMenu(this@EditActivity, view)
                            val gotoLine = getString(R.string.goto_line)
                            val addAdditionalName = getString(R.string.add_additional_name)
                            val removeAdditionalName = getString(R.string.remove_additional_name)
                            popupMenu.menu.add(gotoLine)
                            if (needName) {
                                popupMenu.menu.add(addAdditionalName)
                            } else {
                                popupMenu.menu.add(removeAdditionalName)
                            }
                            popupMenu.setOnMenuItemClickListener {
                                val title = it.title
                                when (title) {
                                    gotoLine -> {
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                        viewBinding.codeEditor.setSelection(lineNum, columnNum)
                                    }
                                    removeAdditionalName -> {
                                        if (symbolIndex != null) {
                                            viewBinding.codeEditor.text.delete(
                                                lineNum,
                                                symbolIndex,
                                                lineNum,
                                                columnNum
                                            )
                                        }
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                    }
                                    addAdditionalName -> {
                                        val labels =
                                            viewBinding.codeEditor.textAnalyzeResult.navigation
                                        var num = 1
                                        labels.forEach {
                                            val type = SourceFile.getSectionType(
                                                it.label.substring(
                                                    1,
                                                    it.label.length - 1
                                                )
                                            )
                                            if (type == sectionName) {
                                                num++
                                            }
                                        }
                                        val name = "_" + num.toString()
                                        viewBinding.codeEditor.setSelection(lineNum, columnNum)
                                        symbolChannel.insertSymbol(name, name.length)
                                        viewBinding.editDrawerlayout.closeDrawer(GravityCompat.END)
                                    }
                                }
                                false
                            }
                            popupMenu.show()
                        }

                        override fun onClickCodeIndexErrorItem(
                            lineNum: Int,
                            view: View,
                            sectionName: String
                        ) {

                        }

                        override fun onShowCompilationResult(code: String): Boolean {
                            return false
                        }


                    })
                } else {
                    openDrawer(GravityCompat.END)
                }
            }
            R.id.show_line_number -> {
                viewBinding.codeEditor.isLineNumberEnabled =
                    !viewBinding.codeEditor.isLineNumberEnabled
                item.isChecked = viewBinding.codeEditor.isLineNumberEnabled
            }
            R.id.word_wrap -> {
                viewBinding.codeEditor.isWordwrap = !viewBinding.codeEditor.isWordwrap
                item.isChecked = viewBinding.codeEditor.isWordwrap
            }
            R.id.convertToTemplate -> {
                viewModel.needCheckAutoSave = false
                val intent = Intent(this@EditActivity, TemplateMakerActivity::class.java)
                val bundle = Bundle()
                bundle.putString(
                    "path",
                    viewModel.openedSourceFileListLiveData.getOpenedSourceFile(viewBinding.tabLayout.selectedTabPosition).file.absolutePath
                )
                intent.putExtra("data", bundle)
                startActivity(intent)
            }
            R.id.text_undo -> {
                viewBinding.codeEditor.undo()
            }
            R.id.text_redo -> {
                viewBinding.codeEditor.redo()
            }
            R.id.search_view -> {
                viewBinding.codeEditor.searcher.stopSearch()
                viewBinding.codeEditor.beginSearchMode()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * 打开侧滑
     */
    fun openDrawer(gravity: Int) {
        viewBinding.editDrawerlayout.openDrawer(gravity)
        viewBinding.codeEditor.hideAutoCompleteWindow()
        viewBinding.codeEditor.hideSoftInput()
    }


    override fun getViewBindingObject(): ActivityEditBinding {
        return ActivityEditBinding.inflate(layoutInflater)
    }
}