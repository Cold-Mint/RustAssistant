package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.TagActivity
import com.coldmint.rust.pro.WebModInfoActivity
import com.coldmint.rust.pro.adapters.WebModAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentPersonalHomeBinding
import com.coldmint.rust.pro.ui.ScrollLinearLayoutManager
import com.coldmint.rust.pro.ui.StableLinearLayoutManager

/**
 * @author Cold Mint
 * @date 2021/12/14 8:55
 */
class PersonalHomeFragment(val userId: String) : BaseFragment<FragmentPersonalHomeBinding>() {

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentPersonalHomeBinding {
        return FragmentPersonalHomeBinding.inflate(LayoutInflater.from(requireContext()))
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.latestWorkRecycleView.layoutManager =
                ScrollLinearLayoutManager(requireContext())
        viewBinding.highestScoreRecycleView.layoutManager =
                ScrollLinearLayoutManager(requireContext())
        viewBinding.highestScoreActionView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(
                    "title", viewBinding.highestScoreView.text.toString()
            )
            bundle.putString("action", "user-download")
            bundle.putString("account", userId)
            val thisIntent =
                    Intent(requireContext(), TagActivity::class.java)
            thisIntent.putExtra("data", bundle)
            startActivity(thisIntent)
        }
        viewBinding.latestWorkActionView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(
                    "title", viewBinding.latestWorkView.text.toString()
            )
            bundle.putString("action", "user-time")
            bundle.putString("account", userId)
            val thisIntent =
                    Intent(requireContext(), TagActivity::class.java)
            thisIntent.putExtra("data", bundle)
            startActivity(thisIntent)
        }

        WebMod.instance.getUserModList(userId, object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                val dataList = t.data?.toMutableList()
                if (t.code == ServerConfiguration.Success_Code && dataList != null && dataList.isNotEmpty()) {
                    val adapter = WebModAdapter(requireContext(), dataList)
                    adapter.setItemEvent { i, webModItemBinding, viewHolder, data ->
                        webModItemBinding.root.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putString("modId", data.id)
                            bundle.putString("modName", data.name)
                            val intent = Intent(requireContext(), WebModInfoActivity::class.java)
                            intent.putExtra("data", bundle)
                            requireContext().startActivity(intent)
                        }
                    }
                    viewBinding.latestWorkRecycleView.adapter = adapter
                } else {
                    viewBinding.latestWorkView.text = t.message
                    viewBinding.latestWorkActionView.isVisible = false
                }
            }

            override fun onFailure(e: Exception) {
                viewBinding.latestWorkView.setText(R.string.network_error)
                viewBinding.latestWorkActionView.isVisible = false
            }

        }, limit = "4", sortMode = WebMod.SortMode.Latest_Time)

        WebMod.instance.getUserModList(userId, object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                val dataList = t.data?.toMutableList()
                if (t.code == ServerConfiguration.Success_Code && dataList != null && dataList.isNotEmpty()) {
                    val adapter = WebModAdapter(requireContext(), dataList)
                    adapter.setItemEvent { i, webModItemBinding, viewHolder, data ->
                        webModItemBinding.root.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putString("modId", data.id)
                            bundle.putString("modName", data.name)
                            val intent = Intent(requireContext(), WebModInfoActivity::class.java)
                            intent.putExtra("data", bundle)
                            requireContext().startActivity(intent)
                        }
                    }
                    viewBinding.highestScoreRecycleView.adapter = adapter
                } else {
                    viewBinding.highestScoreView.text = t.message
                    viewBinding.highestScoreActionView.isVisible = false
                }
            }

            override fun onFailure(e: Exception) {
                viewBinding.highestScoreView.setText(R.string.network_error)
                viewBinding.highestScoreActionView.isVisible = false
            }

        }, limit = "4", sortMode = WebMod.SortMode.Download_Number)
    }
}