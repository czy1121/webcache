# WebCache
 
WebView 缓存管理，提升网页打开速度。
 
在 WebView 请求网络资源前，它会先依次查询以下资源，没有命中时才从网络加载。

- 资源包，将资源包解压到缓存目录，再从中加载资源
  - 离线资源包，直接打包到APK里
  - 动态资源包，需要动态预加载
- 离线资源，直接从`assets`目录加载资源
- HTTP缓存，使用Okhttp的代替WebView的缓存管理
 
## 使用

```kotlin   

// 初始化
WebCacheManager.init(this)


// 加载离线包，离线包直接打包到APK里，放在`assets`目录，在客户端做映射配置 
// 资源包加载时被解压到缓存目录 
// 请求关联路径下的资源时，会先在缓存目录里寻找对应资源，未命中时再交由WebView请求网络资源
val localBundles = mapOf(
    // 在WebView中请求 http://a.com/local/ 路径下的资源时会先偿试在`assets`目录的 local.zip 中查找
    "http://a.com/local/" to "local.zip"
)
WebCacheManager.loadBundles(localBundles)


// 加载动态资源包，可覆盖离线包，服务端动态下发，在使用前预加载 
// 从给定链接下载资源资源包并解压到缓存目录
// 请求关联路径下的资源时，会先在缓存目录里寻找对应资源，未命中时再交由WebView请求网络资源
val remoteBundles = listOf(
    BundleItem("http://a.com/remote/", "http://a.com/remote.zip", "XXXXXX")
)
WebCacheManager.loadBundles(remoteBundles)


// 离线资源，将给定路径映射到`assets`下的子目录 
// 请求关联路径下的资源时，会先在`assets`下对应的子目录里寻找资源，未命中时再交由WebView请求网络资源
val assetPaths = mapOf(
    // 请求 http://a.com/waoh/ 路径下的资源时会先偿试在`assets/waoh`目录下查找
    "http://a.com/waoh/" to "waoh"
)
WebCacheManager.addAssetPaths(assetPaths)


// 使用Okhttp的代替WebView的请求给定路径下的资源，使用Okhttp的缓存管理
WebCacheManager.addCacheUrls("http://example.com/a/", "http://example.com/b/")


// 拦截资源的请求并返回缓存资源
web.webViewClient = object : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return WebCacheManager.get(request)
    }
}
``` 

## Gradle

``` groovy
repositories { 
    maven { url "https://gitee.com/ezy/repo/raw/android_public/"}
} 
dependencies {
    implementation "me.reezy.jetpack:webcache:0.4.0" 
}
```
 



## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).
