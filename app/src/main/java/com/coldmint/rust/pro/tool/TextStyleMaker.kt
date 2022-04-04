package com.coldmint.rust.pro.tool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.tool.LineParser
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.*


class TextStyleMaker private constructor() {
    companion object {
        val instance: TextStyleMaker by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TextStyleMaker()
        }
    }

    /**
     * 生成可点击的字体
     * @param text String
     * @param funOnClick Function1<String, Unit>
     * @return SpannableString
     */
    private fun generate(text: String, funOnClick: (String, String) -> Unit): SpannableString {
        val spannableString = SpannableString(text)
        val start = "@"
        val start2 = "{"
        val start3 = "}"
        var startIndex = text.indexOf(start)
        while (startIndex > -1) {
            val start2Index = text.indexOf(start2, startIndex)
            if (start2Index > -1) {
                val start3Index = text.indexOf(start3, start2Index + start2.length)
                if (start3Index > -1) {
                    val num1 = startIndex
                    val num2 = start2Index
                    val num3 = start3Index
                    spannableString.setSpan(
                        object : ClickableSpan() {
                            override fun onClick(p0: View) {
                                funOnClick.invoke(
                                    text.subSequence(num1 + start.length, num2).toString(),
                                    text.subSequence(num2 + start2.length, num3).toString()
                                )
                            }
                        },
                        startIndex,
                        start3Index + start3.length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                } else {
                    break
                }
            } else {
                break
            }
            startIndex = text.indexOf(start, startIndex + start.length)
        }
        return spannableString
    }

    /**
     * 加载
     * @param textView TextView
     * @param funOnClick Function1<String, Unit>
     */
    fun load(textView: TextView, data: String? = null, funOnClick: (String, String) -> Unit) {
        val text = data ?: textView.text.toString()
        val sp = generate(text, funOnClick)
        textView.text = sp
        textView.movementMethod = LinkMovementMethod()
    }


    /**
     * 获取类型
     * @param string String
     * @return String?
     */
    fun getType(string: String): String? {
        if (string.startsWith("@")) {
            val index = string.indexOf("{")
            if (index > -1) {
                return string.subSequence(1, index).toString()
            }
        }
        return null
    }

    /**
     * 获取数据
     * @param string String
     * @return String?
     */
    fun getData(string: String): String? {
        val index = string.indexOf("{")
        if (index > -1) {
            val index2 = string.indexOf("}", index)
            if (index2 > -1) {
                return string.subSequence(index + 1, index2).toString()
            }
        }
        return null
    }

    /**
     * 默认的点击事件
     * @param context Context
     * @param type String
     * @param data String
     */
    fun clickEvent(context: Context, type: String, data: String) {
        when (type) {
            "mod" -> {
                val bundle = Bundle()
                bundle.putString("modId", data)
                val intent = Intent(
                    context,
                    WebModInfoActivity::class.java
                )
                intent.putExtra("data", bundle)
                context.startActivity(intent)
            }
            "user" -> {
                val intent = Intent(
                    context,
                    UserHomePageActivity::class.java
                )
                intent.putExtra("userId", data)
                context.startActivity(
                    intent
                )
            }
            "tag" -> {
                val bundle = Bundle()
                bundle.putString("tag", data)
                bundle.putString(
                    "title",
                    String.format(context.getString(R.string.tag_title), data)
                )
                bundle.putString("action", "tag")
                val thisIntent =
                    Intent(context, TagActivity::class.java)
                thisIntent.putExtra("data", bundle)
                context.startActivity(thisIntent)
            }
            "link" -> {
                val thisIntent = Intent(context,BrowserActivity::class.java)
                thisIntent.putExtra("link",data)
                context.startActivity(thisIntent)
//                AppOperator.useBrowserAccessWebPage(context, data)
            }
            "qqGroup" -> {
                try {
                    AppOperator.openQQGroupCard(context, data.toInt())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        e.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            "activate" -> {
                val time = ServerConfiguration.toStringTime(
                    AppSettings.getInstance(context)
                        .getValue(AppSettings.Setting.ExpirationTime, 0.toLong())
                )
                if (time == ServerConfiguration.ForeverTime) {
                    Toast.makeText(
                        context,
                        R.string.can_t_activate,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val intent = Intent(context, ActivateActivity::class.java)
                    context.startActivity(intent)
                }
            }
            else -> {
                Toast.makeText(
                    context,
                    String.format(context.getString(R.string.unknown_type), type),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}