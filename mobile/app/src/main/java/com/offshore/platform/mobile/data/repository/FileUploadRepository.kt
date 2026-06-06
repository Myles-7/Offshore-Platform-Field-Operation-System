package com.offshore.platform.mobile.data.repository

import android.content.Context
import com.offshore.platform.mobile.data.local.dao.AttachmentDao
import com.offshore.platform.mobile.data.local.dao.SyncQueueDao
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderAttachmentEntity
import com.offshore.platform.mobile.data.remote.ChunkUploadManager
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.remote.api.FileApi
import com.offshore.platform.mobile.util.AppLogger
import com.offshore.platform.mobile.util.DeviceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileUploadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileApi: FileApi,
    private val attachmentDao: AttachmentDao,
    private val syncQueueDao: SyncQueueDao,
    private val chunkUploadManager: ChunkUploadManager
) : BaseRepository() {

    /**
     * Upload a file. For large files (>20MB), attempts chunked upload with
     * automatic fallback to normal upload if chunk API is unavailable.
     */
    suspend fun uploadAndBind(attachmentId: Long): NetworkResult<String> {
        // Find attachment by the id field
        val att = try {
            findAttachmentById(attachmentId)
        } catch (_: Exception) {
            return NetworkResult.BusinessError(404, "附件未找到", null)
        } ?: return NetworkResult.BusinessError(404, "附件未找到", null)

        if (att.localFilePath.isNullOrBlank()) {
            return NetworkResult.BusinessError(400, "本地文件不存在", null)
        }

        val file = File(att.localFilePath)
        if (!file.exists()) {
            return NetworkResult.BusinessError(400, "文件已丢失", null)
        }

        // Determine strategy
        val shouldChunk = chunkUploadManager.shouldUseChunked(file)

        return if (shouldChunk) {
            val chunkResult = chunkUploadManager.uploadChunked(
                file = file,
                fileType = att.attachmentType,
                workOrderId = att.workOrderId,
                recordId = att.recordId,
                localId = att.localId,
                deviceId = DeviceManager.getOrCreate()
            )
            when (chunkResult) {
                is NetworkResult.Success -> {
                    onUploadSuccess(att, chunkResult.data)
                }
                is NetworkResult.NetworkError -> {
                    AppLogger.w("Chunk upload failed, falling back to normal upload")
                    normalUpload(att, file)
                }
                else -> normalUpload(att, file)
            }
        } else {
            normalUpload(att, file)
        }
    }

    /** Normal (single-part) upload. */
    private suspend fun normalUpload(
        att: LocalWorkOrderAttachmentEntity,
        file: File
    ): NetworkResult<String> {
        val mimeType = att.mimeType?.toMediaType() ?: "image/jpeg".toMediaType()
        val requestBody = file.readBytes().toRequestBody(mimeType)
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val fileTypeBody = att.attachmentType.toRequestBody("text/plain".toMediaType())
        val workOrderIdBody = att.workOrderId.toString().toRequestBody("text/plain".toMediaType())
        val recordIdBody = (att.recordId?.toString() ?: "").toRequestBody("text/plain".toMediaType())
        val localIdBody = att.localId.toRequestBody("text/plain".toMediaType())
        val deviceIdBody = DeviceManager.getOrCreate().toRequestBody("text/plain".toMediaType())

        val uploadResult = safeApiCall {
            fileApi.upload(part, fileTypeBody, workOrderIdBody, recordIdBody, localIdBody, deviceIdBody)
        }

        return when (uploadResult) {
            is NetworkResult.Success -> {
                val fileId = (uploadResult.data["fileId"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                    ?: return NetworkResult.BusinessError(500, "上传成功但未返回fileId", null)
                onUploadSuccess(att, fileId)
            }
            is NetworkResult.NetworkError -> {
                attachmentDao.update(att.copy(uploadStatus = "FAILED", uploadRetryCount = att.uploadRetryCount + 1))
                @Suppress("UNCHECKED_CAST")
                (uploadResult as NetworkResult<String>)
            }
            else -> {
                @Suppress("UNCHECKED_CAST")
                (uploadResult as NetworkResult<String>)
            }
        }
    }

    private suspend fun onUploadSuccess(att: LocalWorkOrderAttachmentEntity, fileId: String): NetworkResult<String> {
        attachmentDao.markFileUploaded(att.localId, fileId)
        attachmentDao.markSynced(att.localId)
        return NetworkResult.Success(fileId)
    }

    /** Find attachment by its primary key id. */
    private suspend fun findAttachmentById(id: Long): LocalWorkOrderAttachmentEntity? {
        // Since we only have getByLocalId and getByServerId, we try all known ways
        // In practice, we'll look up by iterating pending uploads
        val pending = attachmentDao.getByUploadStatuses(listOf("PENDING", "FAILED"))
        return pending.find { it.id == id }
    }

    /** Retry all failed uploads. */
    suspend fun retryFailedUploads(): NetworkResult<Int> {
        val failed = attachmentDao.getByUploadStatuses(listOf("FAILED", "PENDING"))
        var success = 0
        for (att in failed) {
            val result = uploadAndBind(att.id)
            if (result is NetworkResult.Success) success++
        }
        return NetworkResult.Success(success)
    }

    /** Get pending uploads count. */
    suspend fun getPendingCount(): Int =
        attachmentDao.getByUploadStatuses(listOf("PENDING", "FAILED")).size

    /** Alias for backward compatibility. */
    suspend fun pendingUploadCount(): Int = getPendingCount()

    companion object {
        // Re-export from ChunkUploadManager for convenience
        const val DEFAULT_CHUNK_SIZE = ChunkUploadManager.DEFAULT_CHUNK_SIZE
        const val DEFAULT_THRESHOLD = ChunkUploadManager.DEFAULT_THRESHOLD
    }
}
