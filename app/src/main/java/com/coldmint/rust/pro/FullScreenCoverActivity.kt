package com.coldmint.rust.pro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.databinding.ActivityFullScreenCoverBinding
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.tool.GlobalMethod
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar

class FullScreenCoverActivity : BaseActivity<ActivityFullScreenCoverBinding>() {

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityFullScreenCoverBinding {
        return ActivityFullScreenCoverBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            val thisIntent = intent
            val link = thisIntent.getStringExtra("iconLink")
            if (link == null) {
                showError("请设置图像")
                return
            }

            Glide.with(this).load(ServerConfiguration.getRealLink(link)).apply(GlobalMethod.getRequestOptions()).into(viewBinding.coverView)
            immersionBar {
                hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR).statusBarDarkFont(true)
            }
            viewBinding.coverView.setOnClickListener { finishAfterTransition() }
        }
    }


}