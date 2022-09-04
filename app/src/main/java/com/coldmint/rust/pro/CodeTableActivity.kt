package com.coldmint.rust.pro

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.*
import androidx.core.view.isVisible
import com.coldmint.dialog.InputDialog
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.CodeInfo
import com.coldmint.rust.core.database.code.SectionInfo
import com.coldmint.rust.pro.adapters.CodeTableAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityCodeTableBinding
import java.util.concurrent.Executors

class CodeTableActivity : BaseActivity<ActivityCodeTableBinding>() {
    private val executorService = Executors.newSingleThreadExecutor()
    private var filterMode = false
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getString(R.string.code_table)
            setReturnButton()
            loadData()
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            ifNeedFinish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    fun ifNeedFinish() {
        if (filterMode) {
            loadData()
        } else {
            finish()
        }
    }

    /**
     * 加载数据
     * @param key String? 键
     * @param section String? 节
     */
    fun loadData(key: String? = null, section: String? = null) {
        executorService.submit {
            filterMode = key != null || section != null
            val sectionMap = HashMap<String, String>()
            val codeDataBase = CodeDataBase.getInstance(this)
            val allGroup: List<SectionInfo> = if (section == null) {
                codeDataBase.getSectionDao().getAll()
            } else {
                val sectionInfo = codeDataBase.getSectionDao().findSectionInfoByTranslate(section)
                if (sectionInfo == null) {
                    notFindKey(section)
                    return@submit
                } else {
                    listOf(sectionInfo)
                }
            }
            val group = allGroup.filter {
                sectionMap[it.code] = it.translate
                it.isVisible
            }.toMutableList()
            val versionGroup = codeDataBase.getVersionDao().getAll()
            val versionMap = HashMap<Int, String>()
            versionGroup.forEach {
                versionMap[it.versionNumber] = it.versionName
            }

            val typeGroup = codeDataBase.getValueTypeDao().getAll()
            val typeNameMap = HashMap<String, String>()
            typeGroup.forEach {
                typeNameMap[it.type] = it.name
            }

            val item = ArrayList<List<CodeInfo>>()
            val finalGroup = group.toList()
            for (section in finalGroup) {
                val list = if (key == null) {
                    codeDataBase.getCodeDao().findCodeBySection(section.code)
                } else {
                    codeDataBase.getCodeDao()
                        .findCodeByCodeOrTranslateFromSection(key, section.code)
                }
                if (list == null || list.isEmpty()) {
                    group.remove(section)
                } else {
                    item.add(list)
                }
            }

            if (group.isNotEmpty()) {
                val adapter = CodeTableAdapter(this, group, item)
                adapter.setVersionMap(versionMap)
                adapter.setTypeNameMap(typeNameMap)
                adapter.setSectionMap(sectionMap)
                runOnUiThread {
                    adapter.labelFunction = { index, view, string ->
                        loadData(section = string)
                    }
                    viewBinding.displayView.isVisible = false
                    viewBinding.progressBar.isVisible = false
                    viewBinding.expandableListView.isVisible = true
                    viewBinding.expandableListView.setAdapter(adapter)
                }
            } else {
                notFindKey(key)
            }
        }
    }

    /**
     * 没有找到节
     * @param key String?
     */
    fun notFindKey(key: String?) {
        if (key != null && key.isNotBlank()) {
            val tip = String.format(getString(R.string.not_find_code_name), key)
            val action = getString(R.string.not_find_units_action)
            val start = tip.indexOf(action)
            val spannableString = SpannableString(tip)
            if (start > -1) {
                spannableString.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            loadData()
                        }
                    }, start, start + action.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            viewBinding.displayView.movementMethod = LinkMovementMethod.getInstance();
            viewBinding.displayView.highlightColor = Color.parseColor("#36969696");
            viewBinding.displayView.text = spannableString

        }
        viewBinding.displayView.isVisible = true
        viewBinding.expandableListView.isVisible = false
        viewBinding.progressBar.isVisible = false
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_code_table, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_units -> {
                InputDialog(this).setTitle(R.string.filter).setMessage(R.string.filter_tip)
                    .setInputCanBeEmpty(false).setMaxNumber(20)
                    .setPositiveButton(R.string.dialog_ok) { text ->
                        var key = text
                        if (key.length > 20) {
                            key = key.substring(0, 20)
                        }
                        loadData(key)
                        true
                    }.setNegativeButton(R.string.dialog_close) {

                    }.show()
            }
            android.R.id.home -> {
                ifNeedFinish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityCodeTableBinding {
        return ActivityCodeTableBinding.inflate(layoutInflater)
    }
}