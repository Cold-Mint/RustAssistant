package com.coldmint.rust.pro.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.coldmint.rust.pro.tool.AppSettings

abstract class BaseFragment<T : ViewBinding> : Fragment() {
    protected lateinit var appSettings: AppSettings

    val viewBinding: T by lazy {
        getViewBindingObject()
    }

    /**
     * 显示Toast
     * @param text String
     */
    fun showToast(text: () -> String) {
        Toast.makeText(requireContext(), text.invoke(), Toast.LENGTH_SHORT).show()
    }


    /**
     * 当视图创建完毕时
     * @param inflater LayoutInflater
     * @param savedInstanceState Bundle?
     */
    abstract fun whenViewCreated(
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    )


    /**
     * 获取视图绑定对象
     */
    abstract fun getViewBindingObject(): T


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        appSettings = AppSettings.getInstance(requireContext())
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        whenViewCreated(layoutInflater, savedInstanceState)
    }
}