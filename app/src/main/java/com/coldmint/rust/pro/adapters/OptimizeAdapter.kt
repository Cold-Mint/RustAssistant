package com.coldmint.rust.pro.adapters


import android.widget.BaseExpandableListAdapter
import android.view.LayoutInflater
import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import com.coldmint.rust.pro.R
import android.widget.TextView
import android.widget.CheckBox
import android.widget.CompoundButton
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import com.coldmint.rust.pro.databean.OptimizeGroup
import com.coldmint.rust.pro.databean.OptimizeItem

class OptimizeAdapter(
    private val mGroup: List<OptimizeGroup>,
    private val mItemList: List<List<OptimizeItem<*>>>,
    context: Context?
) : BaseExpandableListAdapter() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    //忽略组选中（由代码触发）
    private var groupIgnoreChange = false
    override fun getGroupCount(): Int {
        return mGroup.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return mItemList[groupPosition].size
    }

    override fun getGroup(groupPosition: Int): Any {
        return mGroup[groupPosition]
    }

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

    @SuppressLint("StringFormatMatches", "StringFormatInvalid")
    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        view: View?,
        parent: ViewGroup
    ): View {
        var convertView: View
        if (view == null) {
            convertView = mInflater.inflate(R.layout.optimization_group, parent, false)
        } else {
            convertView = view
        }
        val nameView = convertView.findViewById<TextView>(R.id.nameView)
        val numView = convertView.findViewById<TextView>(R.id.numView)
        val groupCheckBox = convertView.findViewById<CheckBox>(R.id.groupCheckBox)
        val optimizationGroup = mGroup[groupPosition]
        nameView.text = optimizationGroup.groupName
        val optimizeItemList = mItemList[groupPosition]
        numView.text =
            String.format(convertView.context.getString(R.string.filenum), optimizeItemList.size)
        groupCheckBox.isChecked = optimizationGroup.isEnabled
        groupCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (groupIgnoreChange) {
                groupIgnoreChange = false
                return@OnCheckedChangeListener
            }
            val thisOptimizeGroup = mGroup[groupPosition]
            thisOptimizeGroup.isEnabled = isChecked
            for (item in optimizeItemList) {
                item.isEnabled = isChecked
            }
            notifyDataSetChanged()
        })
        return convertView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        view: View?,
        parent: ViewGroup
    ): View {
        var convertView: View
        if (view == null) {
            convertView = mInflater.inflate(R.layout.optimization_item, parent, false)
        } else {
            convertView = view
        }
        val nameView = convertView.findViewById<TextView>(R.id.nameView)
        val descriptionView = convertView.findViewById<TextView>(R.id.descriptionView)
        val itemCheckBox = convertView.findViewById<CheckBox>(R.id.itemCheckBox)
        val optimizeItemList = mItemList[groupPosition]
        val optimizationItem = mItemList[groupPosition][childPosition]
        nameView.text = optimizationItem.name
        val description = optimizationItem.description
        if (description == null) {
            descriptionView.isVisible = false
        } else {
            descriptionView.movementMethod = LinkMovementMethod.getInstance()
            descriptionView.text = description
        }
        itemCheckBox.isChecked = optimizationItem.isEnabled
        itemCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            optimizationItem.isEnabled = isChecked
            var setGroupChecked = false
            for (item in optimizeItemList) {
                if (item.isEnabled) {
                    //有1个子项目被选择，那么父级组即为选中
                    setGroupChecked = true
                    break
                }
            }
            val optimizeGroup = mGroup[groupPosition]
            if (optimizeGroup.isEnabled != setGroupChecked) {
                groupIgnoreChange = true
                optimizeGroup.isEnabled = setGroupChecked
                notifyDataSetChanged()
            }
        }
        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}