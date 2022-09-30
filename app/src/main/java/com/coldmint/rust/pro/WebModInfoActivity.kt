package com.coldmint.rust.pro

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.setPeekHeight
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.AppUpdateData
import com.coldmint.rust.core.dataBean.mod.WebModCommentData
import com.coldmint.rust.core.dataBean.mod.WebModInfoData
import com.coldmint.rust.core.dataBean.mod.WebModUpdateLogData
import com.coldmint.rust.core.dataBean.user.SpaceInfoData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.FileLoader
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.tool.ProgressResponseBody
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.User
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.adapters.CommentAdapter
import com.coldmint.rust.pro.adapters.ModPageDetailsAdapter
import com.coldmint.rust.pro.databinding.ActivityWebModInfoBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.LoadFileLayoutBinding
import com.coldmint.rust.pro.dialog.CommentDialog
import com.coldmint.rust.pro.tool.GlobalMethod
import com.coldmint.rust.pro.tool.TextStyleMaker
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import java.io.File
import java.text.NumberFormat

class WebModInfoActivity : BaseActivity<ActivityWebModInfoBinding>() {

    lateinit var modId: String
    lateinit var tip: String
    val targetFile: File by lazy {
        val modFolderPath = AppSettings.getValue(
            AppSettings.Setting.ModFolder,
            Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units/"
        )
        val modFolder = File(modFolderPath)
        if (!modFolder.exists()) {
            modFolder.mkdirs()
        }
        val modFilePath = "$modFolderPath$modId.rwmod"
        File(modFilePath)
    }
    val token by lazy {
        AppSettings.getValue(AppSettings.Setting.Token, "")
    }
    lateinit var adapter: ModPageDetailsAdapter

    private fun initView() {
        setReturnButton()
        val activityIntent = intent
        val bundle = activityIntent.getBundleExtra("data")
        if (bundle == null) {
            showError("意外的请求")
            return
        } else {
            val name = bundle.getString("modName")
            title = name
            val temId = bundle.getString("modId")
            if (temId == null) {
                showError("未知的模组id")
                return
            }
            modId = temId
            if (targetFile.exists()) {
                viewBinding.button.isEnabled = false
                viewBinding.button.text = getString(R.string.installated)
            }
            adapter = ModPageDetailsAdapter(this, modId)
            adapter.modName.observe(this) {
                title = it
            }
            viewBinding.viewPager2.adapter = adapter
            TabLayoutMediator(viewBinding.tabLayout, viewBinding.viewPager2) { tab, i ->
                tab.text = when (i) {
                    0 -> {
                        getString(R.string.details)
                    }
                    1 -> {
                        getString(R.string.insert_coins)
                    }
                    2 -> {
                        getString(R.string.discussion)
                    }
                    else -> {
                        getString(R.string.title)
                    }
                }
            }.attach()
            viewBinding.button.setOnClickListener {
                val type = viewBinding.button.text
                val installation = getString(R.string.installation)
                when (type) {
                    installation -> {
                        downloadAction(adapter.getLink())
                    }
                }

            }
            tip = getString(R.string.file_download_progress)
        }
    }

    /**
     * 下载事件
     * @param t WebModInfoData
     */
    fun downloadAction(link: String?) {
        if (link == null) {
            return
        }
        val fileLink = ServerConfiguration.getRealLink(link)
        when (AppOperator.getNetworkType(this)) {
            AppOperator.NetWorkType.NetWorkType_Moble -> {
                val useMobileNetWork =
                    AppSettings.getValue(AppSettings.Setting.UseMobileNetwork, false)
                if (useMobileNetWork) {
                    downloadWork(fileLink)
                } else {
                    CoreDialog(this).setTitle(R.string.using_mobile_networks)
                        .setMessage(R.string.using_mobile_networks_msg)
                        .setPositiveButton(R.string.only_one) {
                            downloadWork(fileLink)
                        }.setNegativeButton(R.string.always_allow) {
                            AppSettings.setValue(AppSettings.Setting.UseMobileNetwork, true)
                            downloadWork(fileLink)
                        }.setNeutralButton(R.string.dialog_cancel) {

                        }.show()
                }
            }
            AppOperator.NetWorkType.NetWorkType_Wifi -> {
                downloadWork(fileLink)
            }
            else -> {}
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_webmod, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.report_item -> {
                if (adapter.isOpen()) {
                    val thisIntent = Intent(this, ReportActivity::class.java)
                    val bundle = Bundle()
                    bundle.putString("target", modId)
                    bundle.putString("type", "mod")
                    bundle.putString("name", title.toString())
                    thisIntent.putExtra("data", bundle)
                    startActivity(thisIntent)
                } else {
                    //不能举报未公开的模组
                    Snackbar.make(
                        viewBinding.button,
                        R.string.unable_to_report,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            R.id.share_item -> {
                val link = AppSettings.getValue(AppSettings.Setting.ServerAddress, "")
                val updateData = AppSettings.getValue(AppSettings.Setting.UpdateData, "")
                var appUpdateLink = ""
                if (!updateData.isNullOrBlank()) {
                    val gson = Gson()
                    val updateDataObj = gson.fromJson(updateData, AppUpdateData.Data::class.java)
                    appUpdateLink = updateDataObj.link
                }
                val s = String.format(
                    getString(R.string.share_mod_msg),
                    title,
                    link + "website/pages/modPage.php?&modId=" + modId, appUpdateLink
                )

                AppOperator.shareText(this,getString(R.string.share_mod),s)
            }
            R.id.update_record -> {
                GlobalMethod.showUpdateLog(this, modId)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * 下载工作
     * @param fileLink String
     */
    fun downloadWork(fileLink: String) {
        GlobalMethod.requestStoragePermissions(this) {
            if (it) {
                viewBinding.button.setText(R.string.installation_ing)
                val loadFileLayoutBinding = LoadFileLayoutBinding.inflate(layoutInflater)
                loadFileLayoutBinding.LinearProgressIndicator.max = 100
                var progress = 0

                val fileLoader = FileLoader.getInstantiate(fileLink, targetFile.absolutePath)
                fileLoader.download(object : ProgressResponseBody.ResponseProgressListener {
                    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                        //计算百分比并更新ProgressBar
                        val numberFormat = NumberFormat.getNumberInstance()
                        numberFormat.maximumFractionDigits = 2
                        val trueProgress =
                            numberFormat.format(bytesRead.toDouble() / contentLength.toDouble() * 100)
                        progress = trueProgress.toFloat().toInt()
                        runOnUiThread {
                            val progressTip = String.format(tip, progress)
                            viewBinding.button.text = progressTip
                        }
                    }

                    override fun downloadFail(exception: Exception?) {
                        viewBinding.button.setText(R.string.installation)
                    }

                    override fun downloadSuccess() {
                        viewBinding.button.isEnabled = false
                        viewBinding.button.setText(R.string.installated)
                        WebMod.instance.addDownloadNum(modId)
                    }

                })
            }
        }
    }

//    /**
//     * 加载评论列表
//     * @param modId String
//     */
//    fun loadModCommentList(modId: String) {
//        viewBinding.commentLinearProgressIndicator.isVisible = true
//        WebMod.instance.getCommentsList(modId, object : ApiCallBack<WebModCommentData> {
//            override fun onResponse(t: WebModCommentData) {
//                viewBinding.commentLinearProgressIndicator.isVisible = false
//                val data = t.data
//                if (data == null) {
//                    viewBinding.modCommentRecyclerView.isVisible = false
//                } else {
//                    val adapter = CommentAdapter(this@WebModInfoActivity, data)
//                    viewBinding.discussion.text =
//                        String.format(getString(R.string.discussion_num), data.size)
//                    adapter.setItemEvent { i, itemCommentBinding, viewHolder, data ->
//                        itemCommentBinding.iconView.setOnClickListener {
//                            gotoUserPage(data.account)
//                        }
//                    }
//                    viewBinding.modCommentRecyclerView.isVisible = true
//                    viewBinding.modCommentRecyclerView.adapter = adapter
//                }
//            }
//
//            override fun onFailure(e: Exception) {
//                viewBinding.commentLinearProgressIndicator.isVisible = false
//                viewBinding.modCommentRecyclerView.isVisible = false
//            }
//
//        })
//    }
//
//    fun loadDeveloperInfo(userId: String) {
//        User.getSpaceInfo(userId, object : ApiCallBack<SpaceInfoData> {
//            override fun onResponse(t: SpaceInfoData) {
//                if (t.code == ServerConfiguration.Success_Code) {
//                    val icon = t.data.headIcon
//                    if (icon != null) {
//                        Glide.with(this@WebModInfoActivity)
//                            .load(ServerConfiguration.getRealLink(icon))
//                            .apply(GlobalMethod.getRequestOptions(true))
//                            .into(viewBinding.headIconView)
//                    }
//                    viewBinding.userNameView.text = t.data.userName
//                    val info = String.format(
//                        getString(R.string.fans_information),
//                        ServerConfiguration.numberToString(t.data.fans),
//                        ServerConfiguration.numberToString(t.data.follower),
//                        ServerConfiguration.numberToString(t.data.praise)
//                    )
//                    viewBinding.userInfoView.text = info
//
//                    viewBinding.cardView.postDelayed({
//                        viewBinding.cardView.isVisible = true
//                        viewBinding.openUserSpace.setOnClickListener {
//                            gotoUserPage(t.data.account)
//                        }
//                    }, 300)
//                }
//// else {
////                    viewBinding.cardView.isVisible = false
////                }
//
//            }
//
//            override fun onFailure(e: Exception) {
////                viewBinding.cardView.isVisible = false
//            }
//
//        })
//    }


    /**
     * 打开用户主页
     * @param userId String
     */
    fun gotoUserPage(userId: String) {
        val intent = Intent(
            this@WebModInfoActivity,
            UserHomePageActivity::class.java
        )
        intent.putExtra("userId", userId)
        startActivity(
            intent
        )
    }

//    private fun initAction() {
//        viewBinding.sendDiscussion.setOnClickListener {
//            val account = AppSettings.getValue(AppSettings.Setting.Account, "")
//            if (account.isBlank()) {
//                showError(getString(R.string.please_login_first))
//                return@setOnClickListener
//            }
//
//
//            CommentDialog(this).setCancelable(false)
//                .setSubmitFun { button, textInputLayout, s, alertDialog ->
//                    button.isEnabled = false
//                    WebMod.instance.sendComment(
//                        AppSettings.getValue(AppSettings.Setting.Token, ""),
//                        modId,
//                        s,
//                        object : ApiCallBack<ApiResponse> {
//                            override fun onResponse(t: ApiResponse) {
//                                if (t.code == ServerConfiguration.Success_Code) {
//                                    alertDialog.dismiss()
//                                    loadModCommentList(modId)
//                                    Snackbar.make(
//                                        viewBinding.button,
//                                        R.string.release_ok,
//                                        Snackbar.LENGTH_SHORT
//                                    ).show()
//                                } else {
//                                    textInputLayout.error = t.message
//                                }
//                            }
//
//                            override fun onFailure(e: Exception) {
//                                textInputLayout.error = e.toString()
//                            }
//
//                        })
//                }.show()
//        }
//
//    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityWebModInfoBinding {
        return ActivityWebModInfoBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            initView()
//            initData()
//            initAction()
        }
    }
}