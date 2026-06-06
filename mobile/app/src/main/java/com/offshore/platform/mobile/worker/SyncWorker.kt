package com.offshore.platform.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.repository.SyncRepository
import com.offshore.platform.mobile.util.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager-based sync worker.
 *
 * Constraints:
 *   - NetworkType.CONNECTED (WiFi or Ethernet)
 *   - BatteryNotLow (optional, for production)
 *
 * Schedule:
 *   - Periodic: every 15 minutes (minimum interval for WorkManager)
 *   - One-time: triggered manually from SyncCenterScreen
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        AppLogger.i("SyncWorker started: ${inputData.getString("trigger") ?: "periodic"}")

        return try {
            val result = syncRepository.fullSync()

            when (result) {
                is NetworkResult.Success -> {
                    val summary = result.data
                    AppLogger.i(
                        "SyncWorker SUCCESS: push=${summary.pushCount}, pull=${summary.pullCount}"
                    )
                    Result.success()
                }
                is NetworkResult.NetworkError -> {
                    AppLogger.w("SyncWorker: network error — will retry")
                    Result.retry()
                }
                is NetworkResult.Unauthorized -> {
                    AppLogger.w("SyncWorker: unauthorized — not retrying")
                    Result.failure() // Don't retry on 401
                }
                else -> {
                    AppLogger.w("SyncWorker: ${result::class.simpleName} — will retry")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            AppLogger.e("SyncWorker failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "offshore_periodic_sync"

        /** Schedule periodic sync (every 15 min). */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .addTag("offshore-sync")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // keep existing schedule
                request
            )

            AppLogger.i("Periodic sync scheduled (15 min interval)")
        }

        /** Trigger a one-time sync now. */
        fun enqueueOneTime(context: Context, triggerTag: String = "manual") {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = Data.Builder()
                .putString("trigger", triggerTag)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .addTag("offshore-sync-onetime")
                .build()

            WorkManager.getInstance(context).enqueue(request)
            AppLogger.i("One-time sync enqueued: $triggerTag")
        }

        /** Cancel all pending sync work. */
        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag("offshore-sync")
            WorkManager.getInstance(context).cancelAllWorkByTag("offshore-sync-onetime")
        }
    }
}
