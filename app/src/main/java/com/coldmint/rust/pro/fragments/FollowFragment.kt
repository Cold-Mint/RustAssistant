package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coldmint.rust.core.dataBean.DynamicItemDataBean
import com.coldmint.rust.core.dataBean.follow.FollowUserListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.Community
import com.coldmint.rust.core.web.Dynamic
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.UserHomePageActivity
import com.coldmint.rust.pro.adapters.DynamicAdapter
import com.coldmint.rust.pro.adapters.UserHeadAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentFollowBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.ui.StableLinearLayoutManager

/**
 * 关注者
 * @author Cold Mint
 * @date 2021/12/28 10:23
 */
class FollowFragment : BaseFragment<FragmentFollowBinding>() {

    var oldSize: Int = 0
    var lastIndex = 0

    /**
     * 加载视图如果需要更新的话
     */
    fun loadViewIfNeed() {
        val loginStatus = AppSettings.getValue(AppSettings.Setting.LoginStatus, false)
        if (loginStatus) {
            val account = AppSettings.getValue(AppSettings.Setting.Account, "")
            Community.getUserList(
                account,
                limit = -1,
                apiCallBack = object : ApiCallBack<FollowUserListData> {
                    override fun onResponse(t: FollowUserListData) {
                        if (t.code == ServerConfiguration.Success_Code) {
                            val data = t.data
                            if (data == null) {
                                showTip(R.string.no_followers)
                            } else {
                                try {
                                    viewBinding.tipView.isVisible = false
                                    viewBinding.progressBar.isVisible = false
                                    viewBinding.linearLayout.isVisible = true
                                    if (oldSize != data.size + 1 && data.isNotEmpty()) {
                                        data.add(
                                            0,
                                            FollowUserListData.Data(
                                                account = "",
                                                enable = "",
                                                email = "",
                                                cover = "",
                                                headIcon = null,
                                                loginTime = "",
                                                gender = 0,
                                                userName = getString(R.string.all_dynamic),
                                                permission = ""
                                            )
                                        )
                                        val adapter = UserHeadAdapter(requireContext(), data)
                                        adapter.setItemEvent { i, itemUserHeadBinding, viewHolder, data ->
                                            itemUserHeadBinding.root.setOnClickListener {
                                                lastIndex = viewHolder.adapterPosition
                                                loadDynamic(data.account)
                                            }

                                            itemUserHeadBinding.root.setOnLongClickListener {
                                                val account = data.account
                                                if (account.isNotBlank()) {
                                                    openHomePage(data.account)
                                                }
                                                return@setOnLongClickListener true
                                            }
                                        }
                                        oldSize = data.size
                                        viewBinding.headRecyclerView.adapter = adapter
                                        //如果最后查看的位置小于总长度（不会下标越界），则加载上次的位置
                                        if (lastIndex < oldSize) {
                                            loadDynamic(data[lastIndex].account)
                                        } else {
                                            //等于或大于（加载末尾）
                                            loadDynamic(data[oldSize - 1].account)
                                        }

                                    }
                                } catch (e: Exception) {
                                    showTip(R.string.network_error)
                                }
                            }
                        } else {
                            showTip(content = t.message)
                        }

                    }

                    override fun onFailure(e: Exception) {
                        showTip(R.string.network_error)
                    }

                })
        } else {
            showTip(R.string.follow_introduction)
        }
    }

    /**
     * 打开某个用户主页
     * @param account String
     */
    fun openHomePage(account: String) {
        val goIntent =
            Intent(requireContext(), UserHomePageActivity::class.java)
        goIntent.putExtra("userId", account)
        startActivity(goIntent)
    }

    /**
     * 动态获取完成事件
     * @param t DynamicItemDataBean
     */
    fun getDynamicSuccess(t: DynamicItemDataBean) {
        if (!isAdded) {
            return
        }
        val data = t.data?.toMutableList()
        if (t.code == ServerConfiguration.Success_Code && data != null) {
            val adapter = DynamicAdapter(requireContext(), data)
            adapter.setItemEvent { i, itemDynamicBinding, viewHolder, data ->
                itemDynamicBinding.headIconView.setOnClickListener {
                    openHomePage(data.account)
                }
            }
            viewBinding.textview.isVisible = false
            viewBinding.progressBar2.isVisible = false
            viewBinding.recyclerView.isVisible = true
            viewBinding.recyclerView.adapter = adapter
        } else {
            viewBinding.progressBar2.isVisible = false
            viewBinding.textview.isVisible = true
            viewBinding.recyclerView.isVisible = false
            viewBinding.textview.text = t.message
        }
    }

    /**
     * 获取动态失败
     */
    fun getDynamicFailure() {
        viewBinding.progressBar2.isVisible = false
        viewBinding.textview.isVisible = true
        viewBinding.recyclerView.isVisible = false
        viewBinding.textview.setText(R.string.network_error)
    }

    /**
     * 加载动态
     * @param account String 账号
     */
    fun loadDynamic(account: String) {
        if (account.isBlank()) {
            val selfAccount = AppSettings.getValue(AppSettings.Setting.Account, "")
            Dynamic.instance.getFollowAllDynamic(selfAccount,
                object : ApiCallBack<DynamicItemDataBean> {
                    override fun onResponse(t: DynamicItemDataBean) {
                        getDynamicSuccess(t)
                    }

                    override fun onFailure(e: Exception) {
                        getDynamicFailure()
                    }

                })
        } else {
            Dynamic.instance.getList(object : ApiCallBack<DynamicItemDataBean> {
                override fun onResponse(t: DynamicItemDataBean) {
                    getDynamicSuccess(t)
                }

                override fun onFailure(e: Exception) {
                    getDynamicFailure()
                }

            }, account = account)
        }
    }

    override fun onResume() {
        super.onResume()
        loadViewIfNeed()
    }

    /**
     * 显示提示
     * @param resId Int 资源ID
     * @param content String? 内容
     */
    fun showTip(resId: Int = R.string.network_error, content: String? = null) {
        viewBinding.linearLayout.isVisible = false
        viewBinding.progressBar.isVisible = false
        viewBinding.tipView.isVisible = true
        if (content == null) {
            viewBinding.tipView.setText(resId)
        } else {
            viewBinding.tipView.text = content
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentFollowBinding {
        return FragmentFollowBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.rootLayout.layoutTransition.setAnimateParentHierarchy(false)
        viewBinding.linearLayout2.layoutTransition.setAnimateParentHierarchy(false)
        val linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        viewBinding.headRecyclerView.layoutManager = linearLayoutManager
        viewBinding.headRecyclerView.isNestedScrollingEnabled = false
        val linearLayoutManager2 = StableLinearLayoutManager(requireContext())
        viewBinding.recyclerView.layoutManager = linearLayoutManager2
        loadViewIfNeed()
    }
}