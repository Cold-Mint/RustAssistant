package com.coldmint.rust.pro

import com.coldmint.rust.pro.base.BaseActivity
import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.coldmint.rust.pro.adapters.CreateUnitPageAdapter
import com.coldmint.rust.pro.databinding.ActivityCreateUnitBinding
import com.google.android.material.tabs.TabLayoutMediator

class CreateUnitActivity : BaseActivity<ActivityCreateUnitBinding>() {

    lateinit var createUnitPageAdapter: CreateUnitPageAdapter
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setTitle(R.string.create_unit)
            setReturnButton()
            initView()
        }
    }

    fun initView() {
        val intent = intent
        val bundle = intent.getBundleExtra("data")
        if (bundle == null) {
            Toast.makeText(this, "无效的请求", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            createUnitPageAdapter = CreateUnitPageAdapter(this)
            viewBinding.viewPager2.adapter = createUnitPageAdapter
            TabLayoutMediator(viewBinding.tabLayout, viewBinding.viewPager2) { tab, i ->
                tab.text = if (i == 0) {
                    getString(R.string.installated)
                } else {
                    getString(R.string.template_community)
                }
            }.attach()
            val root = bundle.getString("modPath")
            createUnitPageAdapter.setRootPath(root)
            createUnitPageAdapter.setCreatePath(bundle.getString("createPath", root))
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//                val resultCode = it.resultCode
//                when (requestCode) {
//                    1 -> if (resultCode == RESULT_OK) {
//                        val directents = data!!.getStringExtra("Directents")
//                        if (directents != null) {
//                            createUnitPageAdapter.setCreatePath(directents)
//                        }
//                    }
//                    2 -> if (resultCode == RESULT_OK) {
//                        val path = data!!.getStringExtra("File")
//                        val intent = Intent()
//                        intent.putExtra("File", path)
//                        setResult(RESULT_OK, intent)
//                        finish()
//                    }
//                }
//            }
        }
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityCreateUnitBinding {
        return ActivityCreateUnitBinding.inflate(layoutInflater)
    }


}