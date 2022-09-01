package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.mod.WebModCommentData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.UserHomePageActivity
import com.coldmint.rust.pro.adapters.CommentAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentModCommentsBinding
import com.coldmint.rust.pro.dialog.CommentDialog
import com.coldmint.rust.pro.tool.AppSettings
import com.google.android.material.snackbar.Snackbar

/**
 * 模组评论适配器
 */
class ModCommentsFragment(val modId: String) : BaseFragment<FragmentModCommentsBinding>() {
    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            loadCommentList(modId, false)
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
        viewBinding.sendDiscussion.setOnClickListener {
            val account = AppSettings.getValue(AppSettings.Setting.Account, "")
            if (account.isBlank()) {
                Toast.makeText(requireContext(), R.string.please_login_first, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            CommentDialog(requireContext()).setCancelable(false)
                .setSubmitFun { button, textInputLayout, s, alertDialog ->
                    button.isEnabled = false
                    WebMod.instance.sendComment(
                        AppSettings.getValue(AppSettings.Setting.Token, ""),
                        modId,
                        s,
                        object : ApiCallBack<ApiResponse> {
                            override fun onResponse(t: ApiResponse) {
                                if (t.code == ServerConfiguration.Success_Code) {
                                    alertDialog.dismiss()
                                    loadCommentList(modId)
                                    Snackbar.make(
                                        viewBinding.sendDiscussion,
                                        R.string.release_ok,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                } else {
                                    textInputLayout.error = t.message
                                }
                            }

                            override fun onFailure(e: Exception) {
                                textInputLayout.error = e.toString()
                            }

                        })
                }.show()
        }
    }

    /**
     * 打开用户主页
     * @param userId String
     */
    fun gotoUserPage(userId: String) {
        val intent = Intent(
            requireContext(),
            UserHomePageActivity::class.java
        )
        intent.putExtra("userId", userId)
        startActivity(
            intent
        )
    }

    override fun onResume() {
        super.onResume()
        loadCommentList(modId)
    }

    /**
     * 加载评论列表
     * @param modId String
     */
    fun loadCommentList(modId: String, useLinearProgressIndicator: Boolean = true) {
        val key = "加载评论列表"
        if (useLinearProgressIndicator) {
            viewBinding.linearProgressIndicator.isVisible = true
        }
        WebMod.instance.getCommentsList(modId, object : ApiCallBack<WebModCommentData> {
            override fun onResponse(t: WebModCommentData) {
                val list = t.data
                if (list.isNullOrEmpty()) {
                    DebugHelper.printLog(key, "为空", isError = true)
                    if (useLinearProgressIndicator) {
                        viewBinding.linearProgressIndicator.isVisible = false
                    }
                    viewBinding.titleView.text = getString(R.string.discussion)
                    viewBinding.recyclerView.isVisible = false
                    viewBinding.noContentLayout.isVisible = true
                } else {
                    DebugHelper.printLog(key, "共${list.size}条数据")
                    viewBinding.titleView.text =
                        getString(R.string.discussion) + "(" + list.size + ")"
                    if (useLinearProgressIndicator) {
                        viewBinding.linearProgressIndicator.isVisible = false
                    }
                    viewBinding.recyclerView.isVisible = true
                    viewBinding.noContentLayout.isVisible = false
                    val adapter = CommentAdapter(requireContext(), list)
                    adapter.setItemEvent { i, itemCommentBinding, viewHolder, data ->
                        itemCommentBinding.iconView.setOnClickListener {
                            gotoUserPage(data.account)
                        }
                    }
                    viewBinding.recyclerView.adapter =  adapter
                }
            }

            override fun onFailure(e: Exception) {
                DebugHelper.printLog(key, "加载失败", isError = true)
                if (useLinearProgressIndicator) {
                    viewBinding.linearProgressIndicator.isVisible = false
                }
                viewBinding.titleView.text = getString(R.string.discussion)
                viewBinding.recyclerView.isVisible = false
                viewBinding.noContentLayout.isVisible = true
            }

        })
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentModCommentsBinding {
        return FragmentModCommentsBinding.inflate(layoutInflater)
    }


}