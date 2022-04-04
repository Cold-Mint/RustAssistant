package com.coldmint.rust.pro

import com.coldmint.rust.pro.base.BaseActivity
import android.os.Bundle
import com.coldmint.rust.pro.tool.AppSettings
import android.content.Intent
import android.os.Environment
import org.json.JSONObject
import org.json.JSONException
import android.widget.AdapterView.OnItemLongClickListener
import com.afollestad.materialdialogs.MaterialDialog
import android.widget.*
import com.coldmint.rust.core.TemplatePackage
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.adapters.TemplateAdapter
import com.coldmint.rust.pro.databinding.ActivityCreateUnitBinding
import java.io.File
import java.util.*

class CreateUnitActivity : BaseActivity<ActivityCreateUnitBinding>() {
    private lateinit var mCreatePath: String
    private var mRootPath: String? = null
    private var mTemplateAdapter: TemplateAdapter? = null

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setTitle(R.string.create_unit)
            setReturnButton()
            initView()
            initAction()
        }
    }

    fun initView() {
        val useing = appSettings.getValue(AppSettings.Setting.DatabasePath, "")
        val intent = intent
        val bundle = intent.getBundleExtra("data")
        if (bundle == null) {
            Toast.makeText(this, "无效的请求", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            mRootPath = bundle.getString("modPath")
            mCreatePath = bundle.getString("createPath", mRootPath)
            var relativePath = FileOperator.getRelativePath(
                mCreatePath,
                appSettings.getValue(
                    AppSettings.Setting.ModFolder,
                    Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units/"
                )
            )
            if (relativePath == null) {
                relativePath = mCreatePath
            }
            viewBinding.unitPathView.setText(
                String.format(
                    (resources.getText(R.string.unit_path) as String),
                    relativePath
                )
            )
            loadlist()
        }
    }

    fun getmCreatePath(): String {
        return mCreatePath
    }

    fun loadlist() {
        val directent = appSettings.getValue(
            AppSettings.Setting.TemplateDirectory,
            this@CreateUnitActivity.filesDir.absolutePath + "/template/"
        )
        val only_load_language =
            appSettings.getValue(AppSettings.Setting.OnlyLoadConantLanguageTemple, false)
        val environmentLanguage =
            appSettings.getValue(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
        val path = File(directent)
        if (!path.exists()) {
            path.mkdirs()
            return
        }
        val group: MutableList<TemplatePackage> = ArrayList()
        val item: MutableList<List<File>> = ArrayList()
        //扫描模板根目录（加载所有模板包）
        if (path.isDirectory) {
            val files = path.listFiles()
            if (files.size > 0) {
                for (directents in files) {
                    //读取模板包
                    val templateClass = TemplatePackage(directents)
                    if (templateClass.isTemplate) {
                        group.add(templateClass)
                        //添加子集
                        val itemlist: MutableList<File> = ArrayList()
                        search(directents, itemlist, only_load_language, environmentLanguage)
                        item.add(itemlist)
                    }
                }
            }
        }
        mTemplateAdapter =
            TemplateAdapter(this@CreateUnitActivity, group, item, environmentLanguage)
        viewBinding.expandableList.setAdapter(mTemplateAdapter)
    }

    //扫描某目录的模板(目录,欲保存到的集合,全局语言,全局方法,仅加载符合语言的？,环境语言)
    private fun search(
        directents: File,
        list: MutableList<File>,
        only: Boolean,
        environmentLanguage: String
    ) {
        for (f in directents.listFiles()) {
            if (f.isDirectory) {
                search(f, list, only, environmentLanguage)
            } else {
                val type = FileOperator.getFileType(f)
                if (type == "json") {
                    if (only) {
                        val data = FileOperator.readFile(f)
                        try {
                            val jsonObject = JSONObject(data)
                            val s = jsonObject.getString("language")
                            if (s == "ALL" || s == environmentLanguage) {
                                list.add(f)
                            }
                        } catch (exception: JSONException) {
                            exception.printStackTrace()
                        }
                    } else {
                        list.add(f)
                    }
                }
            }
        }
    }

    fun initAction() {
        viewBinding.selectPathButton.setOnClickListener {
            val bundle = Bundle()
            val intent = Intent(this@CreateUnitActivity, FileManagerActivity::class.java)
            bundle.putString("type", "selectDirectents")
            bundle.putString("path", mCreatePath)
            bundle.putString("rootpath", mRootPath)
            intent.putExtra("data", bundle)
            startActivityForResult(intent, 1)
        }
        //长按监听
        viewBinding.expandableList.onItemLongClickListener =
            OnItemLongClickListener { parent, view, flatPos, l -> //得到点击的父位置，子位置
                val packedPos = (parent as ExpandableListView).getExpandableListPosition(flatPos)
                val groupPosition = ExpandableListView.getPackedPositionGroup(packedPos)
                val childPosition = ExpandableListView.getPackedPositionChild(packedPos)
                if (childPosition == -1) { //长按的是父项
                    //这里做关于父项的相关操作......
                    val numView = view.findViewById<TextView>(R.id.template_num)
                    val templateClass = mTemplateAdapter!!.getGroup(groupPosition) as TemplatePackage
                    MaterialDialog(this).show {
                        title(R.string.template_info).message(
                            text = templateClass.getInfo()?.description
                                ?: templateClass.directest.absolutePath
                        ).cancelable(false).positiveButton(R.string.dialog_ok).positiveButton {
                            numView.setText(R.string.del_moding)
                            Thread {
                                FileOperator.delete_files(templateClass.directest)
                                runOnUiThread { loadlist() }
                            }.start()
                        }.negativeButton(R.string.dialog_cancel)
                    }
                } else { //长按的是子项
                    //这里做关于子项的相关操作.......
                }
                true
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK) {
                val directents = data!!.getStringExtra("Directents")
                if (directents != null) {
                    mCreatePath = directents
                    var relativePath = FileOperator.getRelativePath(
                        mCreatePath,
                        appSettings.getValue(
                            AppSettings.Setting.ModFolder,
                            Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units/"
                        )
                    )
                    if (relativePath == null) {
                        relativePath = mCreatePath
                    }
                    viewBinding.unitPathView.text = String.format(
                        (resources.getText(R.string.unit_path) as String),
                        relativePath
                    )
                }
            }
            2 -> if (resultCode == RESULT_OK) {
                val path = data!!.getStringExtra("File")
                val intent = Intent()
                intent.putExtra("File", path)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun getViewBindingObject(): ActivityCreateUnitBinding {
        return ActivityCreateUnitBinding.inflate(layoutInflater)
    }


}