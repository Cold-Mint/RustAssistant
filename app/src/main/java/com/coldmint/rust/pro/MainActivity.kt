package com.coldmint.rust.pro


import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.CompressionManager
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.AppUpdateData
import com.coldmint.rust.core.dataBean.template.TemplateInfo
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.UnzipListener
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.AppUpdate
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityMainBinding
import com.coldmint.rust.pro.databinding.HeadLayoutBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.EventRecord
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.viewmodel.StartViewModel
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.gyf.immersionbar.ImmersionBar
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.zip.ZipEntry

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var oldLanguage: String? = null
    private var first = true
    private var oldDynamicColor = false
    private val headLayout by lazy {
        HeadLayoutBinding.inflate(layoutInflater)
    }
    val startViewModel by lazy {
        ViewModelProvider(this)[StartViewModel::class.java]
    }

    companion object {
        //仓库和社区碎片链接TabLayout间隔
        const val linkInterval: Long = 195
        const val hideViewDelay: Long = 150
    }

    /**
     * 将Toolbar设置为ActionBar
     */
    private fun useToolbarSetSupportActionBar() {
    }

    /**
     * 初始化导航
     */
    private fun initNav() {
        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.community_item, R.id.mod_item, R.id.database_item, R.id.template_item),
                viewBinding.drawerlayout
        )
        val navController = findNavController(R.id.baseFragment)
        navController.navInflater.inflate(R.navigation.main_nav).apply {
            val use =
                    AppSettings.getValue(AppSettings.Setting.UseTheCommunityAsTheLaunchPage, true)
            this.setStartDestination(
                    if (use) {
                        viewBinding.mainButton.hide()
                        R.id.community_item
                    } else {
                        R.id.mod_item
                    }
            )
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
    private fun checkAppUpdate() {
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
        val key = "应用更新"
        if (ServerConfiguration.isTestServer()) {
            LogCat.w(key, "当前为本地测试服务器，已禁用更新检查。")
            return
        }
        val executorService = Executors.newSingleThreadExecutor()
        executorService.submit {
            //检查更新
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            val checkBetaUpdate =
                    AppSettings.getValue(AppSettings.Setting.CheckBetaUpdate, false)
            var needShowDialog = false
            if (data.versionNumber > packageInfo.versionCode) {
                if (data.isBeta) {
                    if (checkBetaUpdate) {
                        needShowDialog = true
                    }
                } else {
                    needShowDialog = true
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
                    ) { _, _ ->
                    }
                }
                materialAlertDialogBuilder.setPositiveButton(
                        R.string.downlod
                ) { _, _ ->
                    AppOperator.useBrowserAccessWebPage(this, data.link)
                }
                materialAlertDialogBuilder.show()
            }
        }
    }


    /**
     * 显示游戏配置对话框
     */
    private fun showGameConfiguredDialog() {
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
                            .setPositiveButton(R.string.show_details) { _, _ ->
                                startActivity(
                                        Intent(
                                                this@MainActivity,
                                                GameCheckActivity::class.java
                                        )
                                )
                            }.setNeutralButton(R.string.no_longer_prompt) { _, _ ->
                                AppSettings.setValue(AppSettings.Setting.SetGameStorage, true)
                            }.setNeutralButton(R.string.dialog_cancel) { _, _ ->
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
            val newDynamicColor = AppSettings.getValue(
                    AppSettings.Setting.DynamicColor,
                    DynamicColors.isDynamicColorAvailable()
            )
            if (oldDynamicColor != newDynamicColor) {
                recreate()
                return
            }
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
    private fun initNavigationMenu(isActive: Boolean = GlobalMethod.isActive) {
        val menu = viewBinding.navaiagtion.menu
        val dataBase = menu.findItem(R.id.database_item)
        val template = menu.findItem(R.id.template_item)
        val codeTable = menu.findItem(R.id.code_table)
        val mod = menu.findItem(R.id.mod_item)
        val community = menu.findItem(R.id.community_item)
//        val group = menu.findItem(R.id.user_group)
//        group.setOnMenuItemClickListener {
//            viewBinding.drawerlayout.closeDrawer((GravityCompat.START))
//            val userGroupFragment = UserGroupFragment()
//            userGroupFragment.show(supportFragmentManager, "userGroup")
//            false
//        }
        val gitHub = menu.findItem(R.id.github)
        gitHub.setOnMenuItemClickListener {
            AppOperator.useBrowserAccessWebPage(this, "https://github.com/Cold-Mint/RustAssistant")
            false
        }
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
//                    viewBinding.tabLayout.isVisible = false
                    viewBinding.mainButton.hide()
                }, hideViewDelay)
                false
            }

            template.setOnMenuItemClickListener {
                viewBinding.mainButton.postOnAnimationDelayed({
//                    viewBinding.tabLayout.isVisible = true
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
//                        viewBinding.tabLayout.isVisible = true
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
//                viewBinding.tabLayout.isVisible = true
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

        menu.findItem(R.id.rust_api).setOnMenuItemClickListener {
            val thisIntent = Intent(this, BrowserActivity::class.java)
            thisIntent.putExtra("link", "https://git.coldmint.top/")
            thisIntent.putExtra("javaScriptEnabled", true)
            startActivity(thisIntent)
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
        val loginStatus = AppSettings.getValue(
                AppSettings.Setting.LoginStatus,
                false
        )
        val activationItem = menu.findItem(R.id.activation_item)
        if (loginStatus) {
            val time = ServerConfiguration.toStringTime(
                    longTime
            )
            if (time == ServerConfiguration.ForeverTime) {
                activationItem.isVisible = false
            } else {
                activationItem.isVisible = true
                if (isActive) {
                    activationItem.title = getText(R.string.renewal)
                } else {
                    activationItem.title = getText(R.string.activate)
                }
                activationItem.setOnMenuItemClickListener {
                    startActivity(Intent(this, ActivateActivity::class.java))
                    false
                }
            }
        } else {
            activationItem.isVisible = false
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
    private fun importTemplate(formFile: File, templateDirectory: File) {
        val handler = Handler(Looper.getMainLooper())
        Thread(Runnable {
            //如果建立缓存完成，并且模板文件存在
            val compressionManager = CompressionManager.instance
            if (templateDirectory.exists()) {
                val gson = Gson()
                val newInfoData =
                        compressionManager.readEntry(formFile, LocalTemplatePackage.INFONAME)
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
                            ).setPositiveButton(R.string.dialog_ok) { _, _ ->
                                FileOperator.delete_files(templateDirectory)
                                importTemplate(formFile, templateDirectory)
                            }.setNegativeButton(R.string.dialog_cancel) { _, _ ->
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


    override fun onBackPressed() {
        val navController = findNavController(R.id.baseFragment)
        //判断是否在第一个导航 社区或者仓库
        if (navController.currentDestination?.id == navController.graph.startDestinationId) {
            MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_close).setMessage(
                    String.format(
                            getString(R.string.exit_tip),
                            getString(R.string.app_name)
                    )
            ).setPositiveButton(R.string.dialog_ok) { _, _ ->
                super.onBackPressed()
            }.setNegativeButton(R.string.dialog_cancel) { _, _ ->
            }.show()
        } else {
            super.onBackPressed()
        }
    }
    /*
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

/*不知道干什么的代码
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.baseFragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
*/

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
    private fun observeStartViewModel() {
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
            headLayout.imageView.setOnClickListener {
                val opIntent = Intent(this, UserHomePageActivity::class.java)
                opIntent.putExtra("userId", account)
                startActivity(opIntent)
            }
            EventRecord.setUserId(account)
        }

        startViewModel.needLoginLiveData.observe(this) {
            if (it) {
                headLayout.imageView.setImageResource(R.drawable.head_icon)
                headLayout.nameView.text = getString(R.string.click_profile_picture_login)
                headLayout.emailView.text = ""
                headLayout.imageView.setOnClickListener {
                    startActivity(
                            Intent(
                                    this,
                                    LoginActivity::class.java
                            )
                    )
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
                CoreDialog(this).setTitle(R.string.sign_error)
                        .setMessage(R.string.sign_error_message).setCancelable(false)
                        .setPositiveButton(R.string.dialog_close) {
                            finish()
                        }
            }
        }

        startViewModel.isActivationLiveData.observe(this) {
//            GlobalMethod.isActive = it
            initNavigationMenu(GlobalMethod.isActive)
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
            oldDynamicColor = AppSettings.getValue(
                    AppSettings.Setting.DynamicColor,
                    DynamicColors.isDynamicColorAvailable()
            )
//            useToolbarSetSupportActionBar()
            initNav()
            observeStartViewModel()
            //偏移fab
            if (ImmersionBar.hasNavigationBar(this)) {
                val layoutParams =
                        viewBinding.mainButton.layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.setMargins(
                        GlobalMethod.dp2px(16),
                        GlobalMethod.dp2px(16),
                        GlobalMethod.dp2px(16),
                        ImmersionBar.getNavigationBarHeight(this) + GlobalMethod.dp2px(16)
                )
                DebugHelper.printLog("导航适配", "已调整fab按钮的位置。")
            }
            checkAppUpdate()
        } else {
            startViewModel.initAllData()
        }
    }


}