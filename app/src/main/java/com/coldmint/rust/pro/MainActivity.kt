package com.coldmint.rust.pro


import android.Manifest
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.core.iflynote.SquareBracketData
import android.content.pm.PackageInfo
import com.coldmint.rust.pro.tool.AppSettings
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import android.content.Intent
import android.content.pm.PackageManager
import com.google.android.material.snackbar.Snackbar
import android.widget.Toast
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.bumptech.glide.Glide
import com.coldmint.rust.core.CompressionManager
import com.coldmint.rust.core.TemplatePackage
import com.coldmint.rust.core.dataBean.AppUpdateData
import com.coldmint.rust.core.dataBean.iflynote.NoteData
import com.coldmint.rust.core.dataBean.template.TemplateInfo
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.iflynote.IFlyNoteAPi
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.LineParserEvent
import com.coldmint.rust.core.interfaces.UnzipListener
import com.coldmint.rust.core.tool.*
import com.coldmint.rust.core.web.AppUpdate
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.databinding.ActivityMainBinding
import com.coldmint.rust.pro.databinding.HeadLayoutBinding
import com.coldmint.rust.pro.viewmodel.StartViewModel
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.ExplainReasonCallback
import com.permissionx.guolindev.callback.ForwardToSettingsCallback
import com.permissionx.guolindev.callback.RequestCallback
import com.permissionx.guolindev.dialog.RationaleDialog
import com.permissionx.guolindev.request.ExplainScope
import com.permissionx.guolindev.request.ForwardScope
import org.json.JSONObject
import java.io.File
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.zip.ZipEntry


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var oldLanguage: String? = null
    private var first = true
    var tabLayout: TabLayout? = null
    lateinit var searchView: SearchView
    val headLayout by lazy {
        HeadLayoutBinding.inflate(layoutInflater)
    }

    val startViewModel by lazy {
        ViewModelProvider(this).get(StartViewModel::class.java)
    }

    companion object {
        //仓库和社区碎片链接TabLayout间隔
        const val linkInterval: Long = 195
        const val hideViewDelay: Long = 150
    }

    /**
     * 将Toolbar设置为ActionBar
     */
    fun useToolbarSetSupportActionBar() {
        tabLayout = viewBinding.tabLayout
    }

    /**
     * 初始化导航
     */
    fun initNav() {
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.community_item, R.id.mod_item, R.id.database_item, R.id.template_item),
            viewBinding.drawerlayout
        )
        val navController = findNavController(R.id.baseFragment)
        navController.navInflater.inflate(R.navigation.main_nav).apply {
            val use =
                appSettings.getValue(AppSettings.Setting.UseTheCommunityAsTheLaunchPage, true)
            startDestination = if (use) {
                viewBinding.mainButton.hide()
                R.id.community_item
            } else {
                viewBinding.toolbar.postDelayed({
                    searchView.isVisible = false
                }, linkInterval)
                R.id.mod_item
            }
            navController.graph = this
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        viewBinding.navaiagtion.setupWithNavController(navController)
        viewBinding.navaiagtion.addHeaderView(headLayout.root)
    }

    /**
     * 检查App更新
     */
    fun checkAppUpdate() {
        AppUpdate().getUpdate(object : ApiCallBack<AppUpdateData> {
            override fun onResponse(t: AppUpdateData) {
                val data = t.data
                if (t.code == ServerConfiguration.Success_Code && data != null) {
                    val gson = Gson()
                    //同步离线的更新对话框
                    appSettings.forceSetValue(AppSettings.Setting.UpdateData, gson.toJson(data))
                    ifNeedShowUpdate(data)
                } else {
                    Snackbar.make(viewBinding.mainButton, t.message, Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(e: Exception) {
                val updateData = appSettings.getValue(AppSettings.Setting.UpdateData, "")
                if (updateData.isNotBlank()) {
                    val gson = Gson()
                    val updateDataObj = gson.fromJson(updateData, AppUpdateData.Data::class.java)
                    ifNeedShowUpdate(updateDataObj)
                }
            }
        })
    }

    /**
     * 检查如果需要显示更新对话框
     * @param data Data
     */
    fun ifNeedShowUpdate(data: AppUpdateData.Data) {
        val executorService = Executors.newSingleThreadExecutor()
        executorService.submit {
            //检查更新
            val key = "应用更新"
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            val checkBetaUpdate =
                appSettings.getValue(AppSettings.Setting.CheckBetaUpdate, false)
            var needShowDialog = false
            //如果自身是Beta版，则强制更新Beta版本
            if (BuildConfig.DEBUG && packageInfo.versionName.contains("Beta")) {
                data.forced = true
                //版本名不一致，是Beta模式
                if (packageInfo.versionName != data.versionName) {
                    needShowDialog = true
                    Log.d(key, "是测试模式,版本名称不一致")
                } else {
                    Log.d(key, "是测试模式，并且是Beta版本")
                }
            } else if (checkBetaUpdate) {
                //版本名不一致
                if (packageInfo.versionName != data.versionName) {
                    needShowDialog = true
                    Log.d(key, "开启了检查Beta版本,版本名称不一致")
                } else {
                    Log.d(key, "开启了检查Beta版本,无需更新")
                }
            } else {
                //版本号不一致
                if (packageInfo.versionCode != data.versionNumber) {
                    needShowDialog = true
                    Log.d(key, "正式打包模式,版本号不一致")
                } else {
                    Log.d(key, "正式打包模式,版本号一致无需更新")
                }
            }

            if (!needShowDialog) {
                return@submit
            }
            //显示对话框
            runOnUiThread {
                val materialDialog = MaterialDialog(this@MainActivity, BottomSheet())
                materialDialog.title(text = data.title).message(text = data.content)
                if (data.forced) {
                    materialDialog.noAutoDismiss()
                    //禁用点击空白关闭
                    materialDialog.cancelable(false)
                } else {
                    materialDialog.negativeButton(
                        R.string.dialog_cancel
                    )
                }
                materialDialog.positiveButton(
                    R.string.downlod
                ) {
                    AppOperator.useBrowserAccessWebPage(this, data.link)
                }
                materialDialog.show()
            }
        }
    }


    /**
     * 显示游戏配置对话框
     */
    fun showGameConfiguredDialog() {
        if (!appSettings.getValue(AppSettings.Setting.SetGameStorage, false)) {
            try {
                val packageInfo = packageManager.getPackageInfo(
                    GlobalMethod.DEFAULT_GAME_PACKAGE,
                    PackageManager.GET_UNINSTALLED_PACKAGES
                )
                val versionCode = packageInfo.versionCode
                //如果在1.15 p3及以上 (159)
                if (versionCode >= 159) {
                    MaterialDialog(this).show {
                        title(R.string.game_configured).message(R.string.unable_to_detect)
                            .positiveButton(R.string.show_details)
                            .positiveButton {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        GameCheckActivity::class.java
                                    )
                                )
                            }.neutralButton(R.string.no_longer_prompt).neutralButton {
                                appSettings.setValue(AppSettings.Setting.SetGameStorage, true)
                            }.negativeButton(R.string.dialog_cancel).cancelable(false)
                    }
                } else {
                    appSettings.setValue(AppSettings.Setting.SetGameStorage, true)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (first) {
            first = false
        } else {
            val newLanguage = appSettings.getValue(AppSettings.Setting.AppLanguage, "en")
            if (oldLanguage != newLanguage) {
                recreate()
                return
            }
            val error = startViewModel.signatureErrorLiveData.value ?: true
            if (!error) {
                startViewModel.verifyingUserInfo()
            }
        }
    }

    /**
     * 初始化导航菜单
     */
    fun initNavigationMenu(isActive: Boolean = GlobalMethod.isActive) {
        val menu = viewBinding.navaiagtion.menu
        val dataBase = menu.findItem(R.id.database_item)
        val template = menu.findItem(R.id.template_item)
        val codeTable = menu.findItem(R.id.code_table)
        val mod = menu.findItem(R.id.mod_item)
        val community = menu.findItem(R.id.community_item)
        val help = menu.findItem(R.id.help)
        //管理可见性
        dataBase.isVisible = isActive
        template.isVisible = isActive
        help.isVisible = isActive
        codeTable.isVisible = isActive
        if (mod.isChecked) {
            viewBinding.mainButton.isVisible = isActive
        }
        if (isActive) {
            //数据库
            dataBase.setOnMenuItemClickListener {
                viewBinding.mainButton.postOnAnimationDelayed({
                    viewBinding.tabLayout.isVisible = false
                    viewBinding.mainButton.hide()
                }, hideViewDelay)
                searchView.isVisible = false
                false
            }

            template.setOnMenuItemClickListener {
                viewBinding.mainButton.postOnAnimationDelayed({
                    viewBinding.tabLayout.isVisible = false
                    viewBinding.mainButton.show()
                }, hideViewDelay)
                searchView.isVisible = false
                false
            }

            codeTable.setOnMenuItemClickListener {
                startActivity(Intent(this@MainActivity, CodeTableActivity::class.java))
                false
            }

            viewBinding.mainButton.setOnClickListener {
                val item = viewBinding.navaiagtion.checkedItem.toString()
                val warehouseItem = getString(R.string.warehouse)
                //final String database_item = getString(R.string.menu_title3);
                val templateItem = getString(R.string.template_title)
                when (item) {
                    warehouseItem -> {
                        val intent = Intent(this, CreationWizardActivity::class.java)
                        intent.putExtra("type", "mod")
                        startActivity(intent)
                    }
                    templateItem -> {
                        val popupMenu = PopupMenu(this@MainActivity, viewBinding.mainButton)
                        popupMenu.menu.add(R.string.create_template)
                        popupMenu.menu.add(R.string.import_template)
                        popupMenu.setOnMenuItemClickListener { item ->
                            val title = item.title.toString()
                            if (title == getString(R.string.create_template)) {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        CreateTemplateActivity::class.java
                                    )
                                )
                            } else if (title == getString(R.string.import_template)) {
                                val startIntent =
                                    Intent(this@MainActivity, FileManagerActivity::class.java)
                                val fileBundle = Bundle()
                                fileBundle.putString("type", "selectFile")
                                startIntent.putExtra("data", fileBundle)
                                startActivityForResult(startIntent, 2)
                            }
                            true
                        }
                        popupMenu.show()
                    }
                    else -> {
                        Toast.makeText(this@MainActivity, "请设置事件", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        mod.setOnMenuItemClickListener {
            GlobalMethod.requestStoragePermissions(this) {
                if (it) {
                    viewBinding.mainButton.postOnAnimationDelayed({
                        viewBinding.tabLayout.isVisible = true
                        searchView.isVisible = false
                        if (isActive) {
                            viewBinding.mainButton.show()
                        }
                    }, hideViewDelay)
                }
            }
            false
        }

        community.setOnMenuItemClickListener {
            viewBinding.mainButton.postOnAnimationDelayed({
                searchView.isVisible = true
                viewBinding.tabLayout.isVisible = true
                viewBinding.mainButton.hide()
            }, hideViewDelay)
            false
        }

        menu.findItem(R.id.startGame).setOnMenuItemClickListener {
            val packName = appSettings.getValue(
                AppSettings.Setting.GamePackage,
                GlobalMethod.DEFAULT_GAME_PACKAGE
            )
            if (AppOperator.isAppInstalled(this, packName)) {
                AppOperator.openApp(this, packName)
            } else {
                viewBinding.drawerlayout.closeDrawer(GravityCompat.START)
                Snackbar.make(
                    viewBinding.mainButton,
                    R.string.no_game_installed,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            false
        }

        menu.findItem(R.id.about).setOnMenuItemClickListener {
            startActivity(Intent(this@MainActivity, AboutActivity::class.java))
            false
        }
        menu.findItem(R.id.set_up).setOnMenuItemClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            false
        }

        help.setOnMenuItemClickListener {
            AppOperator.useBrowserAccessWebPage(
                this,
                "https://www.kancloud.cn/coldmint/rust_assistant"
            )
            false
        }

        menu.findItem(R.id.donation).setOnMenuItemClickListener {
            AppOperator.useBrowserAccessWebPage(this@MainActivity, "https://afdian.net/@coldmint")
            false
        }
        //激活暂时不可用
        val longTime = appSettings.getValue(
            AppSettings.Setting.ExpirationTime,
            0.toLong()
        )
        val time = ServerConfiguration.toStringTime(
            longTime
        )
        val activationItem = menu.findItem(R.id.activation_item)
        if (time == ServerConfiguration.ForeverTime) {
            activationItem.isVisible = false
        } else {
            activationItem.isVisible = true
            if (isActive) {
                activationItem.title = getText(R.string.renewal)
            }
            activationItem.setOnMenuItemClickListener {
                startActivity(Intent(this, ActivateActivity::class.java))
                false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val from = File(data!!.getStringExtra("File"))
            val type = FileOperator.getFileType(from)
            if (requestCode == 1) {
                if ("rwmod" == type || "zip" == type) {
                    val modDirectory = appSettings.getValue(AppSettings.Setting.ModFolder, "")
                    val to = File(modDirectory + from.name)
                    if (FileOperator.copyFile(from, to)) {
                        Snackbar.make(
                            viewBinding.mainButton,
                            String.format(getString(R.string.import_complete), from.name),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        Snackbar.make(
                            viewBinding.mainButton,
                            String.format(getString(R.string.import_failed), from.name),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Snackbar.make(
                        viewBinding.mainButton,
                        R.string.bad_file_type,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else if (requestCode == 2) {
                if ("rp" == type) {
                    val outputFolder = File(
                        appSettings.getValue(
                            AppSettings.Setting.TemplateDirectory,
                            this.filesDir.absolutePath + "/template/"
                        ) + TemplatePackage.getAbsoluteFileName(from)
                    )
                    importTemplate(from, outputFolder)
                } else {
                    Snackbar.make(
                        viewBinding.mainButton,
                        R.string.bad_file_type,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * 初始化搜索项目事件
     */
    private fun initSearchItem(newSearchView: SearchView) {
        val navigationIcon = viewBinding.toolbar.navigationIcon
        var oldTile: CharSequence = viewBinding.toolbar.title
        val hideView: (collapsed: Boolean) -> Unit = {
            if (it) {
                newSearchView.onActionViewCollapsed()
            }
            viewBinding.toolbar.navigationIcon = navigationIcon
            viewBinding.toolbar.title = oldTile
        }
        newSearchView.queryHint = getString(R.string.search_hint)
        newSearchView.setOnCloseListener {
            hideView.invoke(false)
            return@setOnCloseListener false
        }
        newSearchView.setOnSearchClickListener {
            oldTile = viewBinding.toolbar.title
            viewBinding.toolbar.navigationIcon = null
            viewBinding.toolbar.title = getString(R.string.search)
        }

        newSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideView.invoke(true)
                val key = query
                if (key != null) {
                    val goIntent = Intent(this@MainActivity, SearchActivity::class.java)
                    goIntent.putExtra("key", key)
                    startActivity(goIntent)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        searchView = newSearchView
    }

    /**
     * 导入模板
     * @param formFile File 文件
     * @param templateDirectory File 模板文件夹
     */
    fun importTemplate(formFile: File, templateDirectory: File) {
        val handler = Handler(Looper.getMainLooper())
        Thread(Runnable {
            //如果建立缓存完成，并且模板文件存在
            val compressionManager = CompressionManager.instance
            if (templateDirectory.exists()) {
                val gson = Gson()
                val newInfoData = compressionManager.readEntry(formFile, TemplatePackage.INFONAME)
                if (newInfoData == null) {
                    handler.post {
                        Snackbar.make(
                            viewBinding.mainButton,
                            getString(R.string.import_failed2),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    return@Runnable
                } else {
                    val newInfo = gson.fromJson(newInfoData, TemplateInfo::class.java)
                    val templateClass = TemplatePackage(templateDirectory)
                    val oldInfo = templateClass.getInfo()
                    if (oldInfo == null) {
                        handler.post {
                            Snackbar.make(
                                viewBinding.mainButton,
                                R.string.import_failed2,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        return@Runnable
                    }
                    val thisAppVersion =
                        AppOperator.getAppVersionNum(this, this.packageName)
                    if (newInfo.versionNum > thisAppVersion) {
                        handler.post {
                            Snackbar.make(
                                viewBinding.mainButton,
                                String.format(
                                    getString(R.string.app_version_error),
                                    formFile.name
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
                                    importTemplate(formFile, templateDirectory)
                                }.negativeButton(R.string.dialog_cancel)
                            }
                        }
                        return@Runnable
                    } else {
                        //同等版本，不做处理（覆盖安装）
                    }
                }
            } else {
                //常规导入
                val newInfo = compressionManager.readEntry(formFile, TemplatePackage.INFONAME)
                if (newInfo == null) {
                    handler.post {
                        Snackbar.make(
                            viewBinding.mainButton,
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
                            Snackbar.make(
                                viewBinding.mainButton,
                                String.format(
                                    getString(R.string.app_version_error),
                                    formFile.name
                                ), Snackbar.LENGTH_LONG
                            ).show()
                        }
                        return@Runnable
                    }
                }
            }

            compressionManager.unzip(
                formFile,
                templateDirectory,
                object : UnzipListener {
                    override fun whenUnzipFile(zipEntry: ZipEntry, file: File): Boolean {
                        return true
                    }

                    override fun whenUnzipFolder(zipEntry: ZipEntry, folder: File): Boolean {
                        return true
                    }

                    override fun whenUnzipComplete(result: Boolean) {
                        handler.post {
                            handler.post {
                                Snackbar.make(
                                    viewBinding.mainButton,
                                    String.format(
                                        getString(R.string.import_complete),
                                        formFile.name
                                    ), Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }


                })
        }).start()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            MaterialDialog(this).show {
                title(R.string.dialog_close).message(
                    text = String.format(
                        getString(R.string.exit_tip),
                        getString(R.string.app_name)
                    )
                ).cancelable(false).positiveButton(R.string.dialog_ok) {
                    finish()
                }.negativeButton(R.string.dialog_cancel)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        initSearchItem(searchView)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.baseFragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun getViewBindingObject(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    /**
     * 观察启动视图
     */
    fun observeStartViewModel() {
        startViewModel.userLiveData.observe(this) {
            headLayout.nameView.text = it.data.userName
            headLayout.emailView.text = it.data.email
            val headIcon = it.data.headIcon
            if (headIcon != null) {
                Glide.with(this).load(ServerConfiguration.getRealLink(headIcon))
                    .apply(GlobalMethod.getRequestOptions(true)).into(headLayout.imageView)
            }
            val account = it.data.account
            headLayout.root.setOnClickListener {
                val opIntent = Intent(this, UserHomePageActivity::class.java)
                opIntent.putExtra("userId", account)
                startActivity(opIntent)
            }
        }

        startViewModel.needLoginLiveData.observe(this) {
            if (it) {
                MaterialDialog(this, BottomSheet()).show {
                    title(R.string.login).message(R.string.login_tip).cancelable(false)
                        .positiveButton(R.string.login) {
                            startActivity(
                                Intent(
                                    context,
                                    LoginActivity::class.java
                                )
                            )
                        }.negativeButton(R.string.dialog_close).negativeButton {
                            finish()
                        }
                }
            } else {
                showGameConfiguredDialog()
            }
        }

        startViewModel.dataSetMsgLiveData.observe(this) {
            if (it.isNotBlank()) {
                Snackbar.make(viewBinding.mainButton, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        startViewModel.signatureErrorLiveData.observe(this) {
            if (it) {
                //显示签名错误
                MaterialDialog(this).show {
                    title(R.string.sign_error).cancelable(false)
                        .message(R.string.sign_error_message)
                        .positiveButton(R.string.dialog_close) {
                            finish()
                        }
                }
            }
        }

        startViewModel.isActivationLiveData.observe(this) {
            GlobalMethod.isActive = it
            initNavigationMenu(it)
        }

        startViewModel.verifyErrorMsgLiveData.observe(this) {
            if (it.isNotBlank()) {
                MaterialDialog(this).show {
                    title(R.string.login).message(text = it)
                        .cancelable(false).positiveButton(R.string.login) {
                            startActivity(
                                Intent(
                                    context,
                                    LoginActivity::class.java
                                )
                            )
                        }.negativeButton(R.string.close) {
                            finish()
                        }
                }
            }
        }

    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            oldLanguage = appSettings.getValue(AppSettings.Setting.AppLanguage, "en")
            useToolbarSetSupportActionBar()
            initNav()
            observeStartViewModel()
            checkAppUpdate()
        } else {
            installSplashScreen()
            startViewModel.initAllData()
        }
    }


}