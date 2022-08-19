package com.coldmint.rust.pro.edit

import android.os.Bundle
import com.coldmint.rust.core.tool.DebugHelper
import com.coldmint.rust.pro.edit.autoComplete.AutoCompleteJob
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference

/**
 * 自动完成信息提供者
 */
class RustAutoCompleteProvider {

    private val key = "自动完成提供者"
    private val jobList = ArrayList<AutoCompleteJob>()
    companion object{
        var keyWord = ""
    }

    /**
     * 添加任务
     * @param job AutoCompleteJob
     */
    fun addJob(job: AutoCompleteJob): AutoCompleteJob {
        jobList.add(job)
        DebugHelper.printLog(key, "添加了新任务${job.getName()}")
        return job
    }


    /**
     * 移除任务
     */
    fun removeJob(job: AutoCompleteJob): AutoCompleteJob {
        jobList.remove(job)
        DebugHelper.printLog(key, "移除了任务${job.getName()}")
        return job
    }


    /**
     * 请求自动完成
     * @param contentReference ContentReference
     * @param charPosition CharPosition
     * @param completionPublisher CompletionPublisher
     * @param bundle Bundle
     */
    fun requireAutoComplete(
        contentReference: ContentReference,
        charPosition: CharPosition,
        completionPublisher: CompletionPublisher,
        bundle: Bundle
    ) {
        val requireKey = "请求自动完成"
        if (jobList.isEmpty()) {
            DebugHelper.printLog(key, "没有任务可执行。", requireKey, true)
        } else {
            //行内容
            val lineData = contentReference.getLine(charPosition.getLine())
            if (lineData.isBlank()) {
                DebugHelper.printLog(requireKey, "行内容为空，无需提示。")
                return
            }
            //光标前内容
            val cursorPrefix = lineData.subSequence(0, charPosition.getColumn()).toString()
            val symbolIndex = cursorPrefix.lastIndexOf(':')
            keyWord = if (symbolIndex > 0) {
                //有冒号
                cursorPrefix.substring(symbolIndex + 1)
            } else {
                //无
                cursorPrefix
            }
            var executeNumber = 0
            jobList.forEach {
                //如果需要执行
                if (it.needPerform(contentReference, charPosition)) {
                    executeNumber++
                    it.requireAutoComplete(
                        contentReference,
                        charPosition,
                        completionPublisher,
                        lineData, keyWord
                    )
                }
            }
            DebugHelper.printLog(key, "执行了${executeNumber}个任务。", requireKey)
        }
    }


}