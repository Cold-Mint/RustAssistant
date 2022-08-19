package com.coldmint.rust.pro.edit

import android.graphics.drawable.Drawable
import io.github.rosemoe.sora.lang.completion.CompletionItem
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor

/**
 * Rust完成器对象
 * @property label String
 * @constructor
 */
class RustCompletionItem(var title: String) : CompletionItem(title) {


    init {
        label = title
    }

    constructor(title: String, commit: String, subtitle: String) : this(commit) {
        this.title = title
        this.label = commit
        this.subtitle = subtitle
    }

    constructor(title: String, subtitle: String, icon: Drawable?) : this(title) {
        this.title = title
        this.label = title
        this.subtitle = subtitle
    }

    constructor(title: String, commit: String, sub: String, icon: Drawable?) : this(commit) {
        this.title = title
        this.label = commit
        this.subtitle = sub
        this.icon = icon
    }


    var cursorOffset: Int = 0
    var subtitle: String? = null


    /**
     * 执行完成
     * @param p0 CodeEditor
     * @param p1 Content
     * @param p2 Int
     * @param p3 Int
     */
    //CodeEditor editor, Content text, int line, int column
    override fun performCompletion(editor: CodeEditor, text: Content, line: Int, column: Int) {
        if (label == null) {
            return
        }
        val keyOffset = RustAutoCompleteProvider.keyWord.length
        if (keyOffset == 0) {
            text.insert(line, column, label)
        } else {
            text.replace(line, column - keyOffset, line, column, label)
        }
    }
}