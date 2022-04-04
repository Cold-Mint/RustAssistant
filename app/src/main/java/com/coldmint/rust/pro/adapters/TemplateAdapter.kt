package com.coldmint.rust.pro.adapters

import com.coldmint.rust.pro.CreateUnitActivity
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
import com.coldmint.rust.core.TemplatePackage
import com.coldmint.rust.core.tool.FileOperator
import java.io.File

class TemplateAdapter(
    private val mCreateUnitActivity:CreateUnitActivity,
    private val mGroup: List<TemplatePackage>,
    private val mItemList: List<List<File>>,
    mEnvironmentLanguage: String
) : BaseExpandableListAdapter() {
    private val mInflater: LayoutInflater
    private val mEnvironmentLanguage: String

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

        val convertView: View = if (view == null) {
            mInflater.inflate(R.layout.template_group, parent, false)
        } else {
            view
        }
        val nametextView = convertView.findViewById<TextView>(R.id.template_name)
        val numtextView = convertView.findViewById<TextView>(R.id.template_num)
        val templateClass = mGroup[groupPosition]
        nametextView.text = templateClass.getName()
        numtextView.text = String.format(
            mCreateUnitActivity.getText(R.string.template_num).toString(),
            mItemList[groupPosition].size
        )
        return convertView
    }

    //获取子项的view
    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        view: View?,
        parent: ViewGroup
    ): View {
        val convertView: View
        if (view == null) {
            convertView = mInflater.inflate(R.layout.template_item, parent, false)
        } else {
            convertView = view
        }
        val templateClass = mGroup[groupPosition]
        var child: JSONObject? = null
        try {
            val s = FileOperator.readFile(mItemList[groupPosition][childPosition])
            child = JSONObject(s)
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }
        if (child == null) {
            return convertView
        }
        val textView = convertView.findViewById<TextView>(R.id.name_view)
        val imageView = convertView.findViewById<ImageView>(R.id.template_icon_view)
        val rootFolder = templateClass.directest.absolutePath + "/"
        try {
            if (child.has("name_$mEnvironmentLanguage")) {
                textView.text = child.getString("name_$mEnvironmentLanguage")
            } else {
                textView.text = child.getString("name")
            }
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }
        if (child.has("icon")) {
            try {
                val iconFile = File(rootFolder + child.getString("icon"))
                if (iconFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(iconFile.absolutePath)
                    imageView.setImageBitmap(bitmap)
                }
            } catch (exception: JSONException) {
                exception.printStackTrace()
            }
        }
        /*String modIcon = null;
        String globalIcon = templateClass.getIconPath();
        if (!globalIcon.isEmpty()) {
            File file = new File(rootFolder + globalIcon);
            if (file.exists()) {
                modIcon = file.getAbsolutePath();
            }
        }

        String icon = null;
        try {
            icon = child.getString("icon");
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        if (!icon.isEmpty()) {
            File file = new File(rootFolder + icon);
            if (file.exists()) {
                modIcon = file.getAbsolutePath();
            }
        }

        if (modIcon != null) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(modIcon));
        }*/
        val finalChild: JSONObject = child
        convertView.setOnClickListener {
            val intent = Intent(mCreateUnitActivity, TemplateParserActivity::class.java)
            val bundle = Bundle()
            bundle.putString("path", mCreateUnitActivity.getmCreatePath())
            bundle.putString("rootFolder", rootFolder)
            bundle.putString("json", finalChild.toString())
            bundle.putString("templatePath", templateClass.directest.absolutePath)
            intent.putExtra("data", bundle)
            mCreateUnitActivity.startActivityForResult(intent, 2)
        }
        return convertView
    }

    //子项是否可选中,如果要设置子项的点击事件,需要返回true
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    init {
        mInflater = LayoutInflater.from(mCreateUnitActivity)
        this.mEnvironmentLanguage = mEnvironmentLanguage
    }
}