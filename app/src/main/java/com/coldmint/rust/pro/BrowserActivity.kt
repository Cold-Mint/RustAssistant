package com.coldmint.rust.pro

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import com.coldmint.rust.core.tool.AppOperator
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databinding.ActivityBrowserBinding

/**
 * 浏览器活动
 */
class BrowserActivity : BaseActivity<ActivityBrowserBinding>() {
    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        val link = intent.getStringExtra("link")
        if (link == null) {
            showToast("请设置link")
            finish()
            return
        }
        setReturnButton()
        viewBinding.webView.loadUrl(link)
        viewBinding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                viewBinding.linearProgressIndicator.isVisible = true
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                viewBinding.linearProgressIndicator.isVisible = false
                super.onPageFinished(view, url)
            }
        }
        viewBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                supportActionBar?.title = title
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.otherBrowser -> {
                AppOperator.useBrowserAccessWebPage(this, viewBinding.webView.url)
            }
            R.id.refreshWebpage -> {
                viewBinding.webView.reload()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_browser, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && viewBinding.webView.canGoBack()) {
            viewBinding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityBrowserBinding {
        return ActivityBrowserBinding.inflate(layoutInflater)
    }

}