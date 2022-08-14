package com.coldmint.rust.pro.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.template.LocalTemplateFile
import com.coldmint.rust.core.dataBean.template.Template
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


    var mTemplateAdapter: TemplateAdapter? = null


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
                        mTemplateAdapter!!.getGroup(groupPosition) as LocalTemplatePackage
                    CoreDialog(requireContext()).setTitle(R.string.template_info)
                        .setMessage(
                            templateClass.getInfo()?.description
                                ?: requireContext().getString(R.string.describe)
                        ).setCancelable(false).setPositiveButton(R.string.delete_title) {
                            numView.setText(R.string.del_moding)
                            val handler = Handler(Looper.getMainLooper())
                            val scope = CoroutineScope(Job())
                            scope.launch {
                                FileOperator.delete_files(templateClass.directest)
                                handler.post {
                                    viewModel.loadTemplate(requireContext())
                                }
                            }
                        }.setNegativeButton(R.string.dialog_cancel) {

                        }.show()
                } else { //长按的是子项
                    //这里做关于子项的相关操作.......
                }
                true
            }
    }


    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        initAction()
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
            mTemplateAdapter?.setCreatePath(it)
        }
        viewModel.setLoadCallBack {
            mTemplateAdapter = TemplateAdapter(
                requireContext(),
                viewModel.getGroupData(),
                viewModel.getItemData(),
                viewModel.environmentLanguage
            )
            viewBinding.expandableList.setAdapter(mTemplateAdapter!!)
        }
        viewModel.loadTemplate(requireContext())
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentInstalledTemplateBinding {
        return FragmentInstalledTemplateBinding.inflate(layoutInflater)
    }
}