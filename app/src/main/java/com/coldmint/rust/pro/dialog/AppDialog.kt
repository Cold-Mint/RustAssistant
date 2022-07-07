package com.coldmint.rust.pro.dialog

import android.app.Dialog
import android.content.Context

/**
 * App对话框接口
 */
interface AppDialog {

    /**
     * 初始化对话框
     * @param context Context
     * @return AppDialog
     */
    fun init(context: Context): AppDialog


    /**
     * 设置标题
     * @param string String
     * @return AppDialog
     */
    fun setTitle(string: String): AppDialog
    fun setTitle(stringRes: Int): AppDialog


    /**
     * 设置消息
     * @param stringRes Int
     * @return AppDialog
     */
    fun setMessage(stringRes: Int): AppDialog
    fun setMessage(string: String): AppDialog

    /**
     * 显示
     * @return AppDialog
     */
    fun show(): AppDialog

    /**
     * 设置积极按钮
     * @param text String
     * @param func Function0<Unit>
     * @return AppDialog
     */
    fun setPositiveButton(text: String, func: () -> Unit): AppDialog
    fun setPositiveButton(textRes: Int, func: () -> Unit): AppDialog


    /**
     * 设置否定按钮
     * @param text String
     * @param func Function0<Unit>
     * @return AppDialog
     */
    fun setNegativeButton(text: String, func: () -> Unit): AppDialog
    fun setNegativeButton(textRes: Int, func: () -> Unit): AppDialog


    /**
     * 设置中立按钮
     * @param text String
     * @param func Function0<Unit>
     * @return AppDialog
     */
    @Deprecated("不建议设置中立按钮")
    fun setNeutralButton(text: String, func: () -> Unit): AppDialog

    @Deprecated("不建议设置中立按钮")
    fun setNeutralButton(textRes: Int, func: () -> Unit): AppDialog

    /**
     * 设置是否可以取消
     * @param cancelable Boolean
     * @return AppDialog
     */
    fun setCancelable(cancelable: Boolean): AppDialog

}