package me.reezy.demo.webcache;

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_web.*
import me.reezy.jetpack.webcache.WebCacheManager


class MainActivity : AppCompatActivity(R.layout.layout_web) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        WebCacheManager.init(this)

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                return WebCacheManager.get(request)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }


            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String?) {
                progress.visibility = View.GONE
            }

        }
        web.webChromeClient = object : WebChromeClient() {

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView, title: String?) {
            }
        }


        initWebSettings(web)


        web.requestFocus()
        web.loadUrl("https://juejin.cn")


    }


    fun initWebSettings(web: WebView) {
        val settings = web.settings
        // 缓存(cache)
        settings.setAppCacheEnabled(false)
        settings.setAppCachePath(web.context.cacheDir.absolutePath)

        // 存储(storage)
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        // 定位(location)
        settings.setGeolocationEnabled(true)

        // 缩放(zoom)
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        // 文件权限
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.allowFileAccessFromFileURLs = false
        settings.allowUniversalAccessFromFileURLs = false

        //
        settings.textZoom = 100

        // 支持Javascript
        settings.javaScriptEnabled = true

        // 支持https
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

//        WebView.setWebContentsDebuggingEnabled(Env.isDebuggable)
        WebView.setWebContentsDebuggingEnabled(true)

        // 页面自适应手机屏幕，支持viewport属性
        settings.useWideViewPort = true
        // 缩放页面，使页面宽度等于WebView宽度
        settings.loadWithOverviewMode = true

        // 是否自动加载图片
        settings.loadsImagesAutomatically = true
        // 禁止加载网络图片
        settings.blockNetworkImage = false
        // 禁止加载所有网络资源
        settings.blockNetworkLoads = false

        // deprecated
        settings.saveFormData = true
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        settings.databasePath = web.context.getDir("database", Context.MODE_PRIVATE).path
        settings.setGeolocationDatabasePath(web.context.filesDir.path)
    }

    override fun onDestroy() {
        web.stopLoading()
        web.clearHistory()
        web.removeAllViews()
        web.destroy()
        val parent = web.parent
        if (parent is ViewGroup) {
            parent.removeView(web)
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

}
