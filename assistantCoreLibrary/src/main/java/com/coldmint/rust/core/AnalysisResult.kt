package com.coldmint.rust.core

import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.view.View
import com.coldmint.rust.core.dataBean.CompileConfiguration


/**
 * 代码分析结果
 * @property text String 文本
 * @property icon Drawable 图标
 * @constructor
 */
data class AnalysisResult(
    val lineData: String,
    val errorInfo: String,
    var icon: Drawable? = null,
    var function: ((View) -> Unit)? = null,
    val errorType: CompileConfiguration.ErrorType
)