package com.coldmint.rust.pro.fragments

import android.content.Context
import android.view.LayoutInflater
import com.coldmint.dialog.BaseAppDialog
import com.coldmint.rust.core.dataBean.ApiResponse
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.interfaces.FileFinderListener
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.WebMod
import com.coldmint.rust.pro.databinding.DialogInsertCoinsBottomBinding
import com.coldmint.rust.pro.tool.AppSettings

/**
 * 投币对话框
 * @property viewBinding [@androidx.annotation.NonNull] DialogInsertCoinsBottomBinding
 * @constructor
 */
class InsertCoinsDialog(context: Context, val modId: String) :
    BaseAppDialog<InsertCoinsDialog>(context) {

    private val token by lazy {
        AppSettings.getValue(AppSettings.Setting.Token, "")
    }

    private val viewBinding by lazy {
        DialogInsertCoinsBottomBinding.inflate(LayoutInflater.from(context))
    }

    private var callBackLister: ((Boolean) -> Unit)? = null

    /**
     * 设置回调
     * Boolean是否成功
     * @param listener Function1<Boolean, Unit>?
     */
    fun setCallBackListener(listener: ((Boolean) -> Unit)?): InsertCoinsDialog {
        callBackLister = listener
        return this
    }

    init {
        setView(viewBinding.root)
        viewBinding.positiveButton.setOnClickListener {
            val number = viewBinding.slider.value.toInt()
            WebMod.instance.insertCoins(token, modId, number, object : ApiCallBack<ApiResponse> {
                override fun onResponse(t: ApiResponse) {
                    if (t.code == ServerConfiguration.Success_Code) {
                        dismiss()
                        callBackLister?.invoke(true)
                    } else {
                        viewBinding.textview.text = t.message
                        callBackLister?.invoke(false)
                    }
                }

                override fun onFailure(e: Exception) {
                    e.printStackTrace()
                    dismiss()
                    callBackLister?.invoke(false)
                }

            })
        }
        viewBinding.negativeButton.setOnClickListener {
            dismiss()
        }
    }


}