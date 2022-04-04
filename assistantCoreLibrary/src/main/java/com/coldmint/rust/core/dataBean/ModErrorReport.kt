package com.coldmint.rust.core.dataBean

/**
 * 模组错误报告（开发者模式测试使用）
 * @property modPath String
 * @property errorNumber Int
 * @property warningNumber Int
 * @constructor
 */
data class ModErrorReport(
    val modPath: String,
    private var errorNumber: Int = 0,
    private var warningNumber: Int = 0,
    private val errorList: ArrayList<String> = ArrayList()
) {
    /**
     * 添加单位错误报告
     * @param compileConfiguration CompileConfiguration
     */
    fun addFileReport(compileConfiguration: CompileConfiguration) {
        if (compileConfiguration.getErrorNumber() > 0) {
            errorList.add(compileConfiguration.openedSourceFile.file.absolutePath)
        }
        this.errorNumber += compileConfiguration.getErrorNumber()
        this.warningNumber += compileConfiguration.getWarningNumber()
    }
}