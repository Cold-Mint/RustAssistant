package com.coldmint.rust.pro.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.template.LocalTemplateFile
import com.coldmint.rust.core.dataBean.template.Template
import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.FileManagerActivity
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.adapters.TemplateAdapter
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentInstalledTemplateBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.viewmodel.InstalledTemplateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class InstalledTemplateFragment : BaseFragment<FragmentInstalledTemplateBinding>() {
    val viewModel: InstalledTemplateViewModel by lazy {
        InstalledTemplateViewModel()
    }

    private lateinit var startTemplateParserActivity: ActivityResultLauncher<Intent>


    private lateinit var mTemplateAdapter: TemplateAdapter


    /**
     * 设置创建目录
     * @param createPath String
     * @return Boolean 是否设置成功
     */
    fun setCreatePath(createPath: String) {
        viewModel.createPathLiveData.value = createPath
    }

    fun initAction() {
        viewBinding.selectPathButton.setOnClickListener {
            val createPath = viewModel.createPathLiveData.value
            val bundle = Bundle()
            val intent = Intent(requireContext(), FileManagerActivity::class.java)
            bundle.putString("type", "selectDirectents")
            bundle.putString("path", createPath)
            bundle.putString("rootpath", viewModel.mRootPath)
            intent.putExtra("data", bundle)
            startActivityForResult(intent, 1)
        }
        //长按监听
        viewBinding.expandableList.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { parent, view, flatPos, l -> //得到点击的父位置，子位置
                val packedPos = (parent as ExpandableListView).getExpandableListPosition(flatPos)
                val groupPosition = ExpandableListView.getPackedPositionGroup(packedPos)
                val childPosition = ExpandableListView.getPackedPositionChild(packedPos)
                if (childPosition == -1) { //长按的是父项
                    //这里做关于父项的相关操作......
                    val numView = view.findViewById<TextView>(R.id.template_num)
                    val templateClass =
                        mTemplateAdapter.getGroup(groupPosition) as TemplatePackage
                    CoreDialog(requireContext()).setTitle(R.string.template_info)
                        .setMessage(
                            templateClass.getDescription()
                        ).setCancelable(false).setPositiveButton(
                            if (templateClass.isLocal()) {
                                R.string.delete_title
                            } else {
                                R.string.de_subscription
                            }
                        ) {
                            numView.setText(R.string.del_moding)
                            templateClass.delete(
                                AppSettings.getValue(
                                    AppSettings.Setting.Token,
                                    ""
                                )
                            ) {
                                if (it) {
                                    viewModel.loadTemplate(requireContext())
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.delete_error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.setNegativeButton(R.string.dialog_cancel) {

                        }.show()
                } else { //长按的是子项
                    //这里做关于子项的相关操作.......
                }
                true
            }

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadTemplate(requireContext())
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }


    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        initAction()
        startTemplateParserActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    Log.d("启动模板解析器", "收到成功回调，关闭界面。")
                    requireActivity().finish()
                } else {
                    Log.w("启动模板解析器", "未收到有效回调。")
                }
            }
        viewModel.createPathLiveData.observe(this) {
            var relativePath = FileOperator.getRelativePath(
                it,
                AppSettings.getValue(
                    AppSettings.Setting.ModFolder,
                    Environment.getExternalStorageDirectory().absolutePath + "/rustedWarfare/units/"
                )
            )
            if (relativePath == null) {
                relativePath = it
            }
            viewBinding.unitPathView.text = String.format(
                (requireContext().getText(R.string.unit_path) as String),
                relativePath
            )
            if (this::mTemplateAdapter.isInitialized) {
                mTemplateAdapter.setCreatePath(it)
                Log.d("创建目录观察者", "模板适配器设置目录为${it}。")
            } else {
                Log.e("创建目录观察者", "模板适配器没有设置目录。")
            }
        }
        viewModel.setLoadCallBack {
            mTemplateAdapter = TemplateAdapter(
                requireContext(),
                viewModel.getGroupData(),
                viewModel.getItemData(),
                viewModel.environmentLanguage, startTemplateParserActivity
            )
            viewBinding.expandableList.setAdapter(mTemplateAdapter)
            val path = viewModel.createPathLiveData.value.toString()
            mTemplateAdapter.setCreatePath(path)
            Log.d("创建目录观察者", "模板适配器设置目录为${path}。")
        }
        viewModel.loadTemplate(requireContext())
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentInstalledTemplateBinding {
        return FragmentInstalledTemplateBinding.inflate(layoutInflater)
    }
}