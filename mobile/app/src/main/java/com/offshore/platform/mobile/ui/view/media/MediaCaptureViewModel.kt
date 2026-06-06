package com.offshore.platform.mobile.ui.view.media

import android.content.Context
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.dao.AttachmentDao
import com.offshore.platform.mobile.data.local.dao.SyncQueueDao
import com.offshore.platform.mobile.data.local.entity.LocalSyncQueueEntity
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderAttachmentEntity
import com.offshore.platform.mobile.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MediaCaptureViewModel @Inject constructor(
    private val attachmentDao: AttachmentDao,
    private val syncQueueDao: SyncQueueDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(MediaCaptureUiState())
    val uiState: StateFlow<MediaCaptureUiState> = _uiState.asStateFlow()

    private var workOrderId = 0L
    private var recordId: Long? = null
    private var workOrderNo = ""
    private var fileType = "VIDEO"
    private var mediaRecorder: MediaRecorder? = null
    private var videoRecorder: VideoRecorder? = null
    private var timerJob: Job? = null

    fun init(woId: Long, recId: Long?, woNo: String, type: String) {
        workOrderId = woId; recordId = recId; workOrderNo = woNo; fileType = type
    }

    // ---- Video recording via VideoRecorder interface ----

    fun startVideoRecording(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRecording = true, recordingDuration = 0, error = null)

            // Use VideoRecorder interface (MediaRecorderVideoRecorder is the default)
            val recorder = MediaRecorderVideoRecorder()
            videoRecorder = recorder

            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _uiState.value = _uiState.value.copy(
                        recordingDuration = _uiState.value.recordingDuration + 1
                    )
                }
            }

            withContext(Dispatchers.IO) {
                try {
                    val dir = File(context.filesDir, "videos"); dir.mkdirs()
                    val nameHint = "video_${DateTimeUtil.fileNameTimestamp()}"
                    recorder.prepare(dir, nameHint)
                    recorder.start()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            isRecording = false,
                            error = "录像初始化失败: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    // ---- Audio recording via MediaRecorder ---- (unchanged for stability)

    fun startRecording(context: Context) {
        if (fileType == "VIDEO") {
            startVideoRecording(context)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRecording = true, recordingDuration = 0)
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _uiState.value = _uiState.value.copy(recordingDuration = _uiState.value.recordingDuration + 1)
                }
            }
            withContext(Dispatchers.IO) {
                val dir = File(context.filesDir, "audio"); dir.mkdirs()
                val f = File(dir, "audio_${DateTimeUtil.fileNameTimestamp()}.m4a")
                mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(f.absolutePath)
                    prepare()
                    start()
                }
                _uiState.value = _uiState.value.copy(capturedFile = f)
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            timerJob?.cancel()

            // Try video recorder first
            val vr = videoRecorder
            if (vr != null) {
                withContext(Dispatchers.IO) {
                    try {
                        vr.stop()
                        _uiState.value = _uiState.value.copy(
                            isRecording = false,
                            capturedFile = vr.outputFile,
                            error = vr.errorMessage
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isRecording = false,
                            error = "停止录像失败: ${e.message}"
                        )
                    }
                }
                return@launch
            }

            // Audio recorder
            try {
                withContext(Dispatchers.IO) {
                    mediaRecorder?.apply { stop(); release() }
                    mediaRecorder = null
                }
                _uiState.value = _uiState.value.copy(isRecording = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRecording = false, error = e.message)
            }
        }
    }

    fun releaseRecorder() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                videoRecorder?.release()
                videoRecorder = null
                mediaRecorder?.apply {
                    try { stop() } catch (_: Exception) {}
                    try { release() } catch (_: Exception) {}
                }
                mediaRecorder = null
            }
            timerJob?.cancel()
        }
    }

    fun reset() {
        videoRecorder = null
        _uiState.value = MediaCaptureUiState()
    }

    fun setPermissionDenied() {
        _uiState.value = _uiState.value.copy(permissionDenied = true)
    }

    /** Save video file to attachment table + sync queue. */
    fun saveVideoFile() {
        val file = _uiState.value.capturedFile ?: return
        saveFile(file, "VIDEO")
    }

    /** Save audio file to attachment table + sync queue. */
    fun saveFile() {
        val file = _uiState.value.capturedFile ?: return
        saveFile(file, if (fileType == "AUDIO") "AUDIO" else "VIDEO")
    }

    private fun saveFile(file: File, attType: String) {
        viewModelScope.launch {
            try {
                val now = DateTimeUtil.nowFormatted()
                val localId = "att-${UUID.randomUUID()}"
                val mime = when (attType) {
                    "VIDEO" -> "video/mp4"
                    "AUDIO" -> "audio/mp4"
                    else -> "application/octet-stream"
                }
                val entity = LocalWorkOrderAttachmentEntity(
                    localId = localId, workOrderId = workOrderId, recordId = recordId,
                    attachmentType = attType,
                    attachmentName = file.name,
                    captureTime = now,
                    captureUserName = TokenManager.getRealName() ?: TokenManager.getUsername(),
                    localFilePath = file.absolutePath,
                    fileSize = file.length(),
                    mimeType = mime,
                    durationSeconds = _uiState.value.recordingDuration,
                    uploadStatus = "PENDING",
                    syncStatus = "LOCAL_ONLY",
                    deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now, updatedAt = now
                )
                attachmentDao.insert(entity)
                val payload = buildJsonObject {
                    put("localId", localId)
                    put("attachmentType", attType)
                    put("attachmentName", file.name)
                    put("captureTime", now)
                    put("durationSeconds", _uiState.value.recordingDuration)
                    put("deviceId", DeviceManager.getOrCreate())
                }
                syncQueueDao.enqueue(LocalSyncQueueEntity(
                    moduleType = "ATTACHMENT", entityType = "WORK_ORDER_ATTACHMENT",
                    localId = localId, workOrderId = workOrderId, actionType = "CREATE",
                    payloadJson = Json.encodeToString(JsonObject.serializer(), payload),
                    syncStatus = "PENDING", deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now, updatedAt = now
                ))
                _uiState.value = _uiState.value.copy(saved = true, savedOffline = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseRecorder()
    }
}

data class MediaCaptureUiState(
    val isRecording: Boolean = false,
    val recordingDuration: Int = 0,
    val capturedFile: File? = null,
    val saved: Boolean = false,
    val savedOffline: Boolean = false,
    val permissionDenied: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
