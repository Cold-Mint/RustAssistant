package com.coldmint.rust.pro

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.adapters.WebModAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityTagBinding

/**
 * @author Cold Mint
 * @date 2021/12/10 17:27
 */
class TagActivity : BaseActivity<ActivityTagBinding>() {
    var titleStr: String? = null


    /**
     * 加载标签
     * @param tag String
     */
    fun loadTag(tag: String) {
        WebMod.instance.list(object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                onEventResponse(t)
            }

            override fun onFailure(e: Exception) {
                onEventFailure()
            }

        }, "[${tag}]")
    }

    /**
     * 加载用户模组
     * @param account String 账号
     * @param sortMode SortMode? 排序模式
     */
    fun loadUser(account: String, sortMode: WebMod.SortMode?) {
        WebMod.instance.getUserModList(account, object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                onEventResponse(t)
            }

            override fun onFailure(e: Exception) {
                onEventFailure()
            }

        }, sortMode)
    }

    /**
     * 搜索模式
     * @param keyWord String
     */
    fun loadKeyWord(keyWord: String) {
        WebMod.instance.search(keyWord, object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                onEventResponse(t)
            }

            override fun onFailure(e: Exception) {
                onEventFailure()
            }

        })
    }

    /**
     * 当网络请求成功
     * @param t WebModListData
     */
    fun onEventResponse(t: WebModListData) {
        val dataList = t.data
        if (t.code == ServerConfiguration.Success_Code && dataList != null) {
            val adapter = WebModAdapter(this, dataList)
            adapter.setItemEvent { i, webModItemBinding, viewHolder, data ->
                webModItemBinding.root.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("modId", data.id)
                    bundle.putString("modName", data.name)
                    val intent =
                        Intent(this@TagActivity, WebModInfoActivity::class.java)
                    intent.putExtra("data", bundle)
                    this@TagActivity.startActivity(intent)
                }
            }
            viewBinding.recyclerView.adapter = adapter
            viewBinding.recyclerView.isVisible = true
            viewBinding.progressBar.isVisible = false
        } else {
            viewBinding.progressBar.isVisible = false
            viewBinding.tipView.isVisible = true
            viewBinding.tipView.text = t.message
        }
    }

    /**
     * 当网络加载失败
     */
    fun onEventFailure() {
        viewBinding.progressBar.isVisible = false
        viewBinding.tipView.isVisible = true
        viewBinding.tipView.text = getString(R.string.network_error)
    }


    override fun getViewBindingObject(): ActivityTagBinding {
        return ActivityTagBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            val thisIntent = intent
            val bundle = thisIntent.getBundleExtra("data")
            if (bundle == null) {
                showError("启动错误")
                return
            }
            titleStr = bundle.getString("title")
            val action = bundle.getString("action")
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)

            if (action == null) {
                showError("错误，action为空")
                return
            } else {
                when (action) {
                    "tag" -> {
                        val tag = bundle.getString("tag")
                        if (tag == null) {
                            showError("错误，tag为空")
                            return
                        }
                        title = titleStr ?: tag
                        loadTag(tag)
                    }
                    "user-download" -> {
                        val account = bundle.getString("account")
                        if (account == null) {
                            showError("错误，account为空")
                            return
                        }
                        title = titleStr ?: account
                        loadUser(account, WebMod.SortMode.Download_Number)
                    }
                    "user-time" -> {
                        val account = bundle.getString("account")
                        if (account == null) {
                            showError("错误，account为空")
                            return
                        }
                        title = titleStr ?: account
                        loadUser(account, WebMod.SortMode.Latest_Time)
                    }
                    "search-mod" -> {
                        val keyWord = bundle.getString("keyword")
                        if (keyWord == null) {
                            showError("错误，keyword为空")
                            return
                        }
                        title = titleStr ?: keyWord
                        loadKeyWord(keyWord)
                    }
                }
            }
        }
    }

}