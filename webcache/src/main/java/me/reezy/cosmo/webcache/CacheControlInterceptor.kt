package me.reezy.cosmo.webcache

import okhttp3.Interceptor
import okhttp3.Response

class CacheControlInterceptor(private val maxAge: Int = 7200) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.headers["Cache-Control"] == null) {
            return response.newBuilder().header("Cache-Control", "max-age=$maxAge").build()
        }
        return response
    }
}