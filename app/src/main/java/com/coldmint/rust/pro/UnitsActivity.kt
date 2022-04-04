package com.coldmint.rust.pro


import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.SourceFile
import android.os.Bundle
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.snackbar.Snackbar
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.adapters.ModPageAdapter
import com.coldmint.rust.pro.databinding.ActivityUnitsBinding
import com.coldmint.rust.pro.databinding.DialogSearchUnitsBinding
import com.coldmint.rust.pro.fragments.AllUnitsFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * @author Cold Mint
 */
class UnitsActivity : BaseActivity<ActivityUnitsBinding>() {
    private lateinit var modPageAdapter: ModPageAdapter

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1) {
                val path = data.getStringExtra("File")
                if (path != null) {
                    val file = File(path)
                    val sourceFileClass = SourceFile(file)
                    if (modPageAdapter.initAllUnitsFragment) {
                        modPageAdapter.allUnitsFragment.addFileToHistory(
                            sourceFileClass,
                            useThread = true,
                            whenAddComplete = {
                                modPageAdapter.historyUnitFragment.loadList()
                                modPageAdapter.historyUnitFragment.needUpDateUnitsList = true
                            })
                    } else {
                        viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(1))
                    }
                    viewBinding.fab.postDelayed({
                        Snackbar.make(
                            viewBinding.fab,
                            R.string.create_unit_complete,
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.open) {
                            modPageAdapter.allUnitsFragment.openEditActivity(sourceFileClass)
                        }.show()
                    }, MainActivity.hideViewDelay)
                }
            } else if (requestCode == 2) {
                //编辑单位创建源文件后刷新
                modPageAdapter.allUnitsFragment.loadFiles()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_units -> {
                MaterialDialog(this).show {
                    title(R.string.filter).message(R.string.filter_tip)
                    input(maxLength = 20, waitForPositiveButton = false) { dialog, text ->
                        if (text.isNotEmpty()) {
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                        }
                    }.positiveButton(R.string.dialog_ok, null) { dialog ->
                        var key = dialog.getInputField().text.toString()
                        if (key.length > 20) {
                            key = key.substring(0, 20)
                        }
                        viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(1))
                        modPageAdapter.allUnitsFragment.filter(key)
                    }.negativeButton(R.string.dialog_close)
                }
            }
            R.id.search_units -> {
                viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(1))
                val dialogSearchUnitsBinding = DialogSearchUnitsBinding.inflate(layoutInflater)
                val executorService = Executors.newCachedThreadPool()
                dialogSearchUnitsBinding.advancedSearchBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    dialogSearchUnitsBinding.fileContentInputLayout.isVisible = isChecked
                    dialogSearchUnitsBinding.matchesFileNamesByReBox.isVisible = isChecked
                    dialogSearchUnitsBinding.fileNameInputLayout.isVisible = isChecked
                    dialogSearchUnitsBinding.isCodeBox.isVisible = isChecked
                }
                val showTip: (translate: String) -> Unit = {
                    executorService.submit {
                        val isCode = dialogSearchUnitsBinding.isCodeBox.isChecked
                        if (!isCode) {
                            runOnUiThread {
                                dialogSearchUnitsBinding.fileContentInputLayout.helperText = ""
                            }
                            return@submit
                        }
                        val codeInfo = CodeDataBase.getInstance(this@UnitsActivity).getCodeDao()
                            .findCodeByTranslate(it)
                        if (codeInfo == null) {
                            runOnUiThread {
                                dialogSearchUnitsBinding.fileContentInputLayout.helperText =
                                    it
                            }
                        } else {
                            runOnUiThread {
                                dialogSearchUnitsBinding.fileContentInputLayout.helperText =
                                    codeInfo.code
                            }
                        }
                    }
                }
                dialogSearchUnitsBinding.isCodeBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    showTip.invoke(dialogSearchUnitsBinding.fileContentInputView.text.toString())
                }
                dialogSearchUnitsBinding.fileContentInputView.addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        showTip.invoke(s.toString())
                    }

                })
                MaterialDialog(this).show {
                    title(R.string.search)
                    customView(view = dialogSearchUnitsBinding.root)
                        .positiveButton(R.string.dialog_ok).positiveButton {
                            modPageAdapter.allUnitsFragment.advancedSearch(
                                configuration = AllUnitsFragment.SearchConfiguration(
                                    dialogSearchUnitsBinding.unitNameInputView.text.toString(),
                                    dialogSearchUnitsBinding.fileNameInputView.text.toString(),
                                    dialogSearchUnitsBinding.matchesFileNamesByReBox.isChecked,
                                    dialogSearchUnitsBinding.fileContentInputView.text.toString(),
                                    dialogSearchUnitsBinding.advancedSearchBox.isChecked,
                                    dialogSearchUnitsBinding.isCodeBox.isChecked
                                )
                            )
                        }
                        .negativeButton(R.string.dialog_close)
                }
            }
            R.id.rebuild -> {
                MaterialAlertDialogBuilder(this).setTitle(R.string.rebuild_project)
                    .setMessage(R.string.rebuild_project_tip)
                    .setPositiveButton(R.string.clean_up_cache_and_rebuild) { dialog, which ->
                        thread {
                            modPageAdapter.fileDataBase.getFileInfoDao().clearTable()
                            modPageAdapter.fileDataBase.getValueDao().clearTable()
                            runOnUiThread {
                                viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(1))
                                modPageAdapter.allUnitsFragment.loadFiles()
                            }
                        }
                    }.setNegativeButton(R.string.dialog_cancel, null).show()
                return true
            }
            android.R.id.home -> {
                exitActivity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 关闭数据库链接并退出活动
     */
    fun exitActivity() {
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_units, menu)
        return true
    }


    override fun getViewBindingObject(): ActivityUnitsBinding {
        return ActivityUnitsBinding.inflate(layoutInflater)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            exitActivity()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            viewBinding.toolbar.setTitle(R.string.mod_action1)
            setSupportActionBar(viewBinding.toolbar)
            setReturnButton()
            val intent = intent
            val bundle = intent.getBundleExtra("data")
            val path = bundle!!.getString("path")
            val modClass = ModClass(File(path))
            modPageAdapter = ModPageAdapter(this, modClass)
            viewBinding.pager.adapter = modPageAdapter
            TabLayoutMediator(viewBinding.tabLayout, viewBinding.pager)
            { tab, position ->
                when (position) {
                    0 -> {
                        modPageAdapter.setHistoryChanged {
                            tab.text = "${getText(R.string.recently_opened)}(${it})"
                        }
                        tab.text = getText(R.string.recently_opened)
                    }
                    1 -> {
                        modPageAdapter.setAllUnitsChanged {
                            tab.text = "${getText(R.string.all_units)}(${it})"
                        }
                        tab.text = getText(R.string.all_units)
                    }
                }
            }.attach()
            viewBinding.tabLayout.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val finalTab = tab
                    if (finalTab != null) {
                        val tiltle = finalTab.text ?: ""
                        val allUnits = getString(R.string.all_units)
                        if (tiltle.startsWith(allUnits)) {
                            if (modPageAdapter.historyUnitFragment.needUpDateUnitsList) {
                                modPageAdapter.historyUnitFragment.needUpDateUnitsList = false
                                modPageAdapter.allUnitsFragment.loadFiles()

                            }
                            viewBinding.fab.hide()
                        } else {
                            modPageAdapter.historyUnitFragment.loadList()

                            viewBinding.fab.show()
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

            })
            viewBinding.fab.setOnClickListener {
                val intent = Intent(this@UnitsActivity, CreateUnitActivity::class.java)
                val bundle = Bundle()
                bundle.putString("modPath", modClass.modFile.absolutePath)
                intent.putExtra("data", bundle)
                startActivityForResult(intent, 1)
            }
        }
    }
}