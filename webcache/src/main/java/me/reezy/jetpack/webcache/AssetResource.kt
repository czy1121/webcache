package me.reezy.jetpack.webcache

import android.content.Context
import android.content.res.AssetManager
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse

class AssetResource(context: Context) {

    private val assets: AssetManager = context.assets
    private val versionCode = context.getVersionCode()


    private val mappers = mutableMapOf<String, String>()


    fun add(baseUrl: String, path: String) {
        mappers[baseUrl] = path
    }

    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        val uri = request.url
        uri.getQueryParameter("v")?.toLongOrNull()?.let {
            if (versionCode < it) {
                return null
            }
        }

        val url = "${uri.scheme}://${uri.authority}${uri.path ?: ""}"
        mappers.forEach {
            if (url.startsWith(it.key)) {
                val filename = url.substring(it.key.length)
                val exists = assets.list(it.value)?.contains(filename)
                if (exists == true) {
                    val mime = MimeTypeMap.getFileExtensionFromUrl(url)
                    val stream = assets.open(filename)
                    return WebResourceResponse(mime, "", stream)
                }
                return null
            }
        }
        return null
    }

}