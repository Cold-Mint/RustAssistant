package com.coldmint.rust.pro.edit.autoComplete

import android.os.Bundle
import com.coldmint.rust.pro.edit.RustAutoComplete
import com.coldmint.rust.pro.edit.RustCompletionItem
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference


/**
 * 自动完成工作接口
 */
interface AutoCompleteJob {


    /**
     * 获取工作名称
     */
    fun getName(): String


    /**
     * 该任务是否需要执行（返回true则执行）
     * @param contentReference ContentReference
     * @param charPosition CharPosition
     * @return String
     */
    fun needPerform(
        contentReference: ContentReference,
        charPosition: CharPosition
    ): Boolean


    /**
     * 处理空白关键字，或者行内容响应
     * 即不输入任何内容执行的操作
     * @return Boolean 返回true即响应，返回false反之
     */
    fun respondingEmptyKeyword(
        contentReference: ContentReference,
        charPosition: CharPosition,
        completionPublisher: CompletionPublisher, lineData: String
    )

    /**
     * 请求自动完成
     * @param contentReference ContentReference
     * @param charPosition CharPosition
     * @param completionPublisher CompletionPublisher
     * @param bundle Bundle
     * @return ArrayList<RustCompletionItem>
     */
    fun requireAutoComplete(
        contentReference: ContentReference,
        charPosition: CharPosition,
        completionPublisher: CompletionPublisher,
        lineData: String,
        keyWord: String
    )

}