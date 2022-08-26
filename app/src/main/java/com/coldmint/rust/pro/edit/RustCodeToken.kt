package com.coldmint.rust.pro.edit

import com.coldmint.rust.core.CodeTranslate

/**
 * Rust代码标记
 * @property offset Int
 * @property codeBlockType CodeBlockType
 */
class RustCodeToken(
    var offset: Int = 0,
    var codeBlockType: CodeTranslate.CodeBlockType = CodeTranslate.CodeBlockType.Note
) {


}