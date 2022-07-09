package com.coldmint.rust.pro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.SearchSuggestionsData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Search
import com.coldmint.rust.pro.adapters.SearchSuggestionsAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivitySearchBinding

/**
 * 搜索界面
 */
class SearchActivity : BaseActivity<ActivitySearchBinding>() {
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        title = getString(R.string.search)
        setReturnButton()
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)

        viewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotBlank()) {
                    val intent = Intent(this@SearchActivity, SearchResultActivity::class.java)
                    intent.putExtra("key", query)
                    startActivity(intent)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotBlank()) {
                    Search.instance.suggestions(newText,
                        object : ApiCallBack<SearchSuggestionsData> {
                            override fun onResponse(t: SearchSuggestionsData) {
                                val dataList = t.data
                                if (dataList.isNullOrEmpty()) {
                                    viewBinding.recyclerView.isVisible = false
                                } else {
                                    val adapter =
                                        SearchSuggestionsAdapter(
                                            this@SearchActivity,
                                            newText,
                                            dataList
                                        )
                                    adapter.setItemEvent { i, itemSearchSuggestionsBinding, viewHolder, s ->
                                        itemSearchSuggestionsBinding.root.setOnClickListener {
                                            val intent = Intent(
                                                this@SearchActivity,
                                                SearchResultActivity::class.java
                                            )
                                            intent.putExtra("key", s)
                                            startActivity(intent)
                                        }
                                    }
                                    viewBinding.recyclerView.adapter = adapter
                                    viewBinding.recyclerView.isVisible = true
                                }
                            }

                            override fun onFailure(e: Exception) {
                                viewBinding.recyclerView.isVisible = false
                            }

                        })
                } else {
                    viewBinding.recyclerView.isVisible = false
                }
                return true
            }

        })
    }

    override fun getViewBindingObject(): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }

}