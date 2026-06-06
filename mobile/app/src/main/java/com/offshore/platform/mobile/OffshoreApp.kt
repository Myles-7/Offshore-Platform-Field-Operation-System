package com.offshore.platform.mobile

import android.app.Application
import com.offshore.platform.mobile.util.AppLogger
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import com.offshore.platform.mobile.worker.SyncWorker
import com.offshore.platform.mobile.worker.UploadWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OffshoreApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Restore persisted security state for ApiClient interceptors
        TokenManager.restoreToken()
        DeviceManager.restore()

        // Schedule periodic sync workers (idempotent — keeps existing schedule)
        SyncWorker.schedulePeriodic(this)
        UploadWorker.schedulePeriodic(this)

        AppLogger.i("OffshoreApp initialized, periodic sync workers scheduled")
    }

    companion object {
        lateinit var instance: OffshoreApp
            private set
    }
}
