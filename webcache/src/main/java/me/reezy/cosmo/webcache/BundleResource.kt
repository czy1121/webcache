package me.reezy.cosmo.webcache

import android.content.Context
import android.content.res.AssetManager
import android.os.Parcel
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import okhttp3.*
import okio.Buffer
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream



class BundleResource(context: Context, private val httpClient: OkHttpClient) {


    private val assets: AssetManager = context.assets
    private val rootDir: File = context.getExternalFilesDir("webview-bundle") ?: File(context.filesDir, "webview-bundle")

    private val loaded = mutableSetOf<BundleItem>()

    
    fun load(locals: Map<String, String>) {
        load(locals.map { (baseUrl, filename) ->
            assets.open(filename).use {
                val buffer = Buffer()
                buffer.readFrom(it)
                BundleItem(baseUrl, filename, buffer.md5().hex(), true)
            }
        })
    }

    fun load(items: List<BundleItem>) {
        val bundles = mutableListOf<BundleItem>()
        bundles.addAll(getCachedItems())
        bundles.addAll(items)

        val mappers = mutableMapOf<String, BundleItem>()
        bundles.forEach {
            mappers.put(it.baseUrl, it)?.let { old ->
                if (it.hash != old.hash) {
                    File(rootDir, old.hash).deleteRecursively()
                    loaded.remove(old)
                }
            }
        }
        mappers.values.forEach {
            if (File(rootDir, it.hash).exists()) {
                loaded.add(it)
            } else {
                load(it)
            }
        }
    }

    private fun getCachedItems(): Array<BundleItem> {

        val parcel = Parcel.obtain()
        try {
            val bytes = File(rootDir, "data").readBytes()
            parcel.unmarshall(bytes, 0, bytes.size)
            val a = parcel.readValue(BundleItem::class.java.classLoader) as? Array<BundleItem>
            return a ?: emptyArray()
        } catch (ex: Throwable) {

        } finally {
            parcel.recycle()
        }
        return emptyArray()
    }

    private fun saveLoadedItems() {

        val parcel = Parcel.obtain()

        try {
            parcel.writeValue(loaded.toTypedArray())

            val bytes = parcel.marshall()

            File(rootDir, "data").writeBytes(bytes)
        } catch (ex: Throwable) {

        } finally {
            parcel.recycle()
        }
    }

    private fun load(item: BundleItem) {

        if (item.local) {
            assets.open(item.uri).unzip(item)
        } else {
            val request: Request = Request.Builder().url(item.uri).cacheControl(CacheControl.FORCE_NETWORK).build()
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    response.body?.byteStream()?.unzip(item)
                }

                override fun onFailure(call: Call, e: IOException) {

                }
            })
        }
    }

    private fun InputStream.unzip(item: BundleItem) {
        try {
            val dest = File(rootDir, item.hash)
            ZipInputStream(this).use {
                var ze: ZipEntry? = it.nextEntry

                while (ze != null) {
                    if (ze.isDirectory) {
                        File(dest, ze.name).mkdirs()
                    } else {
                        val os = File(dest, ze.name).outputStream()
                        it.copyTo(os)
                        it.closeEntry()
                    }
                    ze = it.nextEntry
                }
            }
            loaded.add(item)
            saveLoadedItems()
        } catch (ex: Throwable) {

        }
    }


    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        val uri = request.url
        val url = "${uri.scheme}://${uri.authority}${uri.path ?: ""}"

        loaded.forEach {
            if (url.startsWith(it.baseUrl)) {
                val filename = url.substring(it.baseUrl.length)
                val file = File(rootDir, "${it.hash}/$filename")
                if (file.exists()) {
                    val mime = MimeTypeMap.getFileExtensionFromUrl(url)
                    val stream = file.inputStream()
                    return WebResourceResponse(mime, "", stream)
                }
                return null
            }
        }
        return null
    }
}