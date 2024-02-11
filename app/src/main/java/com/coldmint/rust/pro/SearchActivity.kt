package com.coldmint.rust.pro

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.coldmint.rust.core.dataBean.HotSearchData
import com.coldmint.rust.core.dataBean.SearchSuggestionsData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Search
import com.coldmint.rust.pro.adapters.HotSearchAdapter
import com.coldmint.rust.pro.adapters.SearchSuggestionsAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivitySearchBinding
import com.coldmint.rust.pro.databinding.ItemStringBinding
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 搜索界面
 */
class SearchActivity : BaseActivity<ActivitySearchBinding>() {
    lateinit var list: MutableList<String>

    @SuppressLint("CommitPrefEdits", "NotifyDataSetChanged")
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        title = getString(R.string.search)
        setReturnButton()
        viewBinding.recyclerView.layoutManager = StableLinearLayoutManager(this)
        viewBinding.hotSearchView.layoutManager = StableLinearLayoutManager(this)
        loadSearchView()
        loadHotSearch()
        list = getSharedPreferences("lishi", Context.MODE_PRIVATE)
                .getStringSet("data", mutableSetOf())
                ?.toMutableList() ?: mutableListOf()
        viewBinding.hotSearchView2.layoutManager = FlexboxLayoutManager(this)
        viewBinding.hotSearchView2.adapter = adapter
        viewBinding.deleat.setOnClickListener {
            MaterialAlertDialogBuilder(this@SearchActivity)
                    .setTitle("清空所有历史记录")
                    .setPositiveButton("确定") { _, _ ->
                        while (list.isNotEmpty()) {
                            val index = 0
                            list.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                        getSharedPreferences("lishi",
                                Context.MODE_PRIVATE).edit().putStringSet(
                                "data", list.toSet()).apply()
                        /*                        CoroutineScope(Dispatchers.Main).launch {
                                                    while (list.isNotEmpty()) {
                                                        list.removeAt(0) // 删除第一个数据
                                                        adapter.notifyItemRemoved(0) // 刷新 RecyclerView
                                                        delay(300) // 每隔一秒执行一次删除操作
                                                    }
                                                }*/
                    }
                    .setNegativeButton("取消", null).show()
        }
        viewBinding.searchView.onActionViewExpanded()
    }

    private fun additem(string: String) {
        val indexOf = list.indexOf(string)
        if (indexOf != -1) {
            list.remove(string)
            adapter.notifyItemRemoved(indexOf)
        }
        list.add(0, string)
        adapter.notifyItemInserted(0)
        // 限制历史记录数量为10
        if (list.size > 10) {
            list.removeAt(list.lastIndex)
        }
        val editor = getSharedPreferences("lishi", Context.MODE_PRIVATE).edit()
        editor.putStringSet("data", list.toSet())
        editor.apply()
    }

    fun search(string: String) {
        /*        val intent = Intent(this@SearchActivity, SearchResultActivity::class.java)
                intent.putExtra("key",string)
                startActivity(intent)*/
        additem(string)

    }

    val adapter: RecyclerView.Adapter<VH> = object : RecyclerView.Adapter<VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(ItemStringBinding.bind(
                    LayoutInflater.from(viewBinding.root.context)
                            .inflate(R.layout.item_string, parent, false)))

        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.binding.button.text = list[position]
            holder.binding.button.setOnClickListener {
                val a: Button = it as Button
                search(a.text.toString())
            }
        }

        override fun getItemCount(): Int {
            viewBinding.textview1Text1.isVisible = list.isEmpty()
            return list.size
        }

    }

    class VH(itemView: ItemStringBinding) : RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemStringBinding = itemView
    }


    private fun loadHotSearch() {
        Search.instance.hotSearch(object : ApiCallBack<HotSearchData> {
            override fun onResponse(t: HotSearchData) {
                val adapter = HotSearchAdapter(this@SearchActivity, t.data)
                adapter.setItemEvent { _, itemHotSearchBinding, _, data ->
                    itemHotSearchBinding.root.setOnClickListener {
                        search(data.keyword)
                    }
                }
                viewBinding.hotSearchView.adapter = adapter
            }

            override fun onFailure(e: Exception) {

            }

        })
    }

    private fun loadSearchView() {
        viewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    search(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
//                    viewBinding.searchSuggestionsView.setText(R.string.search_suggestions_loading)
                    Search.instance.suggestions(newText,
                            object : ApiCallBack<SearchSuggestionsData> {
                                override fun onResponse(t: SearchSuggestionsData) {
                                    val dataList = t.data
                                    if (dataList.isEmpty()) {
                                        viewBinding.recyclerView.isVisible = false
//                                    viewBinding.searchSuggestionsView.setText(R.string.search_suggestions_null)
                                    } else {
                                        val adapter =
                                                SearchSuggestionsAdapter(
                                                        this@SearchActivity,
                                                        newText,
                                                        dataList
                                                )
                                        adapter.setItemEvent { _, itemSearchSuggestionsBinding, _, s ->
                                            itemSearchSuggestionsBinding.root.setOnClickListener {
                                                search(s)
                                            }
                                        }
                                        viewBinding.recyclerView.adapter = adapter
                                        viewBinding.recyclerView.isVisible = true
//                                    val s = String.format(getString(R.string.search_suggestions_number),dataList.size)
//                                    viewBinding.searchSuggestionsView.text = s
                                    }
                                }

                                override fun onFailure(e: Exception) {

                                    viewBinding.recyclerView.isVisible = false
//                                viewBinding.searchSuggestionsView.setText(R.string.search_suggestions_null)
                                }

                            })
                } else {
//                    viewBinding.searchSuggestionsView.setText(R.string.search_suggestions_null)
                    viewBinding.recyclerView.isVisible = false
                }
                return true
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val add = menu.add("搜索")
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0) {
            if (!viewBinding.searchView.query.isNullOrBlank()) {
                search(viewBinding.searchView.query.toString())
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivitySearchBinding {
        return ActivitySearchBinding.inflate(layoutInflater)
    }

    override fun onBackPressed() {
        if (viewBinding.recyclerView.isVisible) {
            viewBinding.recyclerView.isVisible = false
            return
        }
        super.onBackPressed()
    }

}