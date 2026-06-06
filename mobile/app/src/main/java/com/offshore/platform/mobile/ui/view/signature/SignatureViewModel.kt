package com.offshore.platform.mobile.ui.view.signature

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.geometry.Offset
import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
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
import javax.inject.Inject

data class SignaturePoint(val x: Float, val y: Float)

data class SignatureUiState(
    val points: List<Offset> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val savedOffline: Boolean = false,
    val localId: String? = null,
    val error: String? = null
)

@HiltViewModel
class SignatureViewModel @Inject constructor(
    private val signatureDao: SignatureDao,
    private val syncQueueDao: SyncQueueDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignatureUiState())
    val uiState: StateFlow<SignatureUiState> = _uiState.asStateFlow()

    private var workOrderId: Long = 0
    private val rawPoints = mutableListOf<Offset>()

    fun init(woId: Long) { workOrderId = woId }

    fun addPoint(x: Float, y: Float) {
        rawPoints.add(Offset(x, y))
        _uiState.value = _uiState.value.copy(points = rawPoints.toList())
    }

    fun clear() {
        rawPoints.clear()
        _uiState.value = _uiState.value.copy(points = emptyList(), saved = false, savedOffline = false, error = null)
    }

    fun save(context: Context, canvasWidth: Float, canvasHeight: Float, density: Float) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val localId = "sig-${UUID.randomUUID()}"
                val file = withContext(Dispatchers.IO) {
                    renderSignatureToFile(context, rawPoints, canvasWidth, canvasHeight, density)
                }
                val now = DateTimeUtil.nowFormatted()

                val entity = LocalSignatureEntity(
                    localId = localId,
                    workOrderId = workOrderId,
                    signatureType = "HANDWRITTEN",
                    signatureTime = now,
                    signerName = TokenManager.getRealName() ?: TokenManager.getUsername(),
                    localFilePath = file?.absolutePath,
                    syncStatus = "LOCAL_ONLY",
                    deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now,
                    updatedAt = now
                )
                signatureDao.insert(entity)

                // Enqueue sync — use safe JSON serialization
                val payload = buildJsonObject {
                    put("localId", localId)
                    put("workOrderId", workOrderId)
                    put("signatureType", "HANDWRITTEN")
                    put("signatureTime", now)
                    put("signerName", entity.signerName ?: "")
                    put("canvasWidth", canvasWidth)
                    put("canvasHeight", canvasHeight)
                    put("density", density.toDouble())
                }
                syncQueueDao.enqueue(LocalSyncQueueEntity(
                    moduleType = "SIGNATURE", entityType = "WORK_ORDER_SIGNATURE",
                    localId = localId, workOrderId = workOrderId, actionType = "CREATE",
                    payloadJson = Json.encodeToString(JsonObject.serializer(), payload),
                    syncStatus = "PENDING",
                    deviceId = DeviceManager.getOrCreate(),
                    operatorId = TokenManager.getUserId().takeIf { it > 0 },
                    createdAt = now, updatedAt = now
                ))

                _uiState.value = _uiState.value.copy(isSaving = false, saved = true, savedOffline = true, localId = localId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    private fun renderSignatureToFile(context: Context, pts: List<Offset>, canvasW: Float, canvasH: Float, density: Float): File? {
        if (pts.isEmpty()) return null
        val bmpW = 800; val bmpH = 400
        val scaleX = bmpW / canvasW
        val scaleY = bmpH / canvasH
        val bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp); canvas.drawColor(android.graphics.Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK; strokeWidth = 4f * density; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        val path = Path()
        pts.forEachIndexed { i, pt -> val x = pt.x * scaleX; val y = pt.y * scaleY; if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }
        canvas.drawPath(path, paint)

        val dir = File(context.filesDir, "signatures"); dir.mkdirs()
        val f = File(dir, "sig_${System.currentTimeMillis()}.png")
        FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return f
    }
}
