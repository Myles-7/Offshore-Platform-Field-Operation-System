package com.offshore.platform.mobile.data.remote

import android.content.Context
import com.offshore.platform.mobile.data.remote.api.FileApi
import com.offshore.platform.mobile.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChunkUploadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileApi: FileApi
) {
    companion object {
        const val DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024L
        const val DEFAULT_THRESHOLD = 20 * 1024 * 1024L
    }

    data class ChunkProgress(
        val uploadId: String = "",
        val totalChunks: Int = 0,
        val uploadedChunks: Int = 0,
        val percent: Float = 0f,
        val isComplete: Boolean = false,
        val error: String? = null
    )

    private val _progress = MutableStateFlow(ChunkProgress())
    val progress: StateFlow<ChunkProgress> = _progress.asStateFlow()
    private var cancelled = false

    fun shouldUseChunked(file: File, threshold: Long = DEFAULT_THRESHOLD): Boolean =
        file.length() >= threshold

    suspend fun uploadChunked(
        file: File, fileType: String, workOrderId: Long?, recordId: Long?,
        localId: String, deviceId: String
    ): NetworkResult<String> {
        cancelled = false
        val totalSize = file.length()
        val totalChunks = ((totalSize + DEFAULT_CHUNK_SIZE - 1) / DEFAULT_CHUNK_SIZE).toInt()
        val checksum = computeChecksum(file)
        _progress.value = ChunkProgress(totalChunks = totalChunks)

        val uploadId = tryInit(file.name, totalSize, totalChunks, checksum)
            ?: return NetworkResult.NetworkError(Exception("Chunk init failed"))
        _progress.value = _progress.value.copy(uploadId = uploadId)

        for (i in 0 until totalChunks) {
            if (cancelled) return NetworkResult.NetworkError(Exception("Cancelled"))
            val start = i * DEFAULT_CHUNK_SIZE
            val end = minOf(start + DEFAULT_CHUNK_SIZE, totalSize)
            if (!tryUploadChunk(uploadId, i, file.readBytes(start, end)))
                return NetworkResult.NetworkError(Exception("Chunk $i upload failed"))
            _progress.value = _progress.value.copy(uploadedChunks = i + 1, percent = (i + 1).toFloat() / totalChunks)
        }

        val fileId = tryMerge(uploadId, fileType, workOrderId, recordId, localId, deviceId)
            ?: return NetworkResult.NetworkError(Exception("Merge failed"))
        _progress.value = _progress.value.copy(isComplete = true, percent = 1f)
        return NetworkResult.Success(fileId)
    }

    fun cancel() { cancelled = true }

    private suspend fun tryInit(fileName: String, totalSize: Long, totalChunks: Int, checksum: String): String? = try {
        val body = buildJsonObject {
            put("fileName", fileName); put("fileSize", totalSize)
            put("totalChunks", totalChunks); put("chunkSize", DEFAULT_CHUNK_SIZE)
            put("checksum", checksum)
        }
        val resp = fileApi.chunkInit(body)
        if (resp.isSuccessful && resp.body()?.code == 200)
            (resp.body()?.data?.get("uploadId") as? JsonPrimitive)?.content
        else null
    } catch (_: Exception) { null }

    private suspend fun tryUploadChunk(uploadId: String, chunkIndex: Int, bytes: ByteArray): Boolean = try {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("uploadId", uploadId)
            .addFormDataPart("chunkIndex", chunkIndex.toString())
            .addFormDataPart("file", "chunk_$chunkIndex", bytes.toRequestBody("application/octet-stream".toMediaType()))
            .build()
        val resp = fileApi.chunkUpload(body)
        resp.isSuccessful && resp.body()?.code == 200
    } catch (_: Exception) { false }

    private suspend fun tryMerge(
        uploadId: String, fileType: String, workOrderId: Long?, recordId: Long?,
        localId: String, deviceId: String
    ): String? = try {
        val body = buildJsonObject {
            put("uploadId", uploadId); put("fileType", fileType)
            workOrderId?.let { put("workOrderId", it) }; recordId?.let { put("recordId", it) }
            put("localId", localId); put("deviceId", deviceId)
        }
        val resp = fileApi.chunkMerge(body)
        if (resp.isSuccessful && resp.body()?.code == 200)
            (resp.body()?.data?.get("fileId") as? JsonPrimitive)?.content
        else null
    } catch (_: Exception) { null }

    private fun computeChecksum(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { fis ->
            val buf = ByteArray(8192); var len: Int
            while (fis.read(buf).also { len = it } > 0) md.update(buf, 0, len)
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun File.readBytes(start: Long, end: Long): ByteArray {
        val len = (end - start).toInt(); val buf = ByteArray(len)
        FileInputStream(this).use { fis ->
            fis.skip(start); var off = 0
            while (off < len) { val r = fis.read(buf, off, len - off); if (r < 0) break; off += r }
        }
        return buf
    }
}

private fun JsonObjectBuilder.put(key: String, value: String) { put(key, JsonPrimitive(value)) }
private fun JsonObjectBuilder.put(key: String, value: Long) { put(key, JsonPrimitive(value)) }
private fun JsonObjectBuilder.put(key: String, value: Int) { put(key, JsonPrimitive(value)) }
