package com.coldmint.rust.pro


import com.coldmint.rust.pro.base.BaseActivity
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import android.os.Bundle
import android.content.Intent
import org.json.JSONException
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.LocalTemplatePackage
import com.coldmint.rust.core.interfaces.LineParserEvent
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.pro.adapters.AttachFileAdapter
import com.coldmint.rust.pro.adapters.TemplateActionAdapter
import com.coldmint.rust.pro.adapters.TemplateMakerAdapter
import com.coldmint.rust.pro.adapters.TemplateMakerPagerAdapter
import com.coldmint.rust.pro.databean.CodeData

import com.coldmint.rust.pro.databinding.ActivityTemplateMakerBinding
import com.coldmint.rust.pro.databinding.AttachFilesBinding
import com.coldmint.rust.pro.fragments.EditTurretInfoFragment
import com.coldmint.rust.pro.fragments.SaveTemplateFragment
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.viewmodel.TemplateMakerViewModel
import com.google.gson.Gson
import org.json.JSONArray
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

//模板制作器
class TemplateMakerActivity : BaseActivity<ActivityTemplateMakerBinding>() {
    lateinit var templateMakerAdapter: TemplateMakerAdapter
    val viewModel: TemplateMakerViewModel by lazy {
        ViewModelProvider(this).get(TemplateMakerViewModel::class.java)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            title = getString(R.string.template_maker)
            setReturnButton()
            val name = intent.getStringExtra("name")
            if (name == null) {
                showToast("请输入名称")
                finish()
                return
            }
            viewModel.setName(name)
            val local = intent.getBooleanExtra("local", true)
            val path = intent.getStringExtra("path")
            if (path == null) {
                showError("请输入path")
                return
            }
            viewModel.isLocal(local)
            viewModel.setPath(path)
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
            viewModel.getCodeData { jsonArray, list ->
                templateMakerAdapter = TemplateMakerAdapter(this, list)
                if (jsonArray != null) {
                    templateMakerAdapter.setActionArray(jsonArray)
                }
                viewBinding.recyclerView.adapter = templateMakerAdapter

            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_template, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save_action) {
            val action = templateMakerAdapter.getActionArray()
            val json = viewModel.getJsonData()
            json.put("action", action)
            json.put("name", viewModel.getName())
//            CoreDialog(this).setTitle(R.string.template_maker).setMessage(json.toString(4)).show()
            val saveTemplateFragment = SaveTemplateFragment(viewModel.getName(), json)
            saveTemplateFragment.show(supportFragmentManager, "Save")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityTemplateMakerBinding {
        return ActivityTemplateMakerBinding.inflate(layoutInflater)
    }

}