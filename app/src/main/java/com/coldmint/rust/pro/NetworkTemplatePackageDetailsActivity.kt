package com.coldmint.rust.pro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.WebTemplatePackageDetailsData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.TemplateParser
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.adapters.TemplateItemAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityNetworkTemplatePackageDetailsBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration

class NetworkTemplatePackageDetailsActivity :
    BaseActivity<ActivityNetworkTemplatePackageDetailsBinding>() {
    //    private var createDirectory: String? = null
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        val id = intent.getStringExtra("id")
        if (id == null) {
            finish()
            showToast("请传入id")
            return
        }
        //创建目录是可选的参数，若传递了，那么以模板创建方式处理模板。
//        createDirectory = intent.getStringExtra("createDirectory")
        title = getString(R.string.title)
        setReturnButton()
        viewBinding.recyclerView.layoutManager = StableLinearLayoutManager(this)
        val divider = MaterialDividerItemDecoration(
            this,
            MaterialDividerItemDecoration.VERTICAL
        )
        viewBinding.recyclerView.addItemDecoration(
            divider
        )
        TemplatePhp.instance.getTemplateList(
            id,
            object : ApiCallBack<WebTemplatePackageDetailsData> {
                override fun onResponse(t: WebTemplatePackageDetailsData) {
                    if (t.code == ServerConfiguration.Success_Code) {
                        title = t.data.packageData.name
                        viewBinding.titleView.text =
                            t.data.packageData.modificationTime + " " + t.data.packageData.versionName
                        viewBinding.describeView.text = t.data.packageData.describe
                        val adapter = TemplateItemAdapter(
                            this@NetworkTemplatePackageDetailsActivity,
                            t.data.templateList.toMutableList()
                        )
                        adapter.setItemEvent { i, itemTemplateBinding, viewHolder, template ->
                            itemTemplateBinding.root.setOnClickListener {
                                val intent = Intent(
                                    this@NetworkTemplatePackageDetailsActivity,
                                    TemplateMakerActivity::class.java
                                )
                                intent.putExtra("name", template.title)
                                intent.putExtra("local", false)
                                intent.putExtra("path", template.id)
                                startActivity(intent)
                            }
                        }
                        viewBinding.recyclerView.adapter = adapter

                        viewBinding.progressBar.isVisible = false
                        viewBinding.contentView.isVisible = true
                    } else {
                        showToast(t.message)
                        finish()
                    }
                }

                override fun onFailure(e: Exception) {
                    showToast(e.toString())
                    finish()
                }

            })

    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityNetworkTemplatePackageDetailsBinding {
        return ActivityNetworkTemplatePackageDetailsBinding.inflate(layoutInflater)
    }

}