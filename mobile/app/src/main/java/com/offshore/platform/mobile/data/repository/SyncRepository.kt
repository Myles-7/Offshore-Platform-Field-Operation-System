package com.offshore.platform.mobile.data.repository

import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*
import com.offshore.platform.mobile.data.local.SyncDataMapper
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.remote.api.SyncApi
import com.offshore.platform.mobile.data.remote.dto.*
import com.offshore.platform.mobile.util.AppLogger
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import kotlinx.serialization.json.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val syncQueueDao: SyncQueueDao,
    private val syncLogDao: SyncLogDao,
    private val syncCheckpointDao: SyncCheckpointDao,
    private val deviceInfoDao: DeviceInfoDao,
    private val workOrderDao: WorkOrderDao,
    private val workOrderRecordDao: WorkOrderRecordDao,
    private val attachmentDao: AttachmentDao,
    private val materialRequirementDao: MaterialRequirementDao,
    private val qualificationStatusDao: QualificationStatusDao,
    private val aiResultDao: AiResultDao,
    private val knowledgeCaseDao: KnowledgeCaseDao,
    private val conflictHintDao: ConflictHintDao
) : BaseRepository() {

    private val mapper by lazy {
        SyncDataMapper(
            workOrderDao, workOrderRecordDao, attachmentDao,
            materialRequirementDao, qualificationStatusDao,
            aiResultDao, knowledgeCaseDao, conflictHintDao
        )
    }

    suspend fun fullSync(): NetworkResult<SyncSummary> {
        val deviceId = DeviceManager.getOrCreate()
        if (TokenManager.getToken().isNullOrBlank())
            return NetworkResult.BusinessError(401, "未登录，无法同步", null)

        val pushResult = pushLocalChanges(deviceId)
        val pullResult = pullServerChanges(deviceId)

        val now = DateTimeUtil.nowFormatted()
        val summary = SyncSummary(
            pushSuccess = pushResult is NetworkResult.Success,
            pullSuccess = pullResult is NetworkResult.Success,
            pushCount = (pushResult as? NetworkResult.Success<SyncPushResponse>)?.data?.successCount ?: 0,
            pullCount = pullResult.getOrNull()?.items?.size ?: 0,
            lastSyncTime = now
        )

        syncLogDao.insert(LocalSyncLogEntity(
            localId = UUID.randomUUID().toString(),
            batchId = "batch-${DateTimeUtil.fileNameTimestamp()}",
            syncDirection = "FULL", syncType = "INCREMENTAL",
            syncStatus = if (pushResult.isSuccess && pullResult.isSuccess) "SYNCED" else "PARTIAL",
            successCount = summary.pushCount + summary.pullCount,
            failedCount = if (pullResult.isSuccess) 0 else 1,
            conflictCount = 0,
            totalItems = summary.pushCount + summary.pullCount,
            deviceId = deviceId,
            operatorId = TokenManager.getUserId().takeIf { it > 0 },
            createdAt = now
        ))
        return NetworkResult.Success(summary)
    }

    // ---- push ----

    private suspend fun pushLocalChanges(deviceId: String): NetworkResult<SyncPushResponse> {
        syncQueueDao.resetFailedForRetry()
        val nowMillis = System.currentTimeMillis()
        val pendingItems = syncQueueDao.getPendingBatch(nowMillis, limit = 50)
        if (pendingItems.isEmpty())
            return NetworkResult.Success(SyncPushResponse(successCount = 0, failedCount = 0, conflictCount = 0, items = emptyList()))

        val batchId = "push-${UUID.randomUUID().toString().take(8)}"
        val pushItems = pendingItems.map { entity ->
            SyncPushItem(
                moduleType = entity.moduleType, entityType = entity.entityType,
                actionType = entity.actionType, localId = entity.localId,
                serverId = entity.serverId, version = 0, updatedAt = entity.updatedAt,
                payload = entity.payloadJson?.let { Json.decodeFromString<JsonObject>(it) },
                fileId = entity.fileId, checksum = entity.checksum
            )
        }
        val request = SyncPushRequest(
            deviceId = deviceId, batchId = batchId,
            clientTime = DateTimeUtil.nowFormatted(), appVersion = "1.0.0", items = pushItems
        )
        val result = safeApiCall { syncApi.push(request) }

        when (result) {
            is NetworkResult.Success -> {
                result.data.items.forEach { item ->
                    val localId = item.localId ?: return@forEach
                    val entity = pendingItems.find { it.localId == localId } ?: return@forEach
                    when (item.syncStatus.orEmpty().uppercase()) {
                        "SUCCESS" -> {
                            syncQueueDao.markSynced(entity.queueId)
                            entity.serverId?.let { sid -> item.serverId?.let { srvId ->
                                when {
                                    entity.entityType.contains("WORK_ORDER_RECORD") ->
                                        workOrderRecordDao.markSynced(localId, srvId, item.version)
                                    entity.entityType.contains("WORK_ORDER") ->
                                        workOrderDao.markSynced(localId, srvId, item.version)
                                }
                            } }
                        }
                        "FAILED" -> syncQueueDao.updateAfterAttempt(
                            entity.queueId, "FAILED", item.message, System.currentTimeMillis() + 60_000L
                        )
                        "CONFLICT" -> syncQueueDao.updateAfterAttempt(
                            entity.queueId, "CONFLICT", item.message ?: "冲突", 0
                        )
                    }
                }
            }
            is NetworkResult.NetworkError -> { /* retry later */ }
            else -> pendingItems.forEach { entity ->
                syncQueueDao.updateAfterAttempt(entity.queueId, "FAILED",
                    (result as? NetworkResult.BusinessError)?.message ?: "同步失败",
                    System.currentTimeMillis() + 60_000L)
            }
        }
        return result
    }

    // ---- pull ----

    private suspend fun pullServerChanges(deviceId: String): NetworkResult<SyncPullResponse> {
        val checkpoints = syncCheckpointDao.getAll()
        val cursor = checkpoints.mapNotNull { it.lastServerCursor }.minByOrNull { it }
            ?: checkpoints.mapNotNull { it.lastSyncTime }.minByOrNull { it }
            ?: "2000-01-01 00:00:00"

        val request = SyncPullRequest(deviceId = deviceId, cursor = cursor, limit = 200)
        val result = safeApiCall { syncApi.pull(request) }
        if (result !is NetworkResult.Success) return result

        val response = result.data
        val serverCursor = response.cursor ?: DateTimeUtil.nowFormatted()
        val operatorId = TokenManager.getUserId().takeIf { it > 0 } ?: 0L
        val now = DateTimeUtil.nowFormatted()

        // Convert structured SyncPullItemVOs for mapper
        var allSucceeded = true
        val ackIds = mutableListOf<Long>()

        for (item in response.items) {
            val entityType = item.entityType ?: continue
            val serverId = item.serverId ?: continue
            val version = item.version
            val updatedAt = item.updatedAt
            val payload = item.payload ?: continue

            val pullItem = SyncPullItem(
                entityType = entityType,
                serverId = serverId,
                version = version,
                updatedAt = updatedAt,
                payload = payload
            )
            val ok = mapper.processPullItem(pullItem, operatorId, deviceId, now)
            if (ok) ackIds.add(serverId)
            else allSucceeded = false
        }

        if (ackIds.isNotEmpty() && allSucceeded) {
            val ackResult = safeApiCall {
                syncApi.ack(SyncAckRequest(deviceId, "pull-ack-${UUID.randomUUID().toString().take(8)}", cursor = serverCursor))
            }
            if (ackResult is NetworkResult.Success) {
                AppLogger.d("Pull -> ACK succeeded: ${ackIds.size} items")
            } else {
                AppLogger.w("Pull -> ACK failed, retry next pull")
                allSucceeded = false
            }
        }

        if (allSucceeded) {
            syncCheckpointDao.upsert(LocalSyncCheckpointEntity(
                moduleType = "GENERAL", lastSyncTime = serverCursor,
                lastServerCursor = serverCursor, lastSuccessTime = now, updatedAt = now
            ))
        }
        return NetworkResult.Success(response)
    }

    suspend fun getRecentSyncLogs(limit: Int = 20) = syncLogDao.getRecent(limit)
    suspend fun getPendingCount() = syncQueueDao.pendingCount()
    suspend fun getCheckpoints() = syncCheckpointDao.getAll()

    /** Retry a single failed queue item. */
    suspend fun retryQueueItem(queueId: Long) {
        syncQueueDao.updateAfterAttempt(queueId, "PENDING", null, 0L)
    }
}

data class SyncSummary(
    val pushSuccess: Boolean,
    val pullSuccess: Boolean,
    val pushCount: Int,
    val pullCount: Int,
    val lastSyncTime: String
)
