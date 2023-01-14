package com.coldmint.rust.pro.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.SubscriptionData
import com.coldmint.rust.core.dataBean.template.LocalTemplateFile
import com.coldmint.rust.core.dataBean.template.Template
import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.core.debug.LogCat
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.FileFinderListener
import com.coldmint.rust.core.tool.FileFinder2
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.base.BaseViewModel
import com.coldmint.rust.pro.tool.AppSettings
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


/**
 * 已安装的模板
 */
class InstalledTemplateViewModel : BaseViewModel() {

    val createPathLiveData: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val onlyLoad by lazy {
        AppSettings.getValue(AppSettings.Setting.OnlyLoadConantLanguageTemple, false)
    }

    val environmentLanguage by lazy {
        AppSettings.getValue(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
    }

    private var loadCallBack: (() -> Unit)? = null

    /**
     * 设置加载完成的回调
     * @param callBack Function0<Unit>?
     */
    fun setLoadCallBack(callBack: (() -> Unit)? = null) {
        loadCallBack = callBack
    }

    var mRootPath: String? = null

    private val groupList: ArrayList<TemplatePackage> by lazy {
        ArrayList()
    }
    private val itemList: ArrayList<ArrayList<Template>> by lazy {
        ArrayList()
    }

    /**
     * 获取组
     * @return ArrayList<TemplatePackage>
     */
    fun getGroupData(): ArrayList<TemplatePackage> {
        return groupList
    }

    /**
     * 获取组
     * @return ArrayList<TemplatePackage>
     */
    fun getItemData(): ArrayList<ArrayList<Template>> {
        return itemList
    }

    /**
     * 加载本地和网络模板
     * 如果设置了[InstalledTemplateViewModel.setLoadCallBack]回调，那么会调用接口.
     * 请在回调内使用[InstalledTemplateViewModel.getGroupData]和[InstalledTemplateViewModel.getItemData]获取加载结果
     * @param context Context
     */
    fun loadTemplate(context: Context) {
        groupList.clear()
        itemList.clear()
        val token = AppSettings.getValue(AppSettings.Setting.Token,"")
        TemplatePhp.instance.getSubscriptionDataList(token,object :ApiCallBack<SubscriptionData>{
            override fun onResponse(t: SubscriptionData) {
                if (t.code == ServerConfiguration.Success_Code){
                    LogCat.d("加载网络订阅模板", "正在处理。")
                    t.data.forEach {
                        groupList.add(it)
                        val temList = ArrayList<Template>()
                        itemList.add(temList)
                        it.templateList.forEach {
                            temList.add(it)
                        }
                    }
                    loadLocalTemplate(context)
                    loadCallBack?.invoke()
                }else{
                    LogCat.w("加载网络订阅模板", t.message)
                    loadLocalTemplate(context)
                    loadCallBack?.invoke()
                }
            }

            override fun onFailure(e: Exception) {
                e.printStackTrace()
                LogCat.e("加载网络订阅模板", e.toString())
                loadLocalTemplate(context)
                loadCallBack?.invoke()
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
                            groupList.add(templatePackage)
                            LogCat.d("加载本地模板", "已创建" + templatePackage.getName() + "组")
                            val temList = ArrayList<Template>()
                            itemList.add(temList)
                            val fileFinder2 = FileFinder2(it)
                            fileFinder2.setFinderListener(object : FileFinderListener {
                                override fun whenFindFile(file: File): Boolean {
                                    if (FileOperator.getFileType(file) == "json") {
//读取目录内所有json文件，将其添加到子集内
                                        val templateFile = LocalTemplateFile(file)
                                        if (onlyLoad) {
                                            val data = FileOperator.readFile(file)
                                            try {
                                                val jsonObject = JSONObject(data)
                                                val s = jsonObject.getString("language")
                                                if (s == "ALL" || s == environmentLanguage) {
                                                    temList.add(templateFile)
                                                    LogCat.d("加载本地模板", "已成功分配" + file.absolutePath)
                                                } else {
                                                    LogCat.w("加载本地模板", "不符合语言的项目" + file.absolutePath)
                                                }
                                            } catch (exception: JSONException) {
                                                exception.printStackTrace()
                                            }
                                        } else {
                                            temList.add(templateFile)
                                            LogCat.d("加载本地模板", "已成功分配" + file.absolutePath)
                                        }
                                    } else {
                                        LogCat.w("加载本地模板", "无法分配" + file.absolutePath)
                                    }
                                    return true
                                }

                                override fun whenFindFolder(folder: File): Boolean {
                                    return true
                                }
                            })
                            fileFinder2.onStart()
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