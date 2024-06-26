package com.coldmint.rust.pro

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coldmint.dialog.InputDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.dataBean.user.SpaceInfoData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.core.tool.FileOperator
import com.coldmint.rust.core.web.Community
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.User
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityEditModInfoBinding
import com.coldmint.rust.pro.databinding.ActivityEditUserInfoBinding
import com.coldmint.rust.pro.tool.AppSettings
import com.coldmint.rust.pro.tool.GlobalMethod
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import java.io.File
import java.lang.System.out

class EditUserInfoActivity : BaseActivity<ActivityEditUserInfoBinding>() {
    lateinit var userId: String
    var needIcon = false
    var needCover = false
    var iconLink: String? = null
    var coverLink: String? = null
    var needCleanCache = false
    lateinit var iconCacheFile: File
    private lateinit var item: Array<String>

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            initView()
            initAction()
        }
    }

    private fun initView() {
        title = getText(R.string.editData)
        setReturnButton()
        val thisIntent = intent
        val temUserId = thisIntent.getStringExtra("userId")
        if (temUserId == null) {
            showError("用户id错误。")
            return
        }
        item = resources.getStringArray(R.array.gender_entries)
        userId = temUserId
        User.getSpaceInfo(userId, object : ApiCallBack<SpaceInfoData> {
            override fun onResponse(t: SpaceInfoData) {
                if (t.code == ServerConfiguration.Success_Code) {
                    viewBinding.loadLayout.isVisible = false
                    viewBinding.nestedScrollView.isVisible = true
                    viewBinding.userNameView.setText(t.data.userName)
                    val head = t.data.headIcon
                    if (head != null) {
                        loadIcon(head)
                    }

                    val cover = t.data.cover
                    if (cover != null) {
                        loadCover(cover)
                    }

                    val introduce =
                        t.data.introduce ?: getString(R.string.defaultIntroduced)
                    viewBinding.signatureView.setText(introduce)
                    val gender = t.data.gender
                    if (gender < 0) {
                        viewBinding.sexView.setText(item[1])
                    } else {
                        viewBinding.sexView.setText(item[0])
                    }
                    viewBinding.sexView.setSimpleItems(R.array.gender_entries)
                    viewBinding.button.isVisible = true
                } else {
                    viewBinding.progressBar.isVisible = false
                    viewBinding.tipView.isVisible = true
                    viewBinding.tipView.text = t.message
                }
            }

            override fun onFailure(e: Exception) {
                viewBinding.progressBar.isVisible = false
                viewBinding.tipView.isVisible = true
                viewBinding.tipView.text = getString(R.string.network_error)
            }

        })
    }

    private fun initAction() {
//        viewBinding.changeColorButton.setOnClickListener {
//            ColorPickerDialogBuilder
//                .with(this)
//                .setTitle(getString(R.string.choose_color))
//                .initialColor(Color.WHITE)
//                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
//                .density(12)
//                .setOnColorSelectedListener {
//                    //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
//                }
//                .setPositiveButton(R.string.dialog_ok) { dialog, selectedColor, allColors ->
//                    GlobalMethod.temColor = selectedColor
//                    if (iconLink != null) {
//                        Glide.with(this@EditUserInfoActivity)
//                            .load(iconLink)
//                            .apply(GlobalMethod.getRequestOptions(true, !GlobalMethod.isActive))
//                            .into(viewBinding.iconView)
//                    }
//                }
//                .setNegativeButton(R.string.dialog_cancel) { dialog, which -> }
//                .build()
//                .show()
//        }
        viewBinding.userNameView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val userName = s.toString()
                checkUserName(userName)
            }

        })

        viewBinding.iconView.setOnClickListener {
            val popupMenu = GlobalMethod.createPopMenu(it)
            popupMenu.menu.add(R.string.from_url)
            if (needIcon) {
                popupMenu.menu.add(R.string.change_image)
                popupMenu.menu.add(R.string.del_image)
            } else {
                popupMenu.menu.add(R.string.select_image)
            }
            popupMenu.setOnMenuItemClickListener { item ->
                val title = item.title.toString()
                if (title == getString(R.string.change_image) || title == getString(R.string.select_image)) {
                    //选择文件
                    val startIntent =
                        Intent(this, FileManagerActivity::class.java)
                    val fileBundle = Bundle()
                    fileBundle.putString("type", "selectFile")
                    startIntent.putExtra("data", fileBundle)
                    startActivityForResult(startIntent, 1)
                } else if (title == getString(R.string.from_url)) {
                    InputDialog(this).setTitle(R.string.from_url).setMessage(R.string.from_url_tip)
                        .setErrorTip { s, textInputLayout ->
                            if (s.matches(Regex("^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$|^https://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"))) {
                                textInputLayout.error = getString(R.string.from_url_tip)
                            } else {
                                textInputLayout.isErrorEnabled = false
                            }
                        }.setPositiveButton(R.string.dialog_ok) { input ->
                        needCleanCache = true
                        loadIcon(input)
                        true
                    }.setNegativeButton(R.string.dialog_close) {

                    }.show()
                } else {
                    val link = iconLink
                    if (link != null) {
                        needIcon = false
                    }
                    viewBinding.iconView.setImageResource(R.drawable.image)
                }
                false
            }
            popupMenu.show()
        }

        viewBinding.coverView.setOnClickListener {
            val popupMenu = GlobalMethod.createPopMenu(it)
            popupMenu.menu.add(R.string.from_url)
            if (needCover) {
                popupMenu.menu.add(R.string.change_image)
                popupMenu.menu.add(R.string.del_image)
            } else {
                popupMenu.menu.add(R.string.select_image)
            }
            popupMenu.setOnMenuItemClickListener { item ->
                val title = item.title.toString()
                if (title == getString(R.string.change_image) || title == getString(R.string.select_image)) {
                    //选择文件
                    val startIntent =
                        Intent(this, FileManagerActivity::class.java)
                    val fileBundle = Bundle()
                    fileBundle.putString("type", "selectFile")
                    startIntent.putExtra("data", fileBundle)
                    startActivityForResult(startIntent, 2)
                } else if (title == getString(R.string.from_url)) {
                    InputDialog(this).setTitle(R.string.from_url).setMessage(R.string.from_url_tip)
                        .setErrorTip { s, textInputLayout ->
                            if (s.matches(Regex("^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$|^https://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"))) {
                                textInputLayout.error = getString(R.string.from_url_tip)
                            } else {
                                textInputLayout.isErrorEnabled = false
                            }
                        }.setPositiveButton(R.string.dialog_ok) { input ->
                        needCleanCache = true
                        loadCover(input)
                        true
                    }.setNegativeButton(R.string.dialog_close) {

                    }.show()


                } else {
                    val link = coverLink
                    if (link != null) {
                        needCover = false
                    }
                    viewBinding.coverView.setImageResource(R.drawable.image)
                }
                false
            }
            popupMenu.show()
        }

        viewBinding.signatureView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val introduce = s.toString()
                checkIntroduce(introduce)
            }

        })

        viewBinding.button.setOnClickListener {
            val userName = viewBinding.userNameView.text.toString()
            val introduce = viewBinding.signatureView.text.toString()
            val genderValue = viewBinding.sexView.text.toString()
            val boy = getString(R.string.boy)
            if (!checkUserName(userName)) {
                return@setOnClickListener
            }
            if (!checkIntroduce(introduce)) {
                return@setOnClickListener
            }
            val gender = if (genderValue == boy) {
                1
            } else {
                -1
            }
            viewBinding.button.hide()
            val token =
                AppSettings.getValue(AppSettings.Setting.Token, "")
            User.updateSpaceInfo(
                token,
                userName,
                introduce,
                gender,
                object : ApiCallBack<ApiResponse> {
                    override fun onResponse(t: ApiResponse) {
                        if (t.code == ServerConfiguration.Success_Code) {
                            if (needCleanCache) {
                                Thread {
                                    Glide.get(this@EditUserInfoActivity).clearDiskCache()
                                    runOnUiThread {
                                        Glide.get(this@EditUserInfoActivity).clearMemory()
                                        finish()
                                    }
                                }.start()
                            } else {
                                finish()
                            }
                        } else {
                            viewBinding.button.show()
                            val data = t.data
                            if (data != null && ServerConfiguration.isEvent(data)) {
                                when (data) {
                                    "@event:用户名占用" -> {
                                        setErrorAndInput(
                                            viewBinding.userNameView,
                                            getString(R.string.user_name_error),
                                            viewBinding.userNameInputLayout
                                        )
                                    }
                                }
                            } else {
                                Snackbar.make(
                                    viewBinding.button,
                                    t.message,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(e: Exception) {
                        viewBinding.button.show()
                        showInternetError(viewBinding.button, e)
                    }

                },
                ServerConfiguration.toRelativePath(iconLink ?: ""),
                ServerConfiguration.toRelativePath(coverLink ?: "")
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1) {
                val filePath = data.getStringExtra("File") ?: return
                val newIconFile = File(filePath)
                val type = FileOperator.getFileType(newIconFile)
                if (type == "png" || type == "jpg") {
                    val bitmap = BitmapFactory.decodeFile(newIconFile.absolutePath)
                    if (bitmap == null) {
                        Snackbar.make(
                            viewBinding.button,
                            getString(R.string.parsing_picture_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        return
                    }
                    if (bitmap.height == bitmap.width) {
                        needCleanCache = true
                        loadIcon(filePath)
                    } else {
                        val cacheFolder = File(cacheDir.absolutePath + "/System/Images")
                        if (!cacheFolder.exists()) {
                            cacheFolder.mkdirs()
                        }
                        iconCacheFile = File(cacheFolder.absolutePath + "/" + newIconFile.name)
                        UCrop.of(
                            Uri.parse(newIconFile.toURI().toString()),
                            Uri.parse(iconCacheFile.toURI().toString())
                        ).withAspectRatio(1f, 1f).start(this)
                    }
                } else {
                    Snackbar.make(
                        viewBinding.button,
                        getString(R.string.bad_file_type),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else if (requestCode == 2) {
                val filePath = data.getStringExtra("File") ?: return
                val newIconFile = File(filePath)
                val type = FileOperator.getFileType(newIconFile)
                if (type == "png" || type == "jpg") {
                    needCleanCache = true
                    loadCover(filePath)
                } else {
                    Snackbar.make(
                        viewBinding.button,
                        getString(R.string.bad_file_type),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                //val resultUri = UCrop.getOutput(data)
                needCleanCache = true
                loadIcon(iconCacheFile.absolutePath)
            }
        }
    }

    /**
     * 加载图像
     * @param link String 链接
     */
    fun loadIcon(link: String) {
        needIcon = true
        val temLink = ServerConfiguration.getRealLink(link)
        iconLink = temLink
        Glide.with(this@EditUserInfoActivity)
            .load(temLink).apply(GlobalMethod.getRequestOptions(true, !GlobalMethod.isActive))
            .into(viewBinding.iconView)
    }


    /**
     * 加载封面
     * @param link String 链接
     */
    fun loadCover(link: String) {
        needCover = true
        val temLink = ServerConfiguration.getRealLink(link)
        coverLink = temLink
        Glide.with(this@EditUserInfoActivity)
            .load(temLink).apply(GlobalMethod.getRequestOptions(grayscale = !GlobalMethod.isActive))
            .into(viewBinding.coverView)
    }

    /**
     * 检查用户名
     * @param userName String
     * @return Boolean
     */
    fun checkUserName(userName: String): Boolean {
        return if (userName.isBlank()) {
            setErrorAndInput(
                viewBinding.userNameView,
                String.format(
                    getString(R.string.please_input_value),
                    viewBinding.userNameInputLayout.hint.toString()
                ),
                viewBinding.userNameInputLayout
            )
            false
        } else {
            viewBinding.userNameInputLayout.isErrorEnabled = false
            true
        }
    }


    fun checkIntroduce(introduce: String): Boolean {
        return if (introduce.isBlank()) {
            setErrorAndInput(
                viewBinding.signatureView,
                String.format(
                    getString(R.string.please_input_value),
                    viewBinding.signatureLayout.hint.toString()
                ),
                viewBinding.signatureLayout
            )
            false
        } else {
            viewBinding.signatureLayout.isErrorEnabled = false
            true
        }
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityEditUserInfoBinding {
        return ActivityEditUserInfoBinding.inflate(layoutInflater)
    }


}