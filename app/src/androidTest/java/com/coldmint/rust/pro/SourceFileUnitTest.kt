package com.coldmint.rust.pro

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coldmint.rust.core.SourceFile
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*


@RunWith(AndroidJUnit4::class)
class SourceFileUnitTest {

    @Test
    fun testWriteValue() {
        val sourceFile = SourceFile("[core]\nname:124")
        //测试修改值
        sourceFile.writeValue("name", "love")
        assertEquals("[core]\nname:love", sourceFile.text)
        //测试修改值2
        sourceFile.text = "[core]\nname:124\nmsg:ni"
        sourceFile.writeValue("msg", "happy")
        sourceFile.writeValue("name", "loveMe")
        assertEquals("[core]\nname:loveMe\nmsg:happy", sourceFile.text)
    }

    @Test
    fun testReadValue() {
        val sourceFile = SourceFile("[core]\n\nname:124\nkey:value2")
        //测试修改值
        val value = sourceFile.readValue("name")
        assertEquals("124", value)
        //测试修改值2
        sourceFile.text = "[core]\nname:124\nmsg:ni"
        val value2 = sourceFile.readValue("msg")
        assertEquals("ni", value2)
    }
}