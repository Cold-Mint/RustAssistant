package com.coldmint.rust.pro.adapters

import android.app.Activity
import android.content.Context
import android.widget.BaseExpandableListAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.pro.R
import android.widget.TextView
import org.json.JSONObject
import org.json.JSONException
import android.graphics.BitmapFactory
import android.content.Intent
import com.coldmint.rust.pro.TemplateParserActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.dataBean.template.Template
import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.databinding.TemplateGroupBinding
import com.coldmint.rust.pro.databinding.TemplateItemBinding
import java.io.File

class TemplateAdapter(
    private val context: Context,
    private val mGroup: List<TemplatePackage>,
    private val mItemList: List<List<Template>>,
    mEnvironmentLanguage: String
) : BaseExpandableListAdapter() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val mEnvironmentLanguage: String
    private var createPath: String? = null
    private val templateNum by lazy {
        context.getString(R.string.template_num)
    }

    /**
     * 设置创建目录
     * @param path String
     */
    fun setCreatePath(path: String) {
        createPath = path
    }

    //父项的个数
    override fun getGroupCount(): Int {
        return mGroup.size
    }

    //某个父项的子项的个数
    override fun getChildrenCount(groupPosition: Int): Int {
        return mItemList[groupPosition].size
    }

    //获得某个父项
    override fun getGroup(groupPosition: Int): Any {
        return mGroup[groupPosition]
    }

    //获得某个子项
    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return mItemList[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    //获取父项的view
    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        view: View?,
        parent: ViewGroup
    ): View {

        val templateGroupBinding = TemplateGroupBinding.inflate(layoutInflater, parent, false)
        val templateClass = mGroup[groupPosition]
        templateGroupBinding.templateName.text = templateClass.getName()
        templateGroupBinding.templateNum.text = String.format(
            templateNum,
            mItemList[groupPosition].size
        )
        return templateGroupBinding.root
    }

    //获取子项的view
    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        view: View?,
        parent: ViewGroup
    ): View {
        val templateItemBinding = TemplateItemBinding.inflate(layoutInflater, parent, false)
        val templateClass = mItemList[groupPosition][childPosition]
        templateItemBinding.nameView.text = templateClass.getName(mEnvironmentLanguage)
        val icon = templateClass.getIcon()
        if (icon == null) {
            Glide.with(context).load(R.drawable.template).into(templateItemBinding.templateIconView)
        } else {
            Glide.with(context).load(templateClass.getIcon())
                .into(templateItemBinding.templateIconView)
        }
        templateItemBinding.root.setOnClickListener {
            val intent = Intent(context, TemplateParserActivity::class.java)
            intent.putExtra("link", templateClass.getLink())
            intent.putExtra("isLocal", templateClass.isLocal())
            context.startActivity(intent)
        }
        return templateItemBinding.root
    }

    //子项是否可选中,如果要设置子项的点击事件,需要返回true
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    init {
        this.mEnvironmentLanguage = mEnvironmentLanguage
    }
}