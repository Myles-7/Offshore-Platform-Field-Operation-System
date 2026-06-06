package com.offshore.platform.mobile.data.repository

import com.offshore.platform.mobile.data.local.dao.AiResultDao
import com.offshore.platform.mobile.data.local.dao.SyncQueueDao
import com.offshore.platform.mobile.data.local.entity.LocalAiResultEntity
import com.offshore.platform.mobile.data.local.entity.LocalSyncQueueEntity
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import com.offshore.platform.mobile.domain.model.AiInferenceResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.util.UUID

/**
 * Runs AI inference on a photo and persists results offline-first.
 *
 * Flow:
 *  1. Accept a photo file path.
 *  2. Run inference via the selected [AiInferenceEngine].
 *  3. Write results to [LocalAiResultEntity] → Room.
 *  4. Enqueue sync via [SyncQueueDao].
 */
@Singleton
class AiInferenceRepository @Inject constructor(
    private val aiResultDao: AiResultDao,
    private val syncQueueDao: SyncQueueDao,
    private val engine: AiInferenceEngine
) : BaseRepository() {

    /**
     * Run inference and save results.
     */
    suspend fun inferAndSave(
        photoFile: File,
        workOrderId: Long,
        recordId: Long?,
        attachmentId: Long?
    ): AiInferenceResult? {
        val result = engine.infer(photoFile) ?: return null

        val now = DateTimeUtil.nowFormatted()
        val localId = "ai-${UUID.randomUUID()}"
        val entity = LocalAiResultEntity(
            localId = localId,
            workOrderId = workOrderId,
            recordId = recordId,
            attachmentId = attachmentId,
            aiResultNo = "AI-${DateTimeUtil.fileNameTimestamp()}",
            modelVersion = result.modelVersion,
            defectType = result.defectType,
            confidence = result.confidence.toDouble(),
            suspectedDefectFlag = if (result.suspectedDefectFlag) 1 else 0,
            defectCount = result.boxes.size,
            reviewStatus = "PENDING_REVIEW",
            syncStatus = "LOCAL_ONLY",
            deviceId = DeviceManager.getOrCreate(),
            operatorId = TokenManager.getUserId().takeIf { it > 0 },
            createdAt = now,
            updatedAt = now
        )
        aiResultDao.insertAll(listOf(entity))

        val payload = kotlinx.serialization.json.buildJsonObject {
            put("localId", kotlinx.serialization.json.JsonPrimitive(localId))
            put("workOrderId", kotlinx.serialization.json.JsonPrimitive(workOrderId))
            put("defectType", kotlinx.serialization.json.JsonPrimitive(result.defectType))
            put("confidence", kotlinx.serialization.json.JsonPrimitive(result.confidence))
            put("modelVersion", kotlinx.serialization.json.JsonPrimitive(result.modelVersion))
        }
        syncQueueDao.enqueue(LocalSyncQueueEntity(
            moduleType = "AI_RESULT", entityType = "AI_RESULT",
            localId = localId, workOrderId = workOrderId, actionType = "CREATE",
            payloadJson = kotlinx.serialization.json.Json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), payload),
            syncStatus = "PENDING", deviceId = DeviceManager.getOrCreate(),
            operatorId = TokenManager.getUserId().takeIf { it > 0 },
            createdAt = now, updatedAt = now
        ))

        return result
    }
}
