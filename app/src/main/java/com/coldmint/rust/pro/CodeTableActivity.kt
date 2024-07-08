package com.coldmint.rust.pro

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.database.code.CodeDataBase
import com.coldmint.rust.core.database.code.CodeInfo
import com.coldmint.rust.core.database.code.SectionInfo
import com.coldmint.rust.pro.adapters.CodeTableAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityCodeTableBinding
import com.muqing.gj
import java.util.concurrent.Executors

class CodeTableActivity : BaseActivity<ActivityCodeTableBinding>() {
    private val executorService = Executors.newSingleThreadExecutor()
    private var filterMode = false
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getString(R.string.code_table)
            viewBinding.edittext.hint = title

            setReturnButton()
            loadData()
            viewBinding.edittext.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(a: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(a: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (a.isNullOrEmpty()) {
                        loadData()
                        return
                    }
                    loadData(a.toString())
                }
                override fun afterTextChanged(a: Editable?) {
                }
            })
            viewBinding.edittext.setOnEditorActionListener { v, p1, _ ->
                if (p1 == EditorInfo.IME_ACTION_SEARCH) {
                    if (v?.text.isNullOrEmpty()) {
                        loadData()
                    }else
                        loadData(v?.text.toString())
                }
                false
            }
            viewBinding.back.setOnClickListener { finish() }
        }
    }

    /*

        override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                ifNeedFinish()
                return true
            }
            return super.onKeyDown(keyCode, event)
        }
    */


    private fun ifNeedFinish() {
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
                if (list.isNullOrEmpty()) {
                    group.remove(section)
                } else {
                    item.add(list)
                }
            }

            if (group.isNotEmpty()) {
                adapter = CodeTableAdapter(this, group, item, viewBinding.codeRecyclerB)
                adapter.setVersionMap(versionMap)
                adapter.setTypeNameMap(typeNameMap)
                adapter.setSectionMap(sectionMap)
                runOnUiThread {
/*                    adapter.labelFunction = { _, _, string ->
//                        section = string
                        if (string.isEmpty()) {
                            loadData()
                        }
                        loadData(string)
                    }*/
                    viewBinding.displayView.isVisible = false
                    viewBinding.progressBar.isVisible = false
                    viewBinding.expandableListView.isVisible = true
                    viewBinding.expandableListView.layoutManager = LinearLayoutManager(this)
                    viewBinding.expandableListView.setAdapter(adapter)
//                    viewBinding.expandableListView.swapAdapter(adapter, true)

                }
            } else {
                notFindKey(key)
            }
        }
    }

    lateinit var adapter: CodeTableAdapter
    /**
     * 没有找到节
     * @param key String?
     */
    private fun notFindKey(key: String?) {
        if (!key.isNullOrBlank()) {
            val tip = String.format(getString(R.string.not_find_code_name), key)
            val action = getString(R.string.not_find_units_action)
            val start = tip.indexOf(action)
            val spannableString = SpannableString(tip)
            if (start > -1) {
                spannableString.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            editisVisible(false)
                            loadData()
                        }
                    }, start, start + action.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            viewBinding.displayView.movementMethod = LinkMovementMethod.getInstance()
            viewBinding.displayView.highlightColor = Color.parseColor("#36969696")
            viewBinding.displayView.text = spannableString

        }
        viewBinding.displayView.isVisible = true
        viewBinding.expandableListView.isVisible = false
        viewBinding.progressBar.isVisible = false
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_code_table, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_units -> {
                if (viewBinding.edittext.isVisible) {
                    if (viewBinding.edittext.text.isNullOrEmpty()) {
                        loadData()
                    } else {
                        loadData(viewBinding.edittext.text.toString())
                    }
                } else {
                    editisVisible(true)
                }
                /*
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

                    }.show()*/
            }

            android.R.id.home -> {
                if (viewBinding.edittext.isVisible) {
                    editisVisible(false)
                    return true
                }
                ifNeedFinish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun editisVisible(b: Boolean) {
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewBinding.expandableListView.adapter !is CodeTableAdapter) {
            viewBinding.expandableListView.adapter = adapter
            return
        }
        if (viewBinding.edittext.text.isNotEmpty()) {
            viewBinding.edittext.setText("")
            gj.ycjp(viewBinding.edittext)
        } else {
            gj.tcjp(viewBinding.edittext)
            finish()
//            ifNeedFinish()
        }
    }
    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityCodeTableBinding {
        return ActivityCodeTableBinding.inflate(layoutInflater)
    }
}