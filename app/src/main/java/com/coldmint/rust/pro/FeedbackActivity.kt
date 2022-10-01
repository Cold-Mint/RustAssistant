package com.coldmint.rust.pro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityFeedbackBinding

class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>() {

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        setReturnButton()
        title = getString(R.string.feedback)
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(layoutInflater)
    }
}