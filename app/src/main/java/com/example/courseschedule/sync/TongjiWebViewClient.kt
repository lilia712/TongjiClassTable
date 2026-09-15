package com.example.courseschedule.sync

import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "TongjiSync"

class TongjiWebViewClient(
    private val activity: SyncActivity
) : WebViewClient() {

    var started = false

    val capturedParams = ConcurrentHashMap<String, String>()
    val capturedHeaders = ConcurrentHashMap<String, String>()

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null

        if (url.contains("findStudentTimetab") || url.contains("queryAttendClassContent") || url.contains("queryHaveClassDate")) {
            Log.d(TAG, "=== API request: ${request.method} $url")
            request.requestHeaders.forEach { (name, value) ->
                Log.d(TAG, "  header → $name: $value")
            }

            val uri = Uri.parse(url)
            for (name in uri.queryParameterNames) {
                val value = uri.getQueryParameter(name)
                if (!value.isNullOrBlank()) {
                    capturedParams[name] = value
                }
            }

            request.requestHeaders.forEach { (name, value) ->
                capturedHeaders[name.lowercase()] = value
            }

            Log.d(TAG, "Captured params: $capturedParams")
            Log.d(TAG, "Captured headers: $capturedHeaders")
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        if (url?.contains("GraduateStudentTimeTable") == true && !started) {
            started = true
            Log.d(TAG, "Page loaded. Params=$capturedParams, Headers=$capturedHeaders")
        }
    }
}