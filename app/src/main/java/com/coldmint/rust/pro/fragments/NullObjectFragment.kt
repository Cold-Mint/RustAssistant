package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentNullBinding


//该类的实例是表示集合中单个对象的片段。
class NullObjectFragment : BaseFragment<FragmentNullBinding>() {

    override fun getViewBindingObject(): FragmentNullBinding {
        return FragmentNullBinding.inflate(layoutInflater)
    }

    override fun whenViewCreated(inflater: LayoutInflater, savedInstanceState: Bundle?) {

    }


}