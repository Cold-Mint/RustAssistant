package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.UserGroupAdapter
import com.coldmint.rust.pro.databean.UserGroupData
import com.coldmint.rust.pro.databinding.FragmentUserGroupBinding
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 用户群碎片
 * @property viewBinding FragmentUserGroupBinding
 */
class UserGroupFragment : BottomSheetDialogFragment() {

    private lateinit var viewBinding: FragmentUserGroupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentUserGroupBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewBinding.recyclerView.layoutManager = StableLinearLayoutManager(requireContext())
        val dataList = ArrayList<UserGroupData>()
        dataList.add(UserGroupData(R.drawable.ic_qq, R.string.qq_group))
        dataList.add(UserGroupData(R.drawable.ic_discord, R.string.discord_group))
        val adapter = UserGroupAdapter(requireContext(), dataList)
        viewBinding.recyclerView.adapter = adapter
        adapter.setItemEvent { i, itemUserGroupBinding, viewHolder, userGroupData ->
            itemUserGroupBinding.root.setOnClickListener {
                if (userGroupData.titleRes == R.string.qq_group) {
                    AppOperator.useBrowserAccessWebPage(
                        requireContext(),
                        "https://qun.qq.com/qqweb/qunpro/share?_wv=3&_wwv=128&appChannel=share&inviteCode=1W7Dpb0&businessType=9&from=246610&biz=ka"
                    )
                } else if (userGroupData.titleRes == R.string.discord_group) {
                    AppOperator.useBrowserAccessWebPage(
                        requireContext(),
                        "https://discord.gg/DTQDmVdVK3"
                    )
                }
            }
        }

    }
}