package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.WebTemplatePackageListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.TemplatePhp
import com.coldmint.rust.pro.NetworkTemplatePackageDetailsActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.WebTemplateAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentTemplateCommunityBinding
import com.coldmint.rust.pro.tool.AppSettings
import me.zhanghai.android.fastscroll.FastScrollerBuilder

/**
 * 模板社区
 */
class TemplateCommunityFragment : BaseFragment<FragmentTemplateCommunityBinding>() {
    val token = AppSettings.getValue(AppSettings.Setting.Token, "")

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadData()
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }


    fun loadData() {
        TemplatePhp.instance.getPublicTemplatePackageList(token, object :
            ApiCallBack<WebTemplatePackageListData> {
            override fun onResponse(t: WebTemplatePackageListData) {
                viewBinding.swipeRefreshLayout.isVisible = true
                viewBinding.loadView.isVisible = false
                viewBinding.errorLayout.isVisible = false
                if (t.data != null) {
                    val adapter = WebTemplateAdapter(requireContext(), t.data!!)
                    adapter.setItemEvent { i, itemWebTemplateBinding, viewHolder, data ->
                        itemWebTemplateBinding.root.setOnClickListener {
                            val intent = Intent(requireContext(),
                                NetworkTemplatePackageDetailsActivity::class.java)
                            intent.putExtra("id",data.id)
                            intent.putExtra("createDirectory",data.id)
                            startActivity(intent)
                        }
                        itemWebTemplateBinding.button.setOnClickListener {
                            var subscribe = data.subscribe
                            if (subscribe) {
                                CoreDialog(requireContext()).setTitle(R.string.de_subscription)
                                    .setMessage(
                                        String.format(
                                            getString(R.string.de_subscription_msg),
                                            data.getName()
                                        )
                                    ).setPositiveButton(R.string.dialog_ok) {
//退订
                                        TemplatePhp.instance.deleteSubscription(token, data.id,
                                            object : ApiCallBack<ApiResponse> {
                                                override fun onResponse(t: ApiResponse) {
                                                    if (t.code == ServerConfiguration.Success_Code) {
                                                        itemWebTemplateBinding.button.setText(R.string.subscription)
                                                        data.subscribe = false
                                                    } else {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            t.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }

                                                override fun onFailure(e: Exception) {
                                                    e.printStackTrace()
                                                    Toast.makeText(
                                                        requireContext(),
                                                        R.string.network_error,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                            })
                                    }.setNegativeButton(R.string.dialog_cancel) {

                                    }.setCancelable(false).show()
                            } else {
                                TemplatePhp.instance.subscription(token, data.id,
                                    object : ApiCallBack<ApiResponse> {
                                        override fun onResponse(t: ApiResponse) {
                                            if (t.code == ServerConfiguration.Success_Code) {
                                                itemWebTemplateBinding.button.setText(R.string.de_subscription)
                                                data.subscribe = true
                                            } else {
                                                Toast.makeText(
                                                    requireContext(),
                                                    t.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onFailure(e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.network_error,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    })
                            }
                        }
                    }
                    viewBinding.recyclerView.adapter = adapter
                    FastScrollerBuilder(viewBinding.recyclerView).useMd2Style()
                        .setPopupTextProvider(adapter).build()
                }
            }

            override fun onFailure(e: Exception) {
                e.printStackTrace()
                viewBinding.loadView.isVisible = false
                viewBinding.errorLayout.isVisible = true
                viewBinding.swipeRefreshLayout.isVisible = false
            }

        })
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentTemplateCommunityBinding {
        return FragmentTemplateCommunityBinding.inflate(layoutInflater)
    }
}