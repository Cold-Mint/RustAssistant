package com.coldmint.rust.pro.viewmodel

import android.app.Application
import android.text.SpannableString
import androidx.lifecycle.MutableLiveData
import com.coldmint.rust.core.AnalysisResult
import com.coldmint.rust.pro.base.BaseAndroidViewModel

/**
 * @author Cold Mint
 * @date 2022/2/5 14:18
 */
class EditEndViewModel(application: Application) : BaseAndroidViewModel(application) {

    /**
     * 分析结果LiveData
     */
    val analysisResultLiveData: MutableLiveData<List<AnalysisResult>> by lazy {
        MutableLiveData<List<AnalysisResult>>()
    }

    /**
     *加载状态LiveData
     */
    val loadStateLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

}