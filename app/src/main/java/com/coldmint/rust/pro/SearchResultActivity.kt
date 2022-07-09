package com.coldmint.rust.pro

import android.os.Bundle
import androidx.core.view.isVisible
import com.coldmint.rust.core.dataBean.user.SearchResultDataBean
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Search
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.adapters.SearchPageAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivitySearchResultBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 搜索activity
 */
class SearchResultActivity : BaseActivity<ActivitySearchResultBinding>() {
    lateinit var keyWord: String
    val typeMap by lazy {
        val map = HashMap<String, Int>()
        map["mod"] = R.string.search_type_mod
        map["user"] = R.string.search_type_user
        map["dynamic"] = R.string.search_type_dynamic
        map["mod_comments"] = R.string.search_type_mod_comments
        map["mod_versions"] = R.string.search_type_mod_versions
        map["purchase_plan"] = R.string.search_type_purchase_plan
        map
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            loadData(keyWord)
            title = String.format(getString(R.string.search_mod_key), keyWord)
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
                    val list = t.data.total
                    if (list.isNotEmpty()) {
                        val adapter = SearchPageAdapter(this@SearchResultActivity, keyWord, t.data)
                        viewBinding.viewPager2.adapter = adapter
                        TabLayoutMediator(viewBinding.tabLayout, viewBinding.viewPager2) { tab, i ->
                            tab.text = if (i == 0) {
                                getString(R.string.search_type_mod_all) + "(" + t.data.total.size + ")"
                            } else {
                                val typeData = t.data.type[i - 1]
                                val id = typeMap[typeData.typeName] ?: -1
                                val name = if (id == -1) {
                                    typeData.typeName
                                } else {
                                    getString(id)
                                }
                                name + "(" + typeData.num + ")"
                            }
                        }.attach()
                        viewBinding.progressBar.isVisible = false
                        viewBinding.tipView.isVisible = false
                        viewBinding.contentLayout.isVisible = true
                        title = String.format(
                            getString(R.string.search_mod_key),
                            keyWord
                        )
                    } else {
                        showInfoToView(t.message)
                    }
                } else {
                    showInfoToView(t.message)
                }
            }

            override fun onFailure(e: Exception) {
                showInfoToView(this@SearchResultActivity.getString(R.string.network_error))
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
        viewBinding.contentLayout.isVisible = false
    }


    override fun getViewBindingObject(): ActivitySearchResultBinding {
        return ActivitySearchResultBinding.inflate(layoutInflater)
    }
}