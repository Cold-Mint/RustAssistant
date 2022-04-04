package com.coldmint.rust.core.interfaces

interface CodeTranslatorListener {
    fun beforeTranslate()
    fun onTranslateComplete(code: String)
}