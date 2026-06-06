package com.offshore.platform.mobile.ui.view.media

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offshore.platform.mobile.data.local.dao.AttachmentDao
import com.offshore.platform.mobile.data.local.dao.SyncQueueDao
import com.offshore.platform.mobile.data.local.entity.LocalSyncQueueEntity
import com.offshore.platform.mobile.data.local.entity.LocalWorkOrderAttachmentEntity
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import com.offshore.platform.mobile.util.WatermarkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val attachmentDao: AttachmentDao,
    private val syncQueueDao: SyncQueueDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var workOrderId = 0L
    private var recordId: Long? = null
    private var workOrderNo = ""
    private var workLocation: String? = null

    fun init(woId: Long, recId: Long?, woNo: String, loc: String?) {
        workOrderId = woId; recordId = recId; workOrderNo = woNo; workLocation = loc
    }

    // Called when CameraX is ready
    fun setCameraReady() {
        _uiState.value = _uiState.value.copy(cameraReady = true, useCameraX = true, isSystemCamera = false, error = null)
    }

    // Called when CameraX fails — fallback to system camera
    fun setUseSystemCamera() {
        _uiState.value = _uiState.value.copy(
            cameraReady = true, useCameraX = false, isSystemCamera = true,
            error = null
        )
    }

    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(flashOn = !_uiState.value.flashOn)
    }

    fun toggleCameraLens() {
        _uiState.value = _uiState.value.copy(useFrontCamera = !_uiState.value.useFrontCamera)
    }

    fun setCapturing(capturing: Boolean) {
        _uiState.value = _uiState.value.copy(isCapturing = capturing)
    }

    /**
     * Called when CameraX ImageCapture.takePicture() succeeds with OutputFileOptions.
     * The photo file is written by CameraX, we apply watermark if possible.
     */
    fun onPhotoFileCaptured(context: Context, photoFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Apply watermark
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                val watermarked = WatermarkUtil.applyWatermark(
                    context, uri, workOrderNo,
                    TokenManager.getRealName() ?: TokenManager.getUsername(),
                    workLocation, DeviceManager.getDeviceId()
                )
                val captured = watermarked ?: photoFile
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isCapturing = false,
                        capturedFile = captured
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isCapturing = false,
                        error = "处理照片失败: ${e.message}"
                    )
                }
            }
        }
    }

    /** Called when CameraX capture fails. */
    fun onCaptureFailed(message: String) {
        _uiState.value = _uiState.value.copy(
            isCapturing = false,
            error = message
        )
    }

    /** Called when system camera path completes (watermark already applied). */
    fun onSystemCameraPhoto(file: File) {
        _uiState.value = _uiState.value.copy(
            isCapturing = false,
            capturedFile = file
        )
    }

    fun setPermissionDenied() {
        _uiState.value = _uiState.value.copy(permissionDenied = true)
    }

    fun retake() {
        _uiState.value = _uiState.value.copy(
            capturedFile = null,
            isCapturing = false,
            error = null
        )
    }

    fun savePhoto() {
        val file = _uiState.value.capturedFile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val now = DateTimeUtil.nowFormatted()
                val localId = "att-${UUID.randomUUID()}"

                // Generate thumbnail
                val thumbPath = withContext(Dispatchers.IO) {
                    try {
                        val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                        val bmp = BitmapFactory.decodeFile(file.absolutePath, opts)
                        if (bmp != null) {
                            val thumbFile = File(file.parent, "thumb_${file.name}")
                            FileOutputStream(thumbFile).use { out ->
                                bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
                            }
                            bmp.recycle()
                            thumbFile.absolutePath
                        } else null
                    } catch (_: Exception) { null }
                }

                val entity = LocalWorkOrderAttachmentEntity(
                    localId = localId,
                    workOrderId = workOrderId,
                    recordId = recordId,
                    attachmentType = "PHOTO",
                    attachmentName = file.name,
                    captureTime = now,
                    captureUserName = TokenManager.getRealName() ?: TokenManager.getUsername(),
                    watermarkFlag = 1,
                    watermarkText = "工单:$workOrderNo",
                    localFilePath = file.absolutePath,
                    localThumbnailPath = thumbPath,
                    fileSize = file.length(),
                    mimeType = "image/jpeg",
                    uploadStatus = "PENDING",
                    syncStatus = "LOCAL_ONLY",
                    deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now,
                    updatedAt = now
                )
                attachmentDao.insert(entity)

                val payload = buildJsonObject {
                    put("localId", localId)
                    put("attachmentType", "PHOTO")
                    put("attachmentName", file.name)
                    put("captureTime", now)
                    put("watermarkFlag", 1)
                    put("watermarkText", "工单:$workOrderNo")
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

                // Trigger upload worker
                try {
                    com.offshore.platform.mobile.worker.UploadWorker.enqueueOneTime(com.offshore.platform.mobile.OffshoreApp.instance)
                } catch (_: Exception) {}

                _uiState.value = _uiState.value.copy(isSaving = false, saved = true, savedOffline = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

data class CameraUiState(
    val isCapturing: Boolean = false,
    val capturedFile: File? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val savedOffline: Boolean = false,
    val permissionDenied: Boolean = false,
    val cameraReady: Boolean = false,
    val useCameraX: Boolean = false,
    val isSystemCamera: Boolean = false,
    val flashOn: Boolean = false,
    val useFrontCamera: Boolean = false,
    val error: String? = null
)
