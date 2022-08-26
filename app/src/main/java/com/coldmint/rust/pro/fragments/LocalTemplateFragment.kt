package com.coldmint.rust.pro.fragments


import android.view.LayoutInflater
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.pro.adapters.TemplateListAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.TemplateFragemntBinding
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * 本地模板碎片
 * @property first Boolean
 */
class LocalTemplateFragment : BaseFragment<TemplateFragemntBinding>() {
    private var first = true

    override fun onResume() {
        super.onResume()
        if (!first) {
            val directent = AppSettings.getValue(
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
            AppSettings.getValue(AppSettings.Setting.AppLanguage, Locale.getDefault().language)
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            val mutableList: ArrayList<LocalTemplatePackage> = ArrayList()
            for (f in files) {
                val tem = LocalTemplatePackage(f)
                if (tem.isTemplate) {
                    mutableList.add(tem)
                }
            }
            if (mutableList.isNotEmpty()) {
                val listAdapter = TemplateListAdapter(requireContext(), mutableList, language, path)
                val layoutManager = LinearLayoutManager(activity)
                viewBinding.templateList.layoutManager = layoutManager
                viewBinding.templateList.adapter = listAdapter
                viewBinding.swipeRefreshLayout.isVisible = true
                viewBinding.templateError.isVisible = false
                viewBinding.templateErrorIcon.isVisible = false
            } else {
                viewBinding.swipeRefreshLayout.isVisible = false
                viewBinding.templateError.isVisible = true
                viewBinding.templateErrorIcon.isVisible = true
            }
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): TemplateFragemntBinding {
        return TemplateFragemntBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        val directent = AppSettings.getValue(
            AppSettings.Setting.TemplateDirectory,
            requireContext().filesDir.absolutePath + "/template/"
        )
        loadList(directent)
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            loadList(directent)
            viewBinding.swipeRefreshLayout.isRefreshing = false
            true
        }
    }
}