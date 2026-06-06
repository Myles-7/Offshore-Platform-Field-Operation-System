package com.offshore.platform.mobile.data.remote

import com.offshore.platform.mobile.BuildConfig

/**
 * Tiny wrapper for BuildConfig so that unit tests can mock debug/release flags
 * without loading the real BuildConfig class.
 */
object BuildConfigWrapper {
    @Volatile
    var isDebug: Boolean = BuildConfig.DEBUG
}
