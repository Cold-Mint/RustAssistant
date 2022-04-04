package com.coldmint.rust.pro

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.pro.adapters.ThanksAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databean.ThanksDataBean
import com.coldmint.rust.pro.databinding.ActivityThanksBinding

/**
 * @author Cold Mint
 * @date 2022/1/3 19:37
 */
class ThanksActivity : BaseActivity<ActivityThanksBinding>() {

    override fun getViewBindingObject(): ActivityThanksBinding {
        return ActivityThanksBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            viewBinding.toolbar.setTitle(R.string.special_thanks_to)
            setSupportActionBar(viewBinding.toolbar)
            setReturnButton()
            viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
            val list = ArrayList<ThanksDataBean>()
            list.add(ThanksDataBean("空调大郎", "帮助翻译俄语版本。", 1491779490))
            list.add(ThanksDataBean("Ling ASDJ", "制作助手新手模板，已被整合至助手内置模版。跟随助手更新。", 2735951230))
            list.add(ThanksDataBean("Alice's Dream", "帮助薄荷优化代码表。", 3372003670))
            viewBinding.recyclerView.adapter = ThanksAdapter(this, list)

        }
    }

}