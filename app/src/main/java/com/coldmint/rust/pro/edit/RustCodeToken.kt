package com.coldmint.rust.pro.edit

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