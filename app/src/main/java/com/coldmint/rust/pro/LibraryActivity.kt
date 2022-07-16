package com.coldmint.rust.pro

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import com.coldmint.rust.pro.base.BaseActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.coldmint.rust.pro.adapters.LibAdapter
import com.coldmint.rust.pro.databean.LibInfo
import com.coldmint.rust.pro.databinding.ActivityLibraryBinding
import com.google.android.material.divider.MaterialDividerItemDecoration
import java.util.ArrayList

class LibraryActivity : BaseActivity<ActivityLibraryBinding>() {

    /**
     * 获取库信息列表
     * @return ArrayList<LibInfo>
     */
    private fun getLibInfoList(): ArrayList<LibInfo> {
        val libInfoArrayList = ArrayList<LibInfo>()
        libInfoArrayList.add(
            LibInfo(
                "Kotlin",
                "A modern programming language that makes developers happier.\n让开发人员更快乐的一门现代编程语言。",
                "https://kotlinlang.org/",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "Banner",
                "Banner 2.0 来了！Android广告图片轮播控件，内部基于ViewPager2实现，Indicator和UI都可以自定义。",
                "https://github.com/youth5201314/banner",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "TinyPinyin",
                "适用于Java和Android的快速、低内存占用的汉字转拼音库。",
                "https://github.com/promeG/TinyPinyin",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "RikkaX",
                "Rikka's Android libraries.",
                "https://github.com/RikkaApps/RikkaX",
                "MIT License"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "uCrop",
                "Image Cropping Library for Android",
                "https://github.com/Yalantis/uCrop",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "Kongzue StackLabel",
                "空祖家的堆叠标签（以下碎念：一开始起名字“StackLabel”没想太多结果被人吐槽Stack是整齐堆叠的意思...........好吧这是我的锅不过现在要改也来不及了，好用就行了...吧？",
                "https://github.com/kongzue/StackLabel",
                "Apache License 2.0"
            )
        )
        val libInfo = LibInfo(
            "sora-editor",
            "A cool code editor library on Android with syntax-highlighting and auto-completion. (aka CodeEditor)",
            "https://github.com/Rosemoe/sora-editor",
            "LGPL 2.1"
        )
        libInfo.tip =
            "铁锈助手使用sora-editor的魔改版本。\n\n由ColdMint魔改自Rose的sora-editor编辑框。\n\n在原基础上新增功能:\n-列表控制api（用于实现连锁提示）\n-自定义列表适配器(用于适配视图)\n\n目前修改版本为0.8，由ColdMint维护。\n\n于2022-1-25日同步主项目。"
        libInfoArrayList.add(libInfo)
        libInfoArrayList.add(
            LibInfo(
                "ColorPicker",
                "color picker for android",
                "https://github.com/QuadFlask/colorpicker",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "OkHttp",
                "Square’s meticulous HTTP client for the JVM, Android, and GraalVM.",
                "https://github.com/square/okhttp",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "Gson",
                "A Java serialization/deserialization library to convert Java Objects into JSON and back",
                "https://github.com/google/gson",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "Glide",
                "An image loading and caching library for Android focused on smooth scrolling",
                "https://github.com/bumptech/glide",
                "BSD, part MIT and Apache 2.0."
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "Glide Transformations",
                "An Android transformation library providing a variety of image transformations for Glide.",
                "https://github.com/wasabeef/glide-transformations",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "CustomActivityOnCrash",
                "Android library that allows launching a custom activity when your app crashes, instead of showing the hated \"Unfortunately, X has stopped\" dialog.",
                "https://github.com/Ereza/CustomActivityOnCrash",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "ImmersionBar",
                "android 4.4以上沉浸式状态栏和沉浸式导航栏管理，适配横竖屏切换、刘海屏、软键盘弹出等问题，可以修改状态栏字体颜色和导航栏图标颜色，以及不可修改字体颜色手机的适配，适用于Activity、Fragment、DialogFragment、Dialog，PopupWindow。",
                "https://github.com/gyf-dev/ImmersionBar",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "PermissionX",
                "An open source Android library that makes handling runtime permissions extremely easy.",
                "https://github.com/guolindev/PermissionX",
                "Apache License 2.0"
            )
        )
        libInfoArrayList.add(
            LibInfo(
                "MultiLanguages",
                "Android 多语种适配框架，兼容高版本，适配第三方库语种",
                "https://github.com/getActivity/MultiLanguages",
                "Apache License 2.0"
            )
        )
//        libInfoArrayList.add(
//            LibInfo(
//                "material-dialogs",
//                "A beautiful, fluid, and extensible dialogs API for Kotlin & Android.",
//                "https://github.com/afollestad/material-dialogs",
//                "Apache License 2.0"
//            )
//        )
        return libInfoArrayList
    }

    override fun getViewBindingObject(): ActivityLibraryBinding {
        return ActivityLibraryBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            title = getString(R.string.libs)
            viewBinding.libsView.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                )
            )
            viewBinding.libsView.layoutManager = LinearLayoutManager(this@LibraryActivity)
            val libAdapter = LibAdapter(this@LibraryActivity, getLibInfoList())
            viewBinding.libsView.adapter = libAdapter
        }
    }
}