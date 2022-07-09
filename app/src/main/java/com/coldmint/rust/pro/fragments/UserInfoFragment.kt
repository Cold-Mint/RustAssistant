package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.rust.core.dataBean.user.ActivationInfo
import com.coldmint.rust.core.dataBean.user.UserData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.User
import com.coldmint.rust.pro.*
import com.coldmint.rust.pro.adapters.CommunityServiceAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databean.CommunityServiceInfo
import com.coldmint.rust.pro.databinding.FragmentUserInfoBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod

class UserInfoFragment : BaseFragment<FragmentUserInfoBinding>() {
    lateinit var token: String
    lateinit var account: String

    /**
     * 加载列表
     */
    fun loadRecyclerView(permission: Int) {
        val layoutManager = GridLayoutManager(requireContext(), 4)
        viewBinding.recyclerView.layoutManager = layoutManager
        val dataList = ArrayList<CommunityServiceInfo>()
        dataList.add(CommunityServiceInfo(R.string.work_management, R.drawable.work_management))
//        dataList.add(CommunityServiceInfo(R.string.little_black_house, R.drawable.ban))
//        dataList.add(CommunityServiceInfo(R.string.feedback, R.drawable.feedback))
        dataList.add(CommunityServiceInfo(R.string.fans_management, R.drawable.fans_management))
//        dataList.add(CommunityServiceInfo(R.string.exchange, R.drawable.prize))
        if (permission < 3) {
            //管理员
            dataList.add(CommunityServiceInfo(R.string.report_to_deal, R.drawable.report))
            dataList.add(CommunityServiceInfo(R.string.review_mod, R.drawable.review_mod))
            if (permission == 1) {
                //超级管理员
                dataList.add(CommunityServiceInfo(R.string.order_manager, R.drawable.order_manager))
            }
        }
        val adapter = CommunityServiceAdapter(requireContext(), dataList)
        adapter.setItemEvent { i, itemServiceBinding, viewHolder, communityServiceInfo ->
            itemServiceBinding.root.setOnClickListener {
                when (communityServiceInfo.titleRes) {
                    R.string.work_management -> {
                        val gotoIntent =
                            Intent(requireContext(), WorkManagementActivity::class.java)
                        startActivity(gotoIntent)
                    }
                    R.string.order_manager -> {
                        val sIntent = Intent(requireContext(), OrderListActivity::class.java)
                        sIntent.putExtra("loadAll", true)
                        startActivity(sIntent)
                    }
                    R.string.fans_management -> {
                        openUserList(account, false)
                    }
                    R.string.review_mod -> {
                        val reviewIntent = Intent(requireContext(), ReviewModActivity::class.java)
                        startActivity(reviewIntent)
                    }
                    R.string.report_to_deal -> {
                        val startIntent = Intent(requireContext(), ReportListActivity::class.java)
                        startActivity(startIntent)
                    }
                    else -> {
                        Toast.makeText(context, communityServiceInfo.titleRes, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        viewBinding.recyclerView.adapter = adapter
    }


    /**
     * 打开用户列表
     * @param account String 账号
     * @param isFollowMode Boolean 是否加载偶像
     */
    fun openUserList(account: String, isFollowMode: Boolean) {
        val bundle = Bundle()
        bundle.putString("account", account)
        bundle.putBoolean("isFollowMode", isFollowMode)
        bundle.putBoolean("canRemoveFans", true)
        val intent = Intent(requireContext(), UserListActivity::class.java)
        intent.putExtra("data", bundle)
        startActivity(intent)
    }

    override fun getViewBindingObject(): FragmentUserInfoBinding {
        return FragmentUserInfoBinding.inflate(layoutInflater)
    }


    override fun onResume() {
        super.onResume()

        if (token.isNotBlank()) {
            User.getUserActivationInfo(token, object : ApiCallBack<ActivationInfo> {


                override fun onFailure(e: Exception) {
                    viewBinding.nameView.text = account
                    loadRecyclerView(3)
//                    val localTime =
//                        appSettings.getValue(AppSettings.Setting.ExpirationTime, 0.toLong())
//                    viewBinding.expirationTimeView.text =
//                        ServerConfiguration.toStringTime(localTime)
                }

                override fun onResponse(t: ActivationInfo) {
                    if (t.code == ServerConfiguration.Success_Code) {
                        viewBinding.nameView.text = t.data.userName
                        val icon = t.data.headIcon
                        if (icon != null) {
                            Glide.with(requireContext()).load(ServerConfiguration.getRealLink(icon))
                                .apply(GlobalMethod.getRequestOptions(true))
                                .into(viewBinding.headIconView)
                        }
                        loadRecyclerView(t.data.permission)
                    } else {
                        viewBinding.nameView.text = t.data.userName
                        loadRecyclerView(3)
                    }
                }

            })
        }
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        token = appSettings.getValue(AppSettings.Setting.Token, "")
        account = appSettings.getValue(AppSettings.Setting.Account, "")
        viewBinding.myHomeView.setOnClickListener {
            val intent = Intent(
                requireActivity(),
                UserHomePageActivity::class.java
            )
            intent.putExtra("userId", account)
            startActivity(
                intent
            )
        }

        viewBinding.logOutButton.setOnClickListener {
            appSettings.setValue(AppSettings.Setting.LoginStatus, false)
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
    }
}