package com.coldmint.rust.pro.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.WebTemplatePackageListData
import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.TemplateSelectAdapter
import com.coldmint.rust.pro.databinding.FragmentSaveTemplateBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject
import java.io.File

/**
 * 保存模板对话框
 */
class SaveTemplateFragment(val name: String, val json: JSONObject) : BottomSheetDialogFragment() {

    private lateinit var selectAdapter: TemplateSelectAdapter
    private val list: ArrayList<TemplatePackage> = ArrayList()

    private lateinit var fragmentSaveTemplateBinding: FragmentSaveTemplateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSaveTemplateBinding =
            FragmentSaveTemplateBinding.inflate(layoutInflater, container, false)
        return fragmentSaveTemplateBinding.root
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentSaveTemplateBinding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        loadWebTemplate {
            loadLocalTemplate(requireContext())
            loadList()
        }
        fragmentSaveTemplateBinding.allButton.setOnClickListener {
            list.clear()
            loadWebTemplate {
                loadLocalTemplate(requireContext())
                loadList()
            }
        }
        fragmentSaveTemplateBinding.localButton.setOnClickListener {
            list.clear()
            loadLocalTemplate(requireContext())
            loadList()
        }
        fragmentSaveTemplateBinding.networkButton.setOnClickListener {
            list.clear()
            loadWebTemplate {
                loadList()
            }
        }
        fragmentSaveTemplateBinding.positiveButton.setOnClickListener {
            selectAdapter.getSelectedList().forEach {
                val id = it.pathOrId
                if (it.isLocal) {
                    //如果是本地
                    val newFile = File(id + "/" + name + ".json")
                    FileOperator.writeFile(newFile, json.toString(4))
                } else {
                    val token = AppSettings.getValue(AppSettings.Setting.Token, "")
                    TemplatePhp.instance.addTemplate(name,
                        token,
                        name,
                        json.toString(4),
                        id,
                        object : ApiCallBack<ApiResponse> {
                            override fun onResponse(t: ApiResponse) {

                            }

                            override fun onFailure(e: Exception) {

                            }

                        })
                }
            }
            dismiss()
        }
        fragmentSaveTemplateBinding.negativeButton.setOnClickListener {
            dismiss()
        }
    }

    fun loadList() {
        selectAdapter = TemplateSelectAdapter(requireContext(), list)
        selectAdapter.setSelectNumberChanged {
            fragmentSaveTemplateBinding.positiveButton.isEnabled = it != 0
            fragmentSaveTemplateBinding.title.text =
                getString(R.string.save_template) + "(${it})"
        }
        fragmentSaveTemplateBinding.recyclerView.adapter = selectAdapter
    }

    /**
     * 加载网络模板
     * @param func Function0<Unit> 当加载完成
     */
    fun loadWebTemplate(func: (() -> Unit)) {
        TemplatePhp.instance.getTemplatePackageList(
            AppSettings.getValue(
                AppSettings.Setting.Token,
                ""
            ), object : ApiCallBack<WebTemplatePackageListData> {
                override fun onResponse(t: WebTemplatePackageListData) {
                    if (t.data != null) {
                        t.data.forEach {
                            list.add(it)
                        }
                    }
                    func.invoke()
                }

                override fun onFailure(e: Exception) {
                    func.invoke()
                }


            })
    }


    /**
     * 加载本地模板
     * @param context Context
     */
    private fun loadLocalTemplate(context: Context) {
        val templateDirectory = File(
            AppSettings.getValue(
                AppSettings.Setting.TemplateDirectory,
                context.filesDir.absolutePath + "/template/"
            )
        )
        if (templateDirectory.exists() && templateDirectory.isDirectory) {
            LogCat.d("加载本地模板", "正在读取" + templateDirectory.absolutePath)
            val files = templateDirectory.listFiles()
            if (files.isNotEmpty()) {
                files.forEach {
                    if (it.isDirectory) {
                        //如果是文件夹那么创建组
                        val templatePackage =
                            LocalTemplatePackage(it)
                        if (templatePackage.isTemplate) {
                            list.add(templatePackage)
                            LogCat.w("加载本地模板", "已添加，文件" + it.absolutePath)
                        } else {
                            LogCat.w("加载本地模板", "文件" + it.absolutePath + "不是模板包")
                        }
                    } else {
                        LogCat.w("加载本地模板", "文件" + it.absolutePath + "不是文件夹")
                    }
                }
            } else {
                LogCat.w("加载本地模板", "目录" + templateDirectory.absolutePath + "内，没有文件，无法加载。")
            }
        } else {
            LogCat.e("加载本地模板", "模板目录不存在或不是文件夹" + templateDirectory.absolutePath)
        }

    }


}