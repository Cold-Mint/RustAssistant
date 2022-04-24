package com.coldmint.rust.pro.fragments


import android.view.LayoutInflater
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.pro.tool.AppSettings
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.coldmint.rust.core.TemplatePackage
import com.coldmint.rust.pro.adapters.TemplateListAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.TemplateFragemntBinding
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class TemplateFragment : BaseFragment<TemplateFragemntBinding>() {
    private var first = true

    override fun onResume() {
        super.onResume()
        if (!first) {
            val directent = appSettings.getValue(
                AppSettings.Setting.TemplateDirectory,
                requireContext().filesDir.absolutePath + "/template/"
            )
            loadList(directent)
        } else {
            first = false
        }
    }

    fun loadList(path: String) {
        val language =
            appSettings.getValue(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            val mutableList: ArrayList<TemplatePackage> = ArrayList()
            for (f in files) {
                val tem = TemplatePackage(f)
                if (tem.isTemplate) {
                    mutableList.add(tem)
                }
            }
            if (mutableList.isNotEmpty()) {
                val listAdapter = TemplateListAdapter(requireContext(), mutableList, language, path)
                val layoutManager = LinearLayoutManager(activity)
                viewBinding.templateList.layoutManager = layoutManager
                viewBinding.templateList.adapter = listAdapter
                viewBinding.templateList.isVisible = true
                viewBinding.templateError.isVisible = false
                viewBinding.templateErrorIcon.isVisible = false
            } else {
                viewBinding.templateList.isVisible = false
                viewBinding.templateError.isVisible = true
                viewBinding.templateErrorIcon.isVisible = true
            }
        }
    }

    override fun getViewBindingObject(): TemplateFragemntBinding {
        return TemplateFragemntBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        val directent = appSettings.getValue(
            AppSettings.Setting.TemplateDirectory,
            requireContext().filesDir.absolutePath + "/template/"
        )
        loadList(directent)
    }
}