package com.coldmint.rust.pro

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.ModClass
import com.coldmint.rust.core.SourceFile
import com.coldmint.rust.core.turret.TurretManager
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityTurretDesignBinding
import com.coldmint.rust.pro.fragments.EditTurretInfoFragment
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar
import java.io.File

/**
 * 炮塔设计
 */
class TurretDesignActivity : BaseActivity<ActivityTurretDesignBinding>() {

    private lateinit var turretManager: TurretManager

    @SuppressLint("RestrictedApi")
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
            val mainImage = sourceFile.findResourceFilesFromSection("image", "graphics", false)
            if (mainImage.isNullOrEmpty()) {

                showError(getString(R.string.please_set_main_image))
                return
            } else {
                val file = mainImage[0]
                if (!file.exists()){
                    showError(getString(R.string.file_not_exist))
                    return
                }
                viewBinding.turretSketchpadView.coordinateColor = GlobalMethod.getColorPrimary(this)
                viewBinding.turretSketchpadView.setImage(file.absolutePath)
                turretManager = sourceFile.getTurretManager()
                turretManager.installerAllTurret(
                    viewBinding.frameLayout,
                    viewBinding.turretSketchpadView
                )
                turretManager
                    .setCoordinateChangeListener { gameCoordinateData, turretData ->
                        viewBinding.infoView.text =
                            "x:${gameCoordinateData.x} y:${gameCoordinateData.y}"
                    }
                //设置自动完成
                viewBinding.autoCompleteText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        turretManager.useTurret(s.toString())
                    }

                })
                val nameList = ArrayList<String>()
                var isFirst = true
                turretManager.turretList.forEach {
                    if (isFirst) {
                        viewBinding.autoCompleteText.setText(it.name)
                        isFirst = false
                    }
                    nameList.add(it.name)
                }
                viewBinding.autoCompleteText.setSimpleItems(nameList.toTypedArray())


            }

            viewBinding.button.setOnClickListener {
                val data = turretManager.getTurretView(viewBinding.autoCompleteText.text.toString())
                if (data != null) {
                    val editTurretInfoFragment = EditTurretInfoFragment(data)
                    editTurretInfoFragment.show(supportFragmentManager, "Edit")
                } else {
                    Toast.makeText(this, R.string.not_find_turret, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityTurretDesignBinding {
        return ActivityTurretDesignBinding.inflate(layoutInflater)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_turret_design, menu)
        val item = menu.findItem(R.id.display_coordinate_system)
        val suggestedDisplay = viewBinding.turretSketchpadView.isSuggestedDisplayCoordinateSystem()
        if (!suggestedDisplay) {
            //如果不建议显示,且正在显示那么提示用户
            if (viewBinding.turretSketchpadView.drawCoordinate) {
                CoreDialog(this).setTitle(R.string.turret_design)
                    .setMessage(R.string.automatically_disable_coordinate_system)
                    .setPositiveButton(R.string.dialog_ok) {

                    }.show()
                viewBinding.turretSketchpadView.drawCoordinate = false
            }
            item.isEnabled = false
            item.isChecked = false
        }
        return true
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            saveData()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 保存数据
     */
    fun saveData() {
        turretManager.saveChange()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.display_coordinate_system -> {
                viewBinding.turretSketchpadView.drawCoordinate =
                    !viewBinding.turretSketchpadView.drawCoordinate
                item.isChecked = viewBinding.turretSketchpadView.drawCoordinate
            }
            R.id.show_guides -> {
                viewBinding.turretSketchpadView.drawAuxiliaryLine =
                    !viewBinding.turretSketchpadView.drawAuxiliaryLine
                item.isChecked = viewBinding.turretSketchpadView.drawAuxiliaryLine
            }
            android.R.id.home -> {
                saveData()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}