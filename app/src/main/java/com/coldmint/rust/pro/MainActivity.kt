package com.coldmint.rust.pro


import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.tool.GlobalMethod
import android.content.pm.PackageInfo
import com.coldmint.rust.pro.tool.AppSettings
import android.content.Intent
import android.content.pm.PackageManager
import com.google.android.material.snackbar.Snackbar
import android.os.*
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.CompressionManager
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.AppUpdateData
import com.coldmint.rust.core.dataBean.template.TemplateInfo
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.UnzipListener
import com.coldmint.rust.core.tool.*
import com.coldmint.rust.core.web.AppUpdate
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.databinding.ActivityMainBinding
import com.coldmint.rust.pro.databinding.HeadLayoutBinding
import com.coldmint.rust.pro.viewmodel.StartViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.zip.ZipEntry


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var oldLanguage: String? = null
    private var first = true
    var tabLayout: TabLayout? = null
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
                AppSettings.getValue(AppSettings.Setting.UseTheCommunityAsTheLaunchPage, true)
            startDestination = if (use) {
                viewBinding.mainButton.hide()
                R.id.community_item
            } else {
                R.id.mod_item
            }
            navController.graph = this
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        viewBinding.navaiagtion.setupWithNavController(navController)
        viewBinding.navaiagtion.addHeaderView(headLayout.root)
        //actionbar动画
        val actionToggle = ActionBarDrawerToggle(
            this,
            viewBinding.drawerlayout,
            viewBinding.toolbar,
            R.string.app_name,
            R.string.app_name
        )
        viewBinding.drawerlayout.addDrawerListener(actionToggle)
        actionToggle.syncState()
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
                    AppSettings.forceSetValue(AppSettings.Setting.UpdateData, gson.toJson(data))
                    ifNeedShowUpdate(data)
                } else {
                    Snackbar.make(viewBinding.mainButton, t.message, Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(e: Exception) {
                val updateData = AppSettings.getValue(AppSettings.Setting.UpdateData, "")
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
                AppSettings.getValue(AppSettings.Setting.CheckBetaUpdate, false)
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
                val materialAlertDialogBuilder =
                    MaterialAlertDialogBuilder(this).setTitle(data.title).setMessage(data.content)
                if (data.forced) {
                    //禁用点击空白关闭
                    materialAlertDialogBuilder.setCancelable(false)
                } else {
                    materialAlertDialogBuilder.setNegativeButton(
                        R.string.dialog_cancel
                    ) { i, i2 ->
                    }
                }
                materialAlertDialogBuilder.setPositiveButton(
                    R.string.downlod
                ) { i, i2 ->
                    AppOperator.useBrowserAccessWebPage(this, data.link)
                }
                materialAlertDialogBuilder.show()
            }
        }
    }


    /**
     * 显示游戏配置对话框
     */
    fun showGameConfiguredDialog() {
        if (!AppSettings.getValue(AppSettings.Setting.SetGameStorage, false)) {
            try {
                val packageInfo = packageManager.getPackageInfo(
                    GlobalMethod.DEFAULT_GAME_PACKAGE,
                    PackageManager.GET_UNINSTALLED_PACKAGES
                )
                val versionCode = packageInfo.versionCode
                //如果在1.15 p3及以上 (159)
                if (versionCode >= 159) {
                    MaterialAlertDialogBuilder(this).setTitle(R.string.game_configured)
                        .setMessage(R.string.unable_to_detect)
                        .setPositiveButton(R.string.show_details) { i, i2 ->
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    GameCheckActivity::class.java
                                )
                            )
                        }.setNeutralButton(R.string.no_longer_prompt) { i, i2 ->
                            AppSettings.setValue(AppSettings.Setting.SetGameStorage, true)
                        }.setNeutralButton(R.string.dialog_cancel) { i, i2 ->
                        }.setCancelable(false).show()
                } else {
                    AppSettings.setValue(AppSettings.Setting.SetGameStorage, true)
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
            val newLanguage = AppSettings.getValue(AppSettings.Setting.AppLanguage, "en")
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
//        val help = menu.findItem(R.id.help)
        //管理可见性
        dataBase.isVisible = isActive
        template.isVisible = isActive
//        help.isVisible = isActive
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
                false
            }

            template.setOnMenuItemClickListener {
                viewBinding.mainButton.postOnAnimationDelayed({
                    viewBinding.tabLayout.isVisible = false
                    viewBinding.mainButton.show()
                }, hideViewDelay)
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
                        val intent = Intent(this, CreationWizardActivity::class.java)
                        intent.putExtra("type", "template")
                        startActivity(intent)
                    }
                    else -> {
                    }
                }
            }
        }

        mod.setOnMenuItemClickListener {
            GlobalMethod.requestStoragePermissions(this) {
                if (it) {
                    viewBinding.mainButton.postOnAnimationDelayed({
                        viewBinding.tabLayout.isVisible = true
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
                viewBinding.tabLayout.isVisible = true
                viewBinding.mainButton.hide()
            }, hideViewDelay)
            false
        }

        menu.findItem(R.id.startGame).setOnMenuItemClickListener {
            val packName = AppSettings.getValue(
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

        //激活暂时不可用
        val longTime = AppSettings.getValue(
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
                    val modDirectory = AppSettings.getValue(AppSettings.Setting.ModFolder, "")
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
                        AppSettings.getValue(
                            AppSettings.Setting.TemplateDirectory,
                            this.filesDir.absolutePath + "/template/"
                        ) + LocalTemplatePackage.getAbsoluteFileName(from)
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
                val newInfoData = compressionManager.readEntry(formFile, LocalTemplatePackage.INFONAME)
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
                    val templateClass = LocalTemplatePackage(templateDirectory)
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
                            MaterialAlertDialogBuilder(this).setTitle(oldInfo.name).setMessage(
                                String.format(
                                    getString(R.string.covers_the_import),
                                    newInfo.versionName, oldInfo.versionName
                                )
                            ).setPositiveButton(R.string.dialog_ok) { i, i2 ->
                                FileOperator.delete_files(templateDirectory)
                                importTemplate(formFile, templateDirectory)
                            }.setNegativeButton(R.string.dialog_cancel) { i, i2 ->
                            }.show()
                        }
                        return@Runnable
                    } else {
                        //同等版本，不做处理（覆盖安装）
                    }
                }
            } else {
                //常规导入
                val newInfo = compressionManager.readEntry(formFile, LocalTemplatePackage.INFONAME)
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
            MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_close).setMessage(
                String.format(
                    getString(R.string.exit_tip),
                    getString(R.string.app_name)
                )
            ).setPositiveButton(R.string.dialog_ok) { i, i2 ->
                finish()
            }.setNegativeButton(R.string.dialog_cancel) { i, i2 ->
            }.show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.baseFragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search) {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        return false
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityMainBinding {
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
                    .apply(GlobalMethod.getRequestOptions(true, !it.data.activation))
                    .into(headLayout.imageView)
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
                CoreDialog(this).setTitle(R.string.login).setMessage(R.string.login_tip)
                    .setPositiveButton(R.string.login) {
                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                    }.setNegativeButton(R.string.dialog_close) {
                        finish()
                    }.setCancelable(false).show()
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
                CoreDialog(this).setTitle(R.string.login).setMessage(it)
                    .setCancelable(false).setPositiveButton(R.string.login) {
                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                    }.setNegativeButton(R.string.close) {
                        finish()
                    }.show()
            }
        }

    }


    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            oldLanguage = AppSettings.getValue(AppSettings.Setting.AppLanguage, "en")
            useToolbarSetSupportActionBar()
            initNav()
            observeStartViewModel()
            checkAppUpdate()
        } else {
            startViewModel.initAllData()
        }
    }


}