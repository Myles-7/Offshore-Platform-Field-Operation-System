package com.offshore.platform.mobile.util

import android.util.Log

/**
 * Centralised logging utility.
 * Debug logs are suppressed in release builds via BuildConfig.DEBUG check.
 */
object AppLogger {

    private const val TAG = "OffshoreMobile"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }
}
