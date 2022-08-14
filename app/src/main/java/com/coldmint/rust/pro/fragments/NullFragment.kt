package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentNullBinding


/**
 * 没有内容的碎片
 */
class NullFragment : BaseFragment<FragmentNullBinding>() {

    override fun getViewBindingObject(layoutInflater: LayoutInflater): FragmentNullBinding {
        return FragmentNullBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {

    }


}