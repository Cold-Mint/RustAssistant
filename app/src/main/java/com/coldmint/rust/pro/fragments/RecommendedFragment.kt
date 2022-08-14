package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.coldmint.rust.core.dataBean.BannerItemDataBean
import com.coldmint.rust.core.dataBean.mod.WebModListData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.BannerManager
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.MainActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.WebModInfoActivity
import com.coldmint.rust.pro.adapters.WebModAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.RecommendedFragmentBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.tool.TextStyleMaker
import com.google.android.material.color.DynamicColors
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.transformer.AlphaPageTransformer

class RecommendedFragment : BaseFragment<RecommendedFragmentBinding>() {

    /**
     * 加载最近更新
     */
    fun loaList() {
        //如果进度条可见那么为首次加载
        val isFirst = viewBinding.progressBar.isVisible
        viewBinding.latestReleaseProgressIndicator.isVisible = true
        WebMod.instance.list(object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                if (t.code == ServerConfiguration.Success_Code) {
                    val data = t.data?.toMutableList()
                    if (!data.isNullOrEmpty()) {
                        viewBinding.progressBar.postDelayed({
                            viewBinding.latestReleaseProgressIndicator.isVisible = false
                            if (isFirst) {
                                viewBinding.progressBar.isVisible = false
                                viewBinding.nestedScrollView.isVisible = true
                            }
                            viewBinding.latestReleaseView.adapter = createAdapter(data)
                        }, MainActivity.hideViewDelay)
                    }
                } else {
                    viewBinding.latestReleaseProgressIndicator.isVisible = false
                }
            }

            override fun onFailure(e: Exception) {
                viewBinding.latestReleaseProgressIndicator.isVisible = false
            }

        }, limit = "6", sortMode = WebMod.SortMode.Latest_Time)
    }


    /**
     * 加载随机推荐
     */
    fun loadRandomRecommended() {
        viewBinding.randomRecommendedProgressIndicator.isVisible = true
        WebMod.instance.randomRecommended(6, object : ApiCallBack<WebModListData> {
            override fun onResponse(t: WebModListData) {
                val data = t.data?.toMutableList()
                if (data == null || data.isEmpty()) {
                    viewBinding.randomRecommendedProgressIndicator.isVisible = false
                } else {
                    viewBinding.randomRecommendedProgressIndicator.isVisible = false
                    viewBinding.randomRecommendedView.isVisible = true
                    viewBinding.randomRecommendedView.adapter = createAdapter(data)
                }
            }

            override fun onFailure(e: Exception) {
                e.printStackTrace()
                viewBinding.randomRecommendedProgressIndicator.isVisible = false
            }

        })
    }

    /**
     * 加载个性化推荐
     */
    fun loadSoleRecommended() {
        viewBinding.soleRecommendedCardView.isVisible = false
        viewBinding.soleRecommendedProgressIndicator.isVisible = true
        val account = AppSettings.getValue(AppSettings.Setting.Account, "")
        if (account.isNotBlank()) {
            WebMod.instance.soleRecommended(account, object : ApiCallBack<WebModListData> {
                override fun onResponse(t: WebModListData) {
                    if (isAdded) {
                        val data = t.data?.toMutableList()
                        if (!data.isNullOrEmpty()) {
                            viewBinding.soleRecommendedCardView.isVisible = true
                            viewBinding.soleRecommendedProgressIndicator.isVisible = false
                            viewBinding.soleRecommendedRecyclerView.adapter = createAdapter(data)
                        }
                    }
                }

                override fun onFailure(e: Exception) {
                    if (isAdded) {
                        viewBinding.soleRecommendedCardView.isVisible = false
                        viewBinding.soleRecommendedProgressIndicator.isVisible = false
                    }
                }

            }, limit = "6")
        }
    }

    /**
     * 加载轮播图数据
     */
    fun loadBannerData() {
        BannerManager.instance.getItems(object : ApiCallBack<BannerItemDataBean> {
            override fun onResponse(t: BannerItemDataBean) {
                if (t.code == ServerConfiguration.Success_Code) {
                    val dataList = t.data
                    if (dataList != null && dataList.isNotEmpty()) {
                        val forever =
                            AppSettings.getValue(
                                AppSettings.Setting.ExpirationTime,
                                0.toLong()
                            ) == (-2).toLong()
                        val textStyleMaker = TextStyleMaker.instance
                        val showList = dataList.filter {
                            var show = true
                            val type = textStyleMaker.getType(it.link)
                            //如果点击事件为激活助手，但此用户已经永久激活，那么隐藏轮播图
                            if (type == "activate" && forever) {
                                show = false
                            }
                            show
                        }
                        viewBinding.banner.setAdapter(object :
                            BannerImageAdapter<BannerItemDataBean.Data>(showList) {
                            override fun onBindView(
                                holder: BannerImageHolder?,
                                data: BannerItemDataBean.Data?,
                                position: Int,
                                size: Int
                            ) {
                                if (holder != null && data != null) {
                                    Glide.with(holder.itemView).load(data.picture)
                                        .apply(GlobalMethod.getRequestOptions())
                                        .into(holder.imageView)
                                    holder.imageView.setOnClickListener {
                                        val type = textStyleMaker.getType(data.link)
                                        val linkData = textStyleMaker.getData(data.link)
                                        if (type == null || linkData == null) {
                                            MaterialDialog(requireContext()).show {
                                                title(text = data.title).message(text = data.link)
                                                    .positiveButton(R.string.dialog_ok)
                                            }
                                        } else {
                                            textStyleMaker.clickEvent(
                                                requireContext(),
                                                type,
                                                linkData
                                            )
                                        }
                                    }
                                }
                            }
                        })
                        viewBinding.banner.setBannerGalleryEffect(16, 16, 8)
                        viewBinding.banner.addPageTransformer(AlphaPageTransformer())
                        if (activity != null) {
                            viewBinding.banner.addBannerLifecycleObserver(activity)
                            viewBinding.banner.indicator = CircleIndicator(activity)
//                            viewBinding.banner.setIndicatorSelectedColorRes(R.color.pink_500)

                        }
                    }
                } else {

                }
            }

            override fun onFailure(e: Exception) {

            }

        })
    }


    fun createAdapter(dataList: MutableList<WebModListData.Data>): WebModAdapter {
        val adapter = WebModAdapter(context = requireContext(), dataList = dataList)
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
        return adapter
    }

    override fun onResume() {
        super.onResume()
        loaList()
        loadSoleRecommended()
        loadBannerData()
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): RecommendedFragmentBinding {
        return RecommendedFragmentBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        viewBinding.latestReleaseView.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.soleRecommendedRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        viewBinding.randomRecommendedView.layoutManager = LinearLayoutManager(requireContext())
        loadRandomRecommended()
        viewBinding.changeRandomRecommended.setOnClickListener {
            loadRandomRecommended()
        }
    }
}