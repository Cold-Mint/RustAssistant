package com.coldmint.rust.pro.viewmodel

import androidx.lifecycle.MutableLiveData
import com.coldmint.rust.pro.base.BaseViewModel

class WebModDetailsViewModel : BaseViewModel() {

    var modNameLiveData: MutableLiveData<String>? = null
    var modId: String? = null
    var developer: String? = null

    //此模组是否对外开放
    var isOpen: Boolean = false

    var link: String? = null


}