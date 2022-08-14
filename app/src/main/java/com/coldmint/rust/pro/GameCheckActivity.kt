package com.coldmint.rust.pro


import android.os.Bundle
import android.view.LayoutInflater
import com.coldmint.rust.pro.base.BaseActivity
import android.view.View
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.pro.databinding.ActivityGameCheckBinding
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar
import com.coldmint.rust.pro.tool.AppSettings

/**
 * 游戏检查处理
 */
class GameCheckActivity : BaseActivity<ActivityGameCheckBinding>() {

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityGameCheckBinding {
        return ActivityGameCheckBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            setTitle(R.string.game_configured)
            viewBinding.startButton.setOnClickListener(View.OnClickListener {
                if (AppOperator.isAppInstalled(
                        this@GameCheckActivity,
                        GlobalMethod.DEFAULT_GAME_PACKAGE
                    )
                ) {
                    AppOperator.openApp(this@GameCheckActivity, GlobalMethod.DEFAULT_GAME_PACKAGE)
                } else {
                    Snackbar.make(
                        viewBinding.startButton,
                        R.string.no_game_installed,
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                }
            })
            viewBinding.completionButton.setOnClickListener(View.OnClickListener {
                AppSettings.setValue(AppSettings.Setting.SetGameStorage, true)
                finish()
            })
        }
    }
}