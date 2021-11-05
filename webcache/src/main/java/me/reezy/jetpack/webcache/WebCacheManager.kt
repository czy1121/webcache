package me.reezy.jetpack.webcache

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse

object WebCacheManager {

    // js/css
    private lateinit var bundle: BundleResource

    // html
    private lateinit var asset: AssetResource

    //
    private lateinit var web: WebResource

    fun init(context: Context, cacheDir: String = "okhttp-web", cacheMaxSize: Long = 100 * 1024 * 1024) {

        val httpClient = context.createOkHttpClient(cacheDir, cacheMaxSize)



        bundle = BundleResource(context, httpClient)
        asset = AssetResource(context)
        web = WebResource(httpClient)
    }


    fun loadBundles(localItems: Map<String, String>) {
        bundle.load(localItems)
    }

    fun loadBundles(items: List<BundleItem> = listOf()) {
        bundle.load(items)
    }

    fun addAssetPath(baseUrl: String, path: String) {
        asset.add(baseUrl, path)
    }
    fun addAssetPaths(mappers: Map<String, String>) {
        mappers.forEach { (baseUrl, path) ->
            asset.add(baseUrl, path)
        }
    }

    fun addCacheUrls(vararg baseUrls: String) {
        web.add(*baseUrls)
    }


    fun get(request: WebResourceRequest): WebResourceResponse? {
        asset.intercept(request)?.let {
            return it
        }
        bundle.intercept(request)?.let {
            return it
        }
        web.intercept(request)?.let {
            return it
        }
        return null
    }

}