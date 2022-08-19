package com.coldmint.rust.pro

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import com.afollestad.materialdialogs.utils.MDUtil.getWidthAndHeight
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityCustomizeEditTextBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.yalantis.ucrop.UCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.File


class CustomizeEditTextActivity : BaseActivity<ActivityCustomizeEditTextBinding>() {

    private lateinit var selectFile: ActivityResultLauncher<Intent>
    private var clipPath: String? = null

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        setReturnButton()
        title = getString(R.string.customize_edit_text)
        readData()
        loadAction()

        val enable = AppSettings.getValue(
            AppSettings.Setting.CodeEditBackGroundEnable,
            false
        )
        if (enable) {
            loadImage(
                AppSettings.getValue(AppSettings.Setting.CodeEditBackGroundPath, ""),
                AppSettings.getValue(AppSettings.Setting.BlurTransformationValue, 1)
            )
        }

        selectFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val key = "选择文件"
            if (it.resultCode == RESULT_OK) {
                //选择了文件
                val intent = it.data
                val path = intent?.getStringExtra("File") ?: ""
                val file = File(path)
                if (file.exists()) {
                    val type = FileOperator.getFileType(file)
                    if (type == "png" || type == "jpg") {
                        val bitmap = BitmapFactory.decodeFile(path)
                        val width = windowManager.getWidthAndHeight().first
                        val height = windowManager.getWidthAndHeight().second
                        val imageWidth = bitmap.width
                        val imageHeight = bitmap.height
                        if (width % imageWidth == 0 && height % imageHeight == 0) {
                            //可以被整除
                            DebugHelper.printLog(key, "图像无需裁剪。")
                        } else {
                            clipPath = AppSettings.createNewCodeEditBackGroundFile()
                            val newFile = File(clipPath)
                            DebugHelper.printLog(key, "前往裁剪 输出路径:${newFile.absolutePath}")
                            UCrop.of(
                                Uri.parse(file.toURI().toString()),
                                Uri.parse(newFile.toURI().toString())
                            )
                                .withAspectRatio(width.toFloat(), height.toFloat())
                                .start(this);
                        }
                    } else {
                        DebugHelper.printLog(key, "文件格式不合法", isError = true)
                        Snackbar.make(
                            viewBinding.button,
                            R.string.bad_file_type,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    DebugHelper.printLog(key, "文件不存在", isError = true)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
//            val resultUri = UCrop.getOutput(data!!)
            if (clipPath != null) {
                loadImage(clipPath!!)
            } else {
                Snackbar.make(
                    viewBinding.button,
                    R.string.bad_file_type,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
//            val cropError = UCrop.getError(data!!)
            Snackbar.make(
                viewBinding.button,
                R.string.bad_file_type,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 加载图像
     * @param filePath String
     */
    fun loadImage(filePath: String, value: Int = 1) {
        DebugHelper.printLog("加载文件", "加载了文件${filePath} 模糊度${value}", isError = true)
        Glide.with(this).load(filePath).apply(
            bitmapTransform(
                BlurTransformation(value)
            )
        ).into(viewBinding.imageView)
        viewBinding.filePathInputEdit.setText(filePath)
        viewBinding.imageCardView.isVisible = true
        viewBinding.dimCardView.isVisible = true
        viewBinding.autoGenerateColorSchemeButton.isVisible = true
        viewBinding.slide.value = value.toFloat()
        viewBinding.filePathInputLayout.endIconDrawable =
            getDrawable(R.drawable.ic_outline_clear_24)
    }

    /**
     * 读取数据
     */
    fun readData() {
        //读取内容
        setTextAndColor(
            viewBinding.textColorEditText,
            AppSettings.getValue(AppSettings.Setting.TextColor, "")
        )
        setTextAndColor(
            viewBinding.annotationColorEditText,
            AppSettings.getValue(AppSettings.Setting.AnnotationColor, "")
        )
        setTextAndColor(
            viewBinding.keywordColorEditText,
            AppSettings.getValue(AppSettings.Setting.KeywordColor, "")
        )
        setTextAndColor(
            viewBinding.sectionColorEditText,
            AppSettings.getValue(AppSettings.Setting.SectionColor, "")
        )
    }


    /**
     * 加载事件
     */
    fun loadAction() {
        viewBinding.autoGenerateColorSchemeButton.setOnClickListener {
            val bitmap = BitmapFactory.decodeFile(viewBinding.filePathInputEdit.text.toString())
            Palette.from(bitmap).generate {
                //充满活力的
                setTextAndColor(
                    viewBinding.sectionColorEditText,
                    GlobalMethod.colorToString(it?.getVibrantColor(Color.RED) ?: Color.RED)
                )

                setTextAndColor(
                    viewBinding.annotationColorEditText,
                    GlobalMethod.colorToString(it?.getLightVibrantColor(Color.RED) ?: Color.RED)
                )
                setTextAndColor(
                    viewBinding.textColorEditText,
                    GlobalMethod.colorToString(it?.getDarkMutedColor(Color.RED) ?: Color.RED)
                )
                setTextAndColor(
                    viewBinding.keywordColorEditText,
                    GlobalMethod.colorToString(it?.getMutedColor(Color.RED) ?: Color.RED)
                )

            }
        }
        viewBinding.filePathInputLayout.setEndIconOnClickListener {
            val oldFileData = viewBinding.filePathInputEdit.text.toString()
            if (oldFileData.isBlank()) {
                val intent = Intent(this, FileManagerActivity::class.java)
                val bundle = Bundle()
                bundle.putString("type", "selectFile")
                intent.putExtra("data", bundle)
                selectFile.launch(intent)
            } else {
                viewBinding.filePathInputEdit.setText("")
                viewBinding.filePathInputLayout.endIconDrawable = getDrawable(R.drawable.file)
                viewBinding.imageCardView.isVisible = false
                viewBinding.dimCardView.isVisible = false
                viewBinding.autoGenerateColorSchemeButton.isVisible = false
            }
        }

        viewBinding.slide.addOnChangeListener { slider, value, fromUser ->
            val filePath = viewBinding.filePathInputEdit.text.toString()
            Glide.with(this).load(filePath).apply(
                bitmapTransform(
                    BlurTransformation(value.toInt())
                )
            ).into(viewBinding.imageView)
        }

        viewBinding.keywordColorEditLayout.setEndIconOnClickListener {
            GlobalMethod.showColorPickerDialog(this, true) {
                setTextAndColor(viewBinding.keywordColorEditText, it)
            }
        }


        viewBinding.textColorEditLayout.setEndIconOnClickListener {
            GlobalMethod.showColorPickerDialog(this, true) {
                setTextAndColor(viewBinding.textColorEditText, it)
            }
        }

        viewBinding.sectionColorEditLayout.setEndIconOnClickListener {
            GlobalMethod.showColorPickerDialog(this, true) {
                setTextAndColor(viewBinding.sectionColorEditText, it)
            }
        }


        viewBinding.annotationColorEditLayout.setEndIconOnClickListener {
            GlobalMethod.showColorPickerDialog(this, true) {
                setTextAndColor(viewBinding.annotationColorEditText, it)
            }
        }

        viewBinding.resetButton.setOnClickListener {
            setTextAndColor(viewBinding.keywordColorEditText, "#FF0031C2")
            setTextAndColor(viewBinding.sectionColorEditText, "#FFE10000")
            setTextAndColor(viewBinding.textColorEditText, "#FF000000")
            setTextAndColor(viewBinding.annotationColorEditText, "#FF00AF2C")
        }

        viewBinding.button.setOnClickListener {
            saveData()
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            showSaveDialogIfNeed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun showSaveDialogIfNeed() {
        val needSave = needSave()
        if (needSave) {
            CoreDialog(this).setTitle(R.string.customize_edit_text)
                .setMessage(R.string.save_settings).setPositiveButton(R.string.dialog_ok) {
                    saveData()
                    finish()
                }.setNegativeButton(R.string.dialog_cancel) {
                    finish()
                }.setCancelable(false).show()
        } else {
            finish()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            showSaveDialogIfNeed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 是否需要保存
     * @return Boolean
     */
    fun needSave(): Boolean {
        val textColor = AppSettings.getValue(
            AppSettings.Setting.TextColor,
            ""
        )
        if (viewBinding.textColorEditText.text.toString() != textColor) {
            return true
        }

        val annotationColor = AppSettings.getValue(
            AppSettings.Setting.AnnotationColor,
            ""
        )
        if (viewBinding.annotationColorEditText.text.toString() != annotationColor) {
            return true
        }
        val keywordColor = AppSettings.getValue(
            AppSettings.Setting.KeywordColor,
            ""
        )
        if (viewBinding.keywordColorEditText.text.toString() != keywordColor) {
            return true
        }

        val sectionColor = AppSettings.getValue(
            AppSettings.Setting.SectionColor,
            ""
        )
        if (viewBinding.sectionColorEditText.text.toString() != sectionColor) {
            return true
        }
        val blurTransformationValue = AppSettings.getValue(
            AppSettings.Setting.BlurTransformationValue,
            1
        )
        if (viewBinding.slide.value.toInt() != blurTransformationValue) {
            return true
        }
        val codeEditBackGroundPath =
            AppSettings.getValue(AppSettings.Setting.CodeEditBackGroundPath, "")
        if (viewBinding.filePathInputEdit.text.toString() != codeEditBackGroundPath) {
            return true
        }
        return false
    }

    /**
     * 保存用户数据
     */
    fun saveData() {
        AppSettings.setValue(
            AppSettings.Setting.TextColor,
            viewBinding.textColorEditText.text.toString()
        )
        AppSettings.setValue(
            AppSettings.Setting.AnnotationColor,
            viewBinding.annotationColorEditText.text.toString()
        )
        AppSettings.setValue(
            AppSettings.Setting.KeywordColor,
            viewBinding.keywordColorEditText.text.toString()
        )
        AppSettings.setValue(
            AppSettings.Setting.SectionColor,
            viewBinding.sectionColorEditText.text.toString()
        )
        AppSettings.setValue(
            AppSettings.Setting.BlurTransformationValue,
            viewBinding.slide.value.toInt()
        )
        val filePath = viewBinding.filePathInputEdit.text.toString()
        val enable = if (filePath.isNullOrBlank()) {
            false
        } else {
            val file = File(filePath)
            if (file.exists()) {
                val newPath = AppSettings.createNewCodeEditBackGroundFile()
                val newFile = File(newPath)
                FileOperator.copyFile(file, newFile)
                AppSettings.setValue(AppSettings.Setting.CodeEditBackGroundPath, newPath)
                true
            } else {
                false
            }
        }
        //如果输入了文件路径那么启用
        AppSettings.setValue(
            AppSettings.Setting.CodeEditBackGroundEnable,
            enable
        )

    }

    /**
     * 设置文本和颜色
     * @param textInputEditText TextInputEditText
     */
    fun setTextAndColor(textInputEditText: TextInputEditText, color: String) {
        try {
            textInputEditText.setText(color)
            textInputEditText.setTextColor(Color.parseColor(color))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityCustomizeEditTextBinding {
        return ActivityCustomizeEditTextBinding.inflate(layoutInflater)
    }

}