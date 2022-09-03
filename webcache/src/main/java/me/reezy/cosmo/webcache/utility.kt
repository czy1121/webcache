package me.reezy.cosmo.webcache

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.Proxy
import java.util.concurrent.TimeUnit


internal fun Context.isDebuggable(): Boolean = try {
    applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
} catch (e: Exception) {
    e.printStackTrace()
    false
}

internal fun Context.getVersionCode(pkgName: String = packageName): Long {
    if (pkgName.isBlank()) return -1
    return try {
        val info = packageManager.getPackageInfo(pkgName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else info.versionCode.toLong()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        -1
    }
}


internal fun Context.createOkHttpClient(cacheDir: String = "okhttp", cacheMaxSize: Long = 100 * 1024 * 1024): OkHttpClient {
    return OkHttpClient.Builder()
        .proxy(
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !isDebuggable() -> Proxy.NO_PROXY
                else -> null
            }
        )
        .cache(Cache(File(cacheDir, cacheDir), cacheMaxSize))
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addNetworkInterceptor(CacheControlInterceptor())
        .build()
}