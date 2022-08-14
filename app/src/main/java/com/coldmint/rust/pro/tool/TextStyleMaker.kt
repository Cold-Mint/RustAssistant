package com.coldmint.rust.pro.tool

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coldmint.rust.core.dataBean.user.IconData
import com.coldmint.rust.core.interfaces.ApiCallBack
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.core.web.User
import com.coldmint.rust.pro.*
import com.google.android.material.chip.ChipDrawable


/**
 * 字体样式制作器
 */
class TextStyleMaker private constructor() {
    companion object {
        val instance: TextStyleMaker by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TextStyleMaker()
        }
    }

//    /**
//     * 生成可点击的字体
//     * @param text String
//     * @param funOnClick Function1<String, Unit>
//     * @return SpannableString
//     */
//    private fun generate(text: String, funOnClick: (String, String) -> Unit): SpannableString {
//        val spannableString = SpannableString(text)
//        val start = "@"
//        val start2 = "{"
//        val start3 = "}"
//        var startIndex = text.indexOf(start)
//        while (startIndex > -1) {
//            val start2Index = text.indexOf(start2, startIndex)
//            if (start2Index > -1) {
//                val start3Index = text.indexOf(start3, start2Index + start2.length)
//                if (start3Index > -1) {
//                    val num1 = startIndex
//                    val num2 = start2Index
//                    val num3 = start3Index
//                    spannableString.setSpan(
//                        object : ClickableSpan() {
//                            override fun onClick(p0: View) {
//                                funOnClick.invoke(
//                                    text.subSequence(num1 + start.length, num2).toString(),
//                                    text.subSequence(num2 + start2.length, num3).toString()
//                                )
//                            }
//                        },
//                        startIndex,
//                        start3Index + start3.length,
//                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//                    )
//                } else {
//                    break
//                }
//            } else {
//                break
//            }
//            startIndex = text.indexOf(start, startIndex + start.length)
//        }
//        return spannableString
//    }

    /**
     * 对文本设置样式，可用于编辑框
     * @param editable Editable
     * @param funOnClick Function2<String, String, Unit>
     */
    fun setStyle(
        spannable: Spannable,
        funOnClick: (String, String) -> Unit,
        context: Context
    ) {
        val start = "@"
        val start2 = "{"
        val start3 = "}"
        var startIndex = spannable.indexOf(start)
        while (startIndex > -1) {
            val start2Index = spannable.indexOf(start2, startIndex)
            if (start2Index > -1) {
                val start3Index = spannable.indexOf(start3, start2Index + start2.length)
                if (start3Index > -1) {
                    val num1 = startIndex
                    val num2 = start2Index
                    val num3 = start3Index
                    val type = spannable.subSequence(num1 + start.length, num2).toString()
                    val data: String = spannable.subSequence(num2 + start2.length, num3).toString()
                    val chipDrawable = ChipDrawable.createFromResource(context, R.xml.chip)
                    chipDrawable.text = data
                    when (type) {
                        "mod" -> {
                            chipDrawable.chipIcon = context.getDrawable(R.drawable.mod)
                        }
                        "user" -> {
                            chipDrawable.chipIcon = context.getDrawable(R.drawable.head_icon)
//                            User.getIcon(data, object : ApiCallBack<IconData> {
//                                override fun onResponse(t: IconData) {
//                                    val data2 = t.data
//                                    if (data2 != null) {
//                                        chipDrawable.text = data2.userName
//
//                                        Glide.with(context)
//                                            .load(ServerConfiguration.getRealLink(data2.headIcon!!))
//                                            .apply(GlobalMethod.getRequestOptions(true))
//                                            .into(
//                                                object : CustomTarget<Drawable>() {
//                                                    override fun onResourceReady(
//                                                        resource: Drawable,
//                                                        transition: Transition<in Drawable>?
//                                                    ) {
//                                                        chipDrawable.chipIcon = resource
//                                                    }
//
//                                                    override fun onLoadCleared(placeholder: Drawable?) {
//
//                                                    }
//
//                                                }
//                                            )
//                                    }
//                                }
//
//                                override fun onFailure(e: Exception) {
//                                }
//
//                            })
                        }
                        "activate"->{
                            chipDrawable.chipIcon =
                                context.getDrawable(R.drawable.store)
                            chipDrawable.text = context.getString(R.string.activate)
                        }
                        "link" -> {
                            chipDrawable.chipIcon =
                                context.getDrawable(R.drawable.ic_baseline_link_24)
                        }
                        else -> {
                            chipDrawable.chipIcon = context.getDrawable(R.drawable.image)
                        }
                    }
                    chipDrawable.closeIcon = null
                    chipDrawable.setBounds(
                        0,
                        0,
                        chipDrawable.intrinsicWidth,
                        chipDrawable.intrinsicHeight
                    )
                    val span = ImageSpan(chipDrawable)
                    spannable.setSpan(
                        span, startIndex,
                        start3Index + start3.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        object : ClickableSpan() {
                            override fun onClick(p0: View) {
                                funOnClick.invoke(
                                    type,
                                    data
                                )
                            }
                        },
                        startIndex,
                        start3Index + start3.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
                    break
                }
            } else {
                break
            }
            startIndex = spannable.indexOf(start, startIndex + start.length)
        }
    }

    /**
     * 加载文本框文本样式，会重新生成对象。
     * @param textView TextView
     * @param funOnClick Function1<String, Unit>
     */
    fun load(textView: TextView, data: String? = null, funOnClick: (String, String) -> Unit) {
        val text = data ?: textView.text
        val spannableString = SpannableString(text)
        setStyle(spannableString, funOnClick, textView.context)
        textView.text = spannableString
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
                val thisIntent = Intent(context, BrowserActivity::class.java)
                thisIntent.putExtra("link", data)
                context.startActivity(thisIntent)
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
                    AppSettings
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