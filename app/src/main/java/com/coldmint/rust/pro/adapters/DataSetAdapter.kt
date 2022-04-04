package com.coldmint.rust.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.DataSet
import com.coldmint.rust.pro.base.BaseAdapter
import com.coldmint.rust.pro.databinding.DatabaseItemBinding


class DataSetAdapter(context: Context, dataList: MutableList<DataSet>) :
    BaseAdapter<DatabaseItemBinding, DataSet>(context, dataList) {
    override fun getViewBindingObject(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): DatabaseItemBinding {
        return DatabaseItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBingView(
        data: DataSet,
        viewBinding: DatabaseItemBinding,
        viewHolder: ViewHolder<DatabaseItemBinding>,
        position: Int
    ) {
        val dataBaseManifest = data.getDataBaseManifest()
        viewBinding.databaseNameView.text = data.getDataSetName()
        if (dataBaseManifest != null) {
            viewBinding.databaseIntroduce.text = dataBaseManifest.describe
            viewBinding.databaseOther.text =
                dataBaseManifest.author + "|" + dataBaseManifest.versionName
        }
    }

}