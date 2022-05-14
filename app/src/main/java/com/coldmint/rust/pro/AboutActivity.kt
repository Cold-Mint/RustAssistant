package com.coldmint.rust.pro

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.view.View
import com.coldmint.rust.core.web.ServerConfiguration
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityAboutBinding
import com.coldmint.rust.pro.tool.AppSettings
import java.text.SimpleDateFormat

class AboutActivity : BaseActivity<ActivityAboutBinding>() {
   fun initView() {
       try {
           val packageInfo = packageManager.getPackageInfo(packageName, 0)
           viewBinding.versionView.text = "v." + packageInfo.versionName
       } catch (e: PackageManager.NameNotFoundException) {
           e.printStackTrace()
       }
       val year = Integer.valueOf(SimpleDateFormat("yyyy").format(System.currentTimeMillis()))
       val copyright = String.format(getString(R.string.copyright), year)
       viewBinding.copyRightView.text = copyright
       val aboutText = """<h6>起源</h6>
            |<p>2020年我发现了这款游戏《Rusted Warfare》，并开始学习制作简单的模组。</p>
            |
            |<p>因为之前有开发过App的经验，积累了一点技术。导致某天突发奇想，"这个游戏的模组制作好简单，或许可以制作一款App来降低模组的开发门槛。"于是就起手开发1.x版本的铁锈助手了。在某些方面也受到了《铁锈工具》的影响，那时铁圈用的辅助大多数都是《铁锈工具》。中文转换功能很方便。</p>
            |
            |<p>我发现《铁锈工具》使用《iapp》开发的，我最早是玩《iapp》入坑的程序圈的，也对《iapp》了解一点。那时刷到了《结绳》的视频，一看中文编程很有意思，于是就开始学习《结绳》。</p>
            |
            |<p>《结绳》开发的App体积很小，才几kb。运行速度上一点不次于《iapp》开发的App。我那时特别喜欢体积小的App，于是就改用《结绳》开发新的软件。</p>
            |
            |<p>那是正值高三暑假。因为我是个学渣嘛，走的单招，并且受疫情影响，放假时间很长。大概6个月吧！我也不爱出门，憋在家里开发了1.x版本《铁锈助手》。</p>
            |
            |<h6>发展</h6>
            |
            |<p>漫长的假期过了一半，大概3个月吧，我就完成了初版本的开发。然后在QQ，中文网，百度贴吧进行宣传。</p>
            |
            |<p>因为那是常玩《结绳》我日常活跃在"结绳官方1群"，某天看到群友开发的《结绳助手》上架了《酷安》，就开始梦想自己的App有朝一日也可以上架到《酷安》市场。</p>
            |
            |<p>于是开始尝试上传到《酷安》，成为一名开发者。大概整改了2个版本，花了14天左右我的软件经过酷安审核了与大家见面了。</p>
            |
            |<p>将软件上传到《酷安》显得比较正式一点，也是小编们对独立开发者的认可。高兴死了~</p>
            |
            |<h6>转折</h6>
            |
            |<p>要从《哔哩哔哩》投稿视频说起，那时我抱着试一试的心情投稿了第一部助手介绍视频。引来了好多小伙伴的围观。群里也慢慢热闹了起来。<p>
            |
            |<p>建立了2个500人群，后来又合并群。</p>
            |
            |<h6>现在</h6>
            |
            |<p>《铁锈助手》2.0版本，用零零散散的时间开发了1年。开发语言以及开发环境都特别的正式了。</p>
            |<p>环境移到了电脑上使用《Android Studio》开发，开发语言也从java迁移至了Kotlin。</p>
            |<p>越来越正式了，更多的新功能也在慢慢的加入。</p>
            |
            |
        """.trimMargin()
       viewBinding.aboutView.text = Html.fromHtml(aboutText)
       val time = appSettings.getValue(AppSettings.Setting.ExpirationTime, 0.toLong())
       if (time == 0.toLong()) {
           viewBinding.expirationTimeView.text = getString(R.string.please_login_first)
       } else {
           val stringTime = ServerConfiguration.toStringTime(time)
           viewBinding.expirationTimeView.text =
               if (stringTime == ServerConfiguration.ForeverTime) {
                   getString(R.string.forever_time)
               } else {
                   String.format(
                       getString(R.string.expiration_time_tip),
                       stringTime
                   )
               }
       }
    }

    fun initAction(){
        viewBinding.specialThanksTo.setOnClickListener {
            val gotoIntent = Intent(this, ThanksActivity::class.java)
            startActivity(gotoIntent)
        }
        viewBinding.libsView.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@AboutActivity,
                    LibraryActivity::class.java
                )
            )
        })
    }


    override fun getViewBindingObject(): ActivityAboutBinding {
        return ActivityAboutBinding.inflate(layoutInflater)
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            setTitle(R.string.about)
            initView()
            initAction()
        }
    }
}