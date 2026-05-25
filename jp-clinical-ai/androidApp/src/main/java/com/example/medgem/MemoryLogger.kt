package com.example.medgem

import android.app.ActivityManager
import android.content.Context
import android.util.Log

object MemoryLogger {
    fun logMemoryUsage(context: Context, tag: String = "MemoryLogger") {
        try {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalMem = memoryInfo.totalMem
            val availMem = memoryInfo.availMem
            val usedMem = totalMem - availMem

            val usedMemMb = usedMem / (1024 * 1024)
            val totalMemMb = totalMem / (1024 * 1024)
            val availMemMb = availMem / (1024 * 1024)

            Log.d(
                tag,
                "Memory Usage: Used=${usedMemMb}MB, Free=${availMemMb}MB, Total=${totalMemMb}MB"
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to log memory usage", e)
        }
    }
}
