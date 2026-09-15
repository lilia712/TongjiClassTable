package com.example.courseschedule.sync

import android.graphics.Color
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.R

class SyncActivity : ComponentActivity(), TongjiSyncManager.Listener {

    lateinit var webViewClient: TongjiWebViewClient
        private set

    private lateinit var webView: WebView
    private lateinit var syncButton: Button

    private val syncManager: TongjiSyncManager by lazy {
        TongjiSyncManager(this, webView).apply {
            listener = this@SyncActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().setAcceptCookie(true)

        setContentView(R.layout.activity_sync)

        webViewClient = TongjiWebViewClient(this)

        webView = findViewById<WebView>(R.id.webView).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowContentAccess = true
            settings.userAgentString = settings.userAgentString.replace(
                "wv",
                "",
                ignoreCase = true
            )
            webViewClient = this@SyncActivity.webViewClient
        }

        syncButton = findViewById<Button>(R.id.syncButton).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#E27386"))
            setOnClickListener {
                startSync()
            }
        }

        webView.loadUrl(TONGJI_URL)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.webViewClient = WebViewClient()
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        webView.clearHistory()
        (webView.parent as? android.view.ViewGroup)?.removeView(webView)
        webView.destroy()
        super.onDestroy()
    }

    fun startSync() {
        syncManager.startSync()
    }

    override fun onSyncStart() {
        syncButton.isEnabled = false
        syncButton.text = "同步中…"
        Toast.makeText(this, "正在同步课表…", Toast.LENGTH_SHORT).show()
    }

    override fun onSyncSuccess() {
        Toast.makeText(this, "同步成功", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    override fun onSyncError(message: String) {
        syncButton.isEnabled = true
        syncButton.text = "同步课表"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private companion object {
        const val TONGJI_URL = "https://1.tongji.edu.cn"
    }
}