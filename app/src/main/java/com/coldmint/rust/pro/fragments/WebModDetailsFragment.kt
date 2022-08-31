package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.coldmint.rust.core.dataBean.mod.WebModInfoData
import com.coldmint.rust.core.dataBean.user.SpaceInfoData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.User
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.TagActivity
import com.coldmint.rust.pro.UserHomePageActivity
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentWebModDetailsBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.tool.TextStyleMaker
import com.google.android.material.chip.Chip
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator

/**
 * 模组详情碎片
 */
class WebModDetailsFragment(val modId: String) : BaseFragment<FragmentWebModDetailsBinding>() {
    var developer: String? = null

    //此模组是否对外开放
    private var isOpen = false

    /**
     * 获取此模组是否对外开放
     * @return Boolean
     */
    fun isOpen(): Boolean {
        return isOpen
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        loadInfo()
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentWebModDetailsBinding {
        return FragmentWebModDetailsBinding.inflate(layoutInflater)
    }


    fun loadDeveloperInfo(userId: String) {
        User.getSpaceInfo(userId, object : ApiCallBack<SpaceInfoData> {
            override fun onResponse(t: SpaceInfoData) {
                if (t.code == ServerConfiguration.Success_Code) {
                    val icon = t.data.headIcon
                    if (icon != null) {
                        Glide.with(requireContext())
                            .load(ServerConfiguration.getRealLink(icon))
                            .apply(GlobalMethod.getRequestOptions(true))
                            .into(viewBinding.headIconView)
                    }
                    viewBinding.userNameView.text = t.data.userName
                    val info = String.format(
                        getString(R.string.fans_information),
                        ServerConfiguration.numberToString(t.data.fans),
                        ServerConfiguration.numberToString(t.data.follower),
                        ServerConfiguration.numberToString(t.data.praise)
                    )
                    viewBinding.userInfoView.text = info

                    viewBinding.cardView.postDelayed({
                        viewBinding.cardView.isVisible = true
                        viewBinding.openUserSpace.setOnClickListener {
                            gotoUserPage(t.data.account)
                        }
                    }, 300)
                } else {
                    viewBinding.cardView.isVisible = false
                }

            }

            override fun onFailure(e: Exception) {
                viewBinding.cardView.isVisible = false
            }

        })
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

    /**
     * 加载页面信息
     */
    fun loadInfo() {
        val token = AppSettings.getValue(AppSettings.Setting.Token, "")
        WebMod.instance.getInfo(token, modId, object : ApiCallBack<WebModInfoData> {
            override fun onResponse(t: WebModInfoData) {
                if (t.code == ServerConfiguration.Success_Code) {
                    developer = t.data.developer
                    isOpen = t.data.hidden == 0
                    viewBinding.loadLayout.isVisible = false
                    viewBinding.contentLayout.isVisible = true
                    val icon = t.data.icon
                    if (icon != null && icon.isNotBlank()) {
                        Glide.with(requireContext())
                            .load(ServerConfiguration.getRealLink(icon))
                            .apply(GlobalMethod.getRequestOptions())
                            .into(viewBinding.iconView)
                    }
                    val screenshotListData = t.data.screenshots
                    if (screenshotListData != null && screenshotListData.isNotBlank()) {
                        val list = ArrayList<String>()
                        val lineParser = LineParser()
                        lineParser.symbol = ","
                        lineParser.text = screenshotListData
                        lineParser.analyse { lineNum, lineData, isEnd ->
                            list.add(lineData)
                            true
                        }
                        val adapter = object : BannerImageAdapter<String>(list) {
                            override fun onBindView(
                                holder: BannerImageHolder?,
                                data: String?,
                                position: Int,
                                size: Int
                            ) {
                                if (data != null && holder != null) {
                                    Glide.with(requireContext())
                                        .load(ServerConfiguration.getRealLink(data))
                                        .apply(GlobalMethod.getRequestOptions())
                                        .into(holder.imageView)
                                }
                            }
                        }
                        viewBinding.banner.setAdapter(adapter)
                        viewBinding.banner.addBannerLifecycleObserver(requireActivity())
                        viewBinding.banner.indicator = CircleIndicator(requireActivity())
                        viewBinding.banner.setIndicatorSelectedColorRes(R.color.blue_500)
                        viewBinding.banner.isAutoLoop(false)
                    } else {
                        viewBinding.banner.isVisible = false
                    }
                    val tags = t.data.tags
                    val lineParser = LineParser(tags)
                    val tagList = ArrayList<String>()
                    lineParser.symbol = ","
                    lineParser.analyse { lineNum, lineData, isEnd ->
                        val tag = lineData.subSequence(1, lineData.length - 1).toString()
                        tagList.add(tag)
                        true
                    }
                    if (tagList.size > 0) {
                        tagList.forEach {
                            val chip = Chip(requireContext())
                            chip.text = it
                            val s = it
                            chip.setOnClickListener {
                                val bundle = Bundle()
                                bundle.putString("tag", s)
                                bundle.putString(
                                    "title",
                                    String.format(getString(R.string.tag_title), s)
                                )
                                bundle.putString("action", "tag")
                                val thisIntent =
                                    Intent(requireContext(), TagActivity::class.java)
                                thisIntent.putExtra("data", bundle)
                                startActivity(thisIntent)
                            }
                            viewBinding.chipGroup.addView(chip)
                        }
                    } else {
                        viewBinding.chipGroup.isVisible = false
                    }
                    viewBinding.titleView.text = t.data.name
                    TextStyleMaker.instance.load(
                        viewBinding.modInfoView,
                        t.data.describe
                    ) { type, data ->
                        TextStyleMaker.instance.clickEvent(requireContext(), type, data)
                    }
                    viewBinding.numView.text =
                        String.format(
                            getString(R.string.unit_and_downloadnum),
                            t.data.unitNumber,
                            t.data.downloadNumber,
                            t.data.versionName
                        )
                    viewBinding.updateTimeView.text =
                        String.format(getString(R.string.recent_update), t.data.updateTime)
                    if (t.data.hidden == 0) {
                        viewBinding.hideTextView.isVisible = false
                    }
                    loadDeveloperInfo(t.data.developer)
                } else {
                    viewBinding.contentLayout.isVisible = false
                    viewBinding.loadLayout.isVisible = true
                    viewBinding.tipView.isVisible = true
                    viewBinding.tipView.text = t.message
                    viewBinding.progressBar.isVisible = false
                }
            }

            override fun onFailure(e: Exception) {
                viewBinding.contentLayout.isVisible = false
                viewBinding.loadLayout.isVisible = true
                viewBinding.progressBar.isVisible = false
                viewBinding.tipView.isVisible = true
                viewBinding.tipView.setText(R.string.network_error)
            }

        })
    }
}