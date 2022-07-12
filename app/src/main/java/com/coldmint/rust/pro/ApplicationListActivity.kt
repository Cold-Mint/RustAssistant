package com.coldmint.rust.pro

import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Looper
import android.content.pm.PackageInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import com.coldmint.rust.pro.adapters.ApplicationListAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityApplicationListBinding
import java.util.ArrayList

class ApplicationListActivity : BaseActivity<ActivityApplicationListBinding>() {
    private var loading = true

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_application, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.load_system_application -> {
                val data = !item.isChecked
                if (!loading) {
                    loadList(null, data)
                    item.isChecked = data
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 加载列表
     * @param appName String? app名称
     * @param loadsSystemProgram Boolean 是否加载系统程序
     */
    private fun loadList(appName: String?, loadsSystemProgram: Boolean) {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            loading = true
            //加载视图之前
            handler.post {
                viewBinding.appListView.isVisible = false
                viewBinding.tipView.isVisible = true
                viewBinding.progressBar.isVisible = true
            }
            val packageInfoList = getApplications(appName, loadsSystemProgram)
            if (packageInfoList.isNotEmpty()) {
                val adapter = ApplicationListAdapter(this, packageInfoList.toMutableList())
                handler.post {
                    viewBinding.appListView.isVisible = true
                    viewBinding.tipView.isVisible = false
                    viewBinding.progressBar.isVisible = false
                    viewBinding.appListView.adapter = adapter
                }
            }
            loading = false
        }.start()
    }

    /**
     * 获取app列表
     *
     * @param appName            app名称，可选，传入null不筛选
     * @param loadsSystemProgram 加载系统程序
     * @return app信息列表
     */
    private fun getApplications(appName: String?, loadsSystemProgram: Boolean): List<PackageInfo> {
        val packageInfoList: MutableList<PackageInfo> = ArrayList()
        val allApps = packageManager.getInstalledApplications(0)
        val packageManager = packageManager
        for (info in allApps) {
            try {
                val packageInfo = packageManager.getPackageInfo(info.packageName, 0)
                val thisAppName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                if (appName == null) {
                    if (loadsSystemProgram) {
                        packageInfoList.add(packageInfo)
                    } else {
                        if (!isSystemApplication(info.flags)) {
                            packageInfoList.add(packageInfo)
                        }
                    }
                } else {
                    if (thisAppName.contains(appName)) {
                        if (loadsSystemProgram) {
                            packageInfoList.add(packageInfo)
                        } else {
                            if (!isSystemApplication(info.flags)) {
                                packageInfoList.add(packageInfo)
                            }
                        }
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        return packageInfoList
    }

    /**
     * 是否为系统应用
     *
     * @param flags 应用标志
     * @return
     */
    private fun isSystemApplication(flags: Int): Boolean {
        return flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
    }

    /**
     * 加载布局管理器到列表
     */
    private fun loadLayoutManagerToList() {
//        viewBinding.appListView.addItemDecoration(
//            Decoration(
//                this@ApplicationListActivity,
//                LinearLayoutManager.VERTICAL
//            )
//        )
        viewBinding.appListView.layoutManager = LinearLayoutManager(this@ApplicationListActivity)

    }


    override fun getViewBindingObject(): ActivityApplicationListBinding {
        return ActivityApplicationListBinding.inflate(
            layoutInflater
        )
    }

    /**
     * 设置标题和活动栏
     */
    private fun setTitleAndActionBar() {
        viewBinding.toolbar.setTitle(R.string.select_game_pack)
        setReturnButton()
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setTitleAndActionBar()
            loadLayoutManagerToList()
            loadList(null, false)
        }
    }
}