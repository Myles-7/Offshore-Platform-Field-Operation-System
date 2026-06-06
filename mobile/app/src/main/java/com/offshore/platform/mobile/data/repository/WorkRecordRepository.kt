package com.offshore.platform.mobile.data.repository

import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.remote.api.MobileWorkOrderApi
import com.offshore.platform.mobile.data.remote.dto.*
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import kotlinx.serialization.json.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkRecordRepository @Inject constructor(
    private val workOrderApi: MobileWorkOrderApi,
    private val recordDao: WorkOrderRecordDao,
    private val attachmentDao: AttachmentDao,
    private val syncQueueDao: SyncQueueDao
) : BaseRepository() {

    // ---- fetch from server ----

    suspend fun fetchRecords(workOrderId: Long): NetworkResult<List<MobileWorkRecordResponseDto>> {
        return safeApiCall { workOrderApi.listWorkOrderRecords(workOrderId) }
    }

    // ---- local ----

    fun observeRecords(workOrderId: Long) = recordDao.observeByWorkOrderId(workOrderId)
    suspend fun getById(id: Long) = recordDao.getById(id)
    suspend fun getByLocalId(localId: String) = recordDao.getByLocalId(localId)

    // ---- create (offline-first) ----

    suspend fun createRecord(
        workOrderId: Long, constructionDesc: String?, siteCondition: String?,
        abnormalFlag: Int, abnormalDesc: String?, weather: String?,
        temperature: Double?, humidity: Double?, locationName: String?,
        latitude: Double?, longitude: Double?
    ): NetworkResult<LocalWorkOrderRecordEntity> {
        val now = DateTimeUtil.nowFormatted()
        val localId = "rec-${UUID.randomUUID()}"
        val opId = TokenManager.getUserId().takeIf { it > 0 }

        val entity = LocalWorkOrderRecordEntity(
            localId = localId, workOrderId = workOrderId,
            constructionTime = now, constructionUserId = opId,
            constructionUserName = TokenManager.getRealName() ?: TokenManager.getUsername(),
            constructionDesc = constructionDesc, siteCondition = siteCondition,
            abnormalFlag = abnormalFlag, abnormalDesc = abnormalDesc,
            weather = weather, temperature = temperature, humidity = humidity,
            locationName = locationName, latitude = latitude, longitude = longitude,
            syncStatus = "LOCAL_ONLY", deviceId = DeviceManager.getOrCreate(),
            operatorId = opId, createdAt = now, updatedAt = now
        )
        recordDao.insert(entity)
        enqueueSync(entity, "CREATE")
        return NetworkResult.Success(entity)
    }

    // ---- update (offline-first) ----

    suspend fun updateRecord(
        recordId: Long, constructionDesc: String?, siteCondition: String?,
        abnormalFlag: Int, abnormalDesc: String?, weather: String?,
        temperature: Double?, humidity: Double?, locationName: String?,
        latitude: Double?, longitude: Double?
    ): NetworkResult<LocalWorkOrderRecordEntity> {
        val existing = recordDao.getById(recordId)
            ?: return NetworkResult.BusinessError(404, "记录不存在", null)
        val now = DateTimeUtil.nowFormatted()
        val updated = existing.copy(
            constructionDesc = constructionDesc, siteCondition = siteCondition,
            abnormalFlag = abnormalFlag, abnormalDesc = abnormalDesc,
            weather = weather, temperature = temperature, humidity = humidity,
            locationName = locationName, latitude = latitude, longitude = longitude,
            updatedAt = now, syncStatus = "PENDING"
        )
        recordDao.update(updated)
        enqueueSync(updated, "UPDATE")
        return NetworkResult.Success(updated)
    }

    // ---- attachment helpers ----

    suspend fun attachLocalFile(
        workOrderId: Long, recordId: Long?, attachmentType: String,
        localFilePath: String, mimeType: String?, fileSize: Long,
        captureTime: String?, watermarkText: String?,
        durationSeconds: Int = 0, mediaWidth: Int = 0, mediaHeight: Int = 0
    ): LocalWorkOrderAttachmentEntity {
        val now = DateTimeUtil.nowFormatted()
        val localId = "att-${UUID.randomUUID()}"
        val fileName = localFilePath.substringAfterLast("/")
        val entity = LocalWorkOrderAttachmentEntity(
            localId = localId, workOrderId = workOrderId, recordId = recordId,
            attachmentType = attachmentType, attachmentName = fileName,
            captureTime = captureTime ?: now,
            captureUserName = TokenManager.getRealName() ?: TokenManager.getUsername(),
            watermarkFlag = if (watermarkText != null) 1 else 0,
            watermarkText = watermarkText, durationSeconds = durationSeconds,
            mediaWidth = mediaWidth, mediaHeight = mediaHeight,
            localFilePath = localFilePath, fileSize = fileSize, mimeType = mimeType,
            uploadStatus = "PENDING", syncStatus = "LOCAL_ONLY",
            deviceId = DeviceManager.getOrCreate(),
            operatorId = TokenManager.getUserId().takeIf { it > 0 },
            createdAt = now, updatedAt = now
        )
        attachmentDao.insert(entity)

        val payload = buildJsonObject {
            put("localId", localId); put("recordId", recordId?.toString() ?: "")
            put("attachmentType", attachmentType); put("attachmentName", fileName)
            put("captureTime", captureTime ?: now)
            put("watermarkFlag", if (watermarkText != null) 1 else 0)
            put("watermarkText", watermarkText ?: "")
            put("durationSeconds", durationSeconds)
            put("mediaWidth", mediaWidth); put("mediaHeight", mediaHeight)
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
        return entity
    }

    fun observeAttachments(workOrderId: Long) = attachmentDao.observeByWorkOrderId(workOrderId)
    suspend fun getPendingUploads() = attachmentDao.getByUploadStatuses(listOf("PENDING", "FAILED"))

    private suspend fun enqueueSync(entity: LocalWorkOrderRecordEntity, actionType: String) {
        val payload = buildJsonObject {
            put("localId", entity.localId); put("workOrderId", entity.workOrderId)
            put("recordType", entity.recordType ?: "DAILY")
            put("constructionTime", entity.constructionTime ?: "")
            put("constructionDesc", entity.constructionDesc ?: "")
            put("siteCondition", entity.siteCondition ?: "")
            put("abnormalFlag", entity.abnormalFlag)
            put("abnormalDesc", entity.abnormalDesc ?: "")
            put("weather", entity.weather ?: "")
            put("temperature", entity.temperature?.toString() ?: "")
            put("humidity", entity.humidity?.toString() ?: "")
            put("locationName", entity.locationName ?: "")
            put("latitude", entity.latitude?.toString() ?: "")
            put("longitude", entity.longitude?.toString() ?: "")
            put("deviceId", DeviceManager.getOrCreate())
        }
        syncQueueDao.enqueue(LocalSyncQueueEntity(
            moduleType = "WORK_RECORD", entityType = "WORK_ORDER_RECORD",
            localId = entity.localId, serverId = entity.serverId, workOrderId = entity.workOrderId,
            actionType = actionType,
            payloadJson = Json.encodeToString(JsonObject.serializer(), payload),
            syncStatus = "PENDING", deviceId = DeviceManager.getOrCreate(),
            operatorId = TokenManager.getUserId().takeIf { it > 0 },
            createdAt = DateTimeUtil.nowFormatted(), updatedAt = DateTimeUtil.nowFormatted()
        ))
    }
}
