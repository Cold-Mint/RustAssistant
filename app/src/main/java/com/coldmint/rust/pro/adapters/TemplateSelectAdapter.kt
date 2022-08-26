package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.coldmint.rust.core.dataBean.template.TemplatePackage
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.ItemTemplateSelectBinding
import com.google.android.material.checkbox.MaterialCheckBox

class TemplateSelectAdapter(context: Context, dataList: MutableList<TemplatePackage>) :
    BaseAdapter<ItemTemplateSelectBinding, TemplatePackage>(context, dataList) {
    private val selectedTemplateList: ArrayList<SelectedTemplate> by lazy {
        ArrayList<SelectedTemplate>()
    }

    private var change: ((Int) -> Unit)? = null


    /**
     * 获取选中的列表
     * @return ArrayList<SelectedTemplate>
     */
    fun getSelectedList() :ArrayList<SelectedTemplate> {
        return selectedTemplateList
    }

    /**
     * 当选择项目数量改变监听
     * @param func Function1<Int, Unit>?
     */
    fun setSelectNumberChanged(func: ((Int) -> Unit)?) {
        change = func
    }

    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemTemplateSelectBinding {
        return ItemTemplateSelectBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: TemplatePackage,
        viewBinding: ItemTemplateSelectBinding,
        viewHolder: ViewHolder<ItemTemplateSelectBinding>,
        position: Int
    ) {
        viewBinding.titleView.text = data.getName()
        viewBinding.titleView.addOnCheckedStateChangedListener { checkBox, state ->
            if (state == MaterialCheckBox.STATE_CHECKED) {
//被选择
                selectedTemplateList.add(SelectedTemplate(data.getPathORId(), data.isLocal()))

            } else {
                selectedTemplateList.remove(SelectedTemplate(data.getPathORId(), data.isLocal()))
            }
            change?.invoke(selectedTemplateList.size)
        }
        change?.invoke(0)

    }


    /**
     * 选中的模板
     * @property pathOrId String
     * @property isLocal Boolean
     * @constructor
     */
    data class SelectedTemplate(val pathOrId: String, val isLocal: Boolean) {

    }

}