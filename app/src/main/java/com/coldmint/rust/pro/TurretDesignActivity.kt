package com.coldmint.rust.pro

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.turret.TurretInstaller
import com.coldmint.rust.core.turret.TurretSketchpadView
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityTurretDesignBinding
import java.io.File

/**
 * 炮塔设计
 */
class TurretDesignActivity : BaseActivity<ActivityTurretDesignBinding>() {


    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            title = getString(R.string.turret_design)
            val modPath = intent.getStringExtra("modPath")
            val filePath = intent.getStringExtra("filePath")

            if (modPath == null) {
                showError("请设置模组路径")
                return
            }

            if (filePath == null) {
                showError("请设置文件路径")
                return
            }

            val modClass = ModClass(File(modPath))
            val sourceFile = SourceFile(File(filePath), modClass)
            val mainImage = sourceFile.findResourceFilesFromSection("image", "graphics", true)
            if (mainImage.isNullOrEmpty()) {
                showError("请设置主体图像")
                return
            } else {
                val file = mainImage[0]
                viewBinding.turretSketchpadView.setImage(file.absolutePath)
//                viewBinding.toolbar.postDelayed({
//                    Toast.makeText(this, "执行", Toast.LENGTH_SHORT).show()
//                    viewBinding.turretsDragView.setBaseImage(file.absolutePath)
//                }, 1000)
//                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
//                viewBinding.mainImageView.setImageBitmap(bitmap)
//                val layoutParams = viewBinding.mainImageView.layoutParams
//                layoutParams.width = bitmap.width * scale
//                layoutParams.height = bitmap.height * scale

            }
//            val sketchpadView = TurretSketchpadView(this)
//            sketchpadView.
//            viewBinding.frameLayout.addView(sketchpadView)
            TurretInstaller.installerAllTurret(viewBinding.frameLayout, sourceFile) {
               val game = viewBinding.turretSketchpadView.toGameCoordinate(it)
//                viewBinding.textView.text = "坐标 x:${game.x} y:${game.y}"
            }
        }
    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityTurretDesignBinding {
        return ActivityTurretDesignBinding.inflate(layoutInflater)
    }

}