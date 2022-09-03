package me.reezy.cosmo.webcache

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import okhttp3.OkHttpClient
import okhttp3.Request

class WebResource(private val httpClient: OkHttpClient) {

    private val baseUrls = mutableSetOf<String>()

    fun add(vararg baseUrls: String) {
        this.baseUrls.addAll(baseUrls)
    }

    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()

        baseUrls.forEach {
            if (url.startsWith(it)) {
                val builder = Request.Builder().url(url)

                request.requestHeaders.map { (name, value) ->
                    builder.addHeader(name, value)
                }

                val response = httpClient.newCall(builder.build()).execute()

                response.body?.let { body ->
                    val mime = body.contentType()?.toString()
                    val stream = body.byteStream()
                    val headers = response.headers.toMap()
                    return WebResourceResponse(mime, "", response.code, response.message, headers, stream)
                }
                return null
            }
        }
        return null
    }

}