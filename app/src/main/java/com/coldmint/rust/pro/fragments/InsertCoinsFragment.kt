package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentInsertCoinsBinding

/**
 * 投币碎片
 */
class InsertCoinsFragment(val modId: String) : BaseFragment<FragmentInsertCoinsBinding>() {
    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {

    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentInsertCoinsBinding {
        return FragmentInsertCoinsBinding.inflate(layoutInflater)
    }


}