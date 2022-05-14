package com.coldmint.rust.pro

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.SearchResultDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Search
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.adapters.SearchResultAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivitySearchBinding

/**
 * 搜索activity
 */
class SearchActivity : BaseActivity<ActivitySearchBinding>() {
    lateinit var keyWord: String
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            loadData(keyWord)
            title = String.format(getString(R.string.search_mod_key), keyWord)
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        } else {
            val thisIntent = intent
            val key = thisIntent.getStringExtra("key")
            if (key == null) {
                showError("key为null")
                return
            }
            keyWord = key
        }
    }

    /**
     * 加载数据
     * @param keyWord String
     */
    fun loadData(keyWord: String) {
        Search.instance.searchAll(keyWord, object : ApiCallBack<SearchResultDataBean> {
            override fun onResponse(t: SearchResultDataBean) {
                if (t.code == ServerConfiguration.Success_Code) {
                    val list = t.data
                    if (list != null && list.isNotEmpty()) {
                        val adapter = SearchResultAdapter(this@SearchActivity, keyWord, list)
                        viewBinding.progressBar.isVisible = false
                        viewBinding.tipView.isVisible = false
                        viewBinding.recyclerView.isVisible = true
                        viewBinding.recyclerView.adapter = adapter
                        title = String.format(
                            getString(R.string.search_mod_key),
                            keyWord
                        ) + "(" + list.size + ")"
                    } else {
                        showInfoToView(t.message)
                    }
                } else {
                    showInfoToView(t.message)
                }
            }

            override fun onFailure(e: Exception) {
                showInfoToView(this@SearchActivity.getString(R.string.network_error))
            }

        })
    }

    /**
     * 显示信息到视图
     * @param text String
     */
    fun showInfoToView(text: String) {
        viewBinding.tipView.isVisible = true
        viewBinding.tipView.text = text
        viewBinding.progressBar.isVisible = false
        viewBinding.recyclerView.isVisible = false
    }


    override fun getViewBindingObject(): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }
}