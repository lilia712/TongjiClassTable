package com.example.courseschedule.sync

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import com.example.courseschedule.data.RetrofitClient
import com.example.courseschedule.data.Timetable
import com.example.courseschedule.data.TimetableStorage
import com.example.courseschedule.data.TongjiMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TongjiSync"

class TongjiSyncManager(
    private val activity: SyncActivity,
    private val webView: WebView
) {

    interface Listener {
        fun onSyncStart()
        fun onSyncSuccess()
        fun onSyncError(message: String)
    }

    var listener: Listener? = null

    private val handler = Handler(Looper.getMainLooper())

    fun startSync() {
        listener?.onSyncStart()
        Log.d(TAG, "startSync called")
        attemptSync(retriesLeft = 6)
    }

    private fun attemptSync(retriesLeft: Int) {
        val client = activity.webViewClient
        val studentCode = client.capturedParams["studentCode"] ?: ""
        val calendarId = client.capturedParams["calendarId"] ?: ""
        // 页面发送的 X-Token 为空，认证由 Cookie 完成；此处保留非空捕获以防万一
        val token = client.capturedHeaders["x-token"] ?: ""

        Log.d(TAG, "attemptSync retriesLeft=$retriesLeft, studentCode=$studentCode, calendarId=$calendarId, token=$token")

        if (studentCode.isBlank() && retriesLeft > 0) {
            handler.postDelayed({
                attemptSync(retriesLeft - 1)
            }, 1000)
            return
        }

        if (studentCode.isBlank()) {
            listener?.onSyncError(
                "未能捕获登录凭证\n\n捕获到的参数:\n" +
                    client.capturedParams.entries.joinToString("\n") { "${it.key}=${it.value}" }
            )
            return
        }

        val effectiveCalendarId = calendarId.ifBlank { "122" }
        performSync(token, studentCode, effectiveCalendarId)
    }

    private fun performSync(
        token: String,
        studentCode: String,
        calendarId: String
    ) {
        activity.lifecycleScope.launch {
            try {
                Log.d(TAG, "Sync request: token='$token', studentCode=$studentCode, calendarId=$calendarId")

                val courses = withContext(Dispatchers.IO) {
                    val json = RetrofitClient.api.getTimetable(
                        calendarId = calendarId,
                        studentCode = studentCode,
                        timestamp = System.currentTimeMillis(),
                        token = token
                    )
                    Log.d(TAG, "API response length=${json.length}")
                    Log.d(TAG, "API response preview=${json.take(300)}")
                    TongjiMapper.convert(json)
                }
                Log.d(TAG, "Parsed ${courses.size} courses")

                TimetableStorage(activity).save(
                    Timetable(
                        updatedAt = System.currentTimeMillis(),
                        courses = courses
                    )
                )
                listener?.onSyncSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                listener?.onSyncError("同步失败: ${e.message}")
            }
        }
    }
}