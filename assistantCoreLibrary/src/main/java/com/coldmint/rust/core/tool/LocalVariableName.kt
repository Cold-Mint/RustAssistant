package com.coldmint.rust.core.tool

/**
 * 本地变量名
 * @property name String
 * @property lineNum Int
 * @constructor
 */
data class LocalVariableName(val name: String, val lineNum: Int) {
    override fun equals(other: Any?): Boolean {
        val localVariableName = other as LocalVariableName
        return name == localVariableName.name
    }

    override fun hashCode(): Int {
        return 0
    }


}