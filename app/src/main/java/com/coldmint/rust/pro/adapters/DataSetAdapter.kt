package com.coldmint.rust.pro.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.coldmint.rust.core.DataSet
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.pro.R
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
        val appVersion = AppOperator.getAppVersionNum(context)
        if (dataBaseManifest == null) {
            viewBinding.databaseIntroduce.text = context.getString(R.string.dataset_not_exist)
            viewBinding.databaseOther.text = ""
            viewBinding.databaseUse.isEnabled = false
        } else {
            Log.d(
                "DataSetAdapter",
                "appVersion: ${appVersion} name: ${data.getDataSetName()} minVersion: ${dataBaseManifest.minAppVersion}"
            )
            if (dataBaseManifest.minAppVersion <= appVersion) {
                viewBinding.databaseIntroduce.text = dataBaseManifest.describe
                viewBinding.databaseOther.text =
                    dataBaseManifest.author + "|" + dataBaseManifest.versionName
                viewBinding.databaseUse.isEnabled = true
            } else {
                viewBinding.databaseUse.isEnabled = false
                viewBinding.databaseIntroduce.text =
                    context.getString(R.string.dataset_minversion_low)
                viewBinding.databaseOther.text = ""
            }
        }
    }

}