package com.offshore.platform.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.repository.FileUploadRepository
import com.offshore.platform.mobile.util.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker for uploading files that are stuck in PENDING/FAILED state.
 *
 * Constraints: NetworkType.CONNECTED
 */
@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileUploadRepository: FileUploadRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        AppLogger.i("UploadWorker started")

        return try {
            val result = fileUploadRepository.retryFailedUploads()
            when (result) {
                is NetworkResult.Success -> {
                    AppLogger.i("UploadWorker: ${result.data} files uploaded")
                    Result.success()
                }
                is NetworkResult.NetworkError -> {
                    AppLogger.w("UploadWorker: network error — retry")
                    Result.retry()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) {
            AppLogger.e("UploadWorker failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "offshore_file_upload"

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<UploadWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .addTag("offshore-upload")
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }

        fun enqueueOneTime(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(constraints)
                .addTag("offshore-upload-onetime")
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
