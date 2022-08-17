package com.coldmint.rust.pro.edit

import com.coldmint.rust.core.dataBean.CompileConfiguration

/**
 * Rust代码标记
 * @property offset Int
 * @property codeBlockType CodeBlockType
 */
class RustCodeToken(
    var offset: Int = 0,
    var codeBlockType: CompileConfiguration.CodeBlockType = CompileConfiguration.CodeBlockType.Note
) {


}