package com.offshore.platform.mobile.data.repository

import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*
import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.data.remote.api.MobileWorkOrderApi
import com.offshore.platform.mobile.data.remote.dto.*
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import com.offshore.platform.mobile.util.AppLogger
import kotlinx.serialization.json.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkOrderRepository @Inject constructor(
    private val workOrderApi: MobileWorkOrderApi,
    private val workOrderDao: WorkOrderDao,
    private val syncQueueDao: SyncQueueDao
) : BaseRepository() {

    // ---- list ----

    suspend fun fetchWorkOrders(): NetworkResult<List<MobileWorkOrderDTO>> {
        val result = safeApiCall { workOrderApi.listMyWorkOrders() }
        if (result is NetworkResult.Success) {
            val entities = result.data.map { it.toLocalWorkOrderEntity() }
            workOrderDao.insertAll(entities)
            AppLogger.d("Synced ${entities.size} work orders to local DB")
        }
        return result
    }

    // ---- detail ----

    suspend fun getWorkOrderFromApi(id: Long): NetworkResult<MobileWorkOrderDTO> {
        val result = safeApiCall { workOrderApi.getWorkOrder(id) }
        if (result is NetworkResult.Success) {
            // Also cache detail in local DB
            val entity = result.data.toLocalWorkOrderEntity()
            workOrderDao.insert(entity)
        }
        return result
    }

    // ---- materials ----

    suspend fun fetchMaterials(workOrderId: Long): NetworkResult<List<MobileMaterialDTO>> {
        return safeApiCall { workOrderApi.materials(workOrderId) }
    }

    // ---- qualification check ----

    suspend fun fetchQualificationCheck(workOrderId: Long): NetworkResult<List<JsonObject>> {
        return safeApiCall { workOrderApi.qualificationCheck(workOrderId) }
    }

    // ---- local ----

    fun observeLocalWorkOrders() = workOrderDao.observeAll()
    suspend fun getLocalById(serverId: Long) = workOrderDao.getByServerId(serverId)
    suspend fun getLocalByLocalId(localId: String) = workOrderDao.getByLocalId(localId)

    // ---- status actions (offline-first) ----

    suspend fun acceptWorkOrder(serverId: Long): NetworkResult<WorkOrderStateChange> =
        executeStatusAction(serverId, "ACCEPTED") { workOrderApi.acceptWorkOrder(serverId) }

    suspend fun startWorkOrder(serverId: Long): NetworkResult<WorkOrderStateChange> =
        executeStatusAction(serverId, "IN_PROGRESS") { workOrderApi.startWorkOrder(serverId) }

    suspend fun submitFeedback(
        serverId: Long, desc: String, abnormalFlag: Int, abnormalDesc: String?
    ): NetworkResult<WorkOrderStateChange> =
        executeStatusAction(serverId, "IN_PROGRESS",
            MobileFeedbackDto(feedback = desc, abnormalFlag = abnormalFlag)
        ) { workOrderApi.feedback(serverId, MobileFeedbackDto(desc, abnormalFlag)) }

    suspend fun submitForAcceptance(serverId: Long, desc: String): NetworkResult<WorkOrderStateChange> =
        executeStatusAction(serverId, "PENDING_ACCEPTANCE",
            MobileSubmitAcceptanceDto(submitDesc = desc)
        ) { workOrderApi.submitAcceptance(serverId, MobileSubmitAcceptanceDto(desc)) }

    // --- core offline-first ---

    private suspend fun <T> executeStatusAction(
        serverId: Long,
        newStatus: String,
        payloadDto: Any? = null,
        apiCall: suspend () -> retrofit2.Response<ApiResponse<T>>
    ): NetworkResult<WorkOrderStateChange> {
        val local = workOrderDao.getByServerId(serverId)
            ?: return NetworkResult.BusinessError(404, "工单未本地缓存", null)
        val now = DateTimeUtil.nowFormatted()
        val opId = TokenManager.getUserId().takeIf { it > 0 }

        workOrderDao.update(local.copy(status = newStatus, updatedAt = now, syncStatus = "PENDING", operatorId = opId))

        // Enqueue sync payload as JSON
        val payloadStr = payloadDto?.let { dto ->
            when (dto) {
                is MobileFeedbackDto -> Json.encodeToString(MobileFeedbackDto.serializer(), dto)
                is MobileSubmitAcceptanceDto -> Json.encodeToString(MobileSubmitAcceptanceDto.serializer(), dto)
                else -> Json.encodeToString(JsonObject.serializer(), buildJsonObject { put("_action", "status_change") })
            }
        } ?: Json.encodeToString(JsonObject.serializer(),
            buildJsonObject { put("_action", "status_change") })

        syncQueueDao.enqueue(LocalSyncQueueEntity(
            moduleType = "WORK_ORDER", entityType = "WORK_ORDER",
            localId = local.localId, serverId = serverId, workOrderId = serverId,
            actionType = "UPDATE", payloadJson = payloadStr,
            syncStatus = "PENDING", deviceId = DeviceManager.getOrCreate(),
            operatorId = opId, createdAt = now, updatedAt = now
        ))

        val result = safeApiCall { apiCall() }
        return when (result) {
            is NetworkResult.Success -> {
                workOrderDao.markSynced(local.localId, serverId, local.version + 1)
                AppLogger.d("Status $newStatus for WO#$serverId synced")
                NetworkResult.Success(WorkOrderStateChange(serverId, newStatus, true))
            }
            is NetworkResult.NetworkError -> {
                AppLogger.d("Status $newStatus for WO#$serverId queued (offline)")
                NetworkResult.Success(WorkOrderStateChange(serverId, newStatus, false))
            }
            else -> {
                workOrderDao.update(local.copy(syncStatus = "CONFLICT", conflictFlag = 1, updatedAt = now))
                @Suppress("UNCHECKED_CAST")
                (result as NetworkResult<WorkOrderStateChange>)
            }
        }
    }
}

data class WorkOrderStateChange(val serverId: Long, val newStatus: String, val syncedOnline: Boolean)

/** Map typed DTO to Room entity safely. */
private fun MobileWorkOrderDTO.toLocalWorkOrderEntity(): LocalWorkOrderEntity = LocalWorkOrderEntity(
    localId = "wo-${serverId ?: id}",
    serverId = serverId ?: id,
    version = version ?: 0,
    updatedAt = updatedAt,
    syncStatus = syncStatus ?: "SYNCED",
    conflictFlag = conflictFlag ?: 0,
    workOrderNo = workOrderNo,
    projectId = projectId ?: 0,
    projectName = projectName,
    workTitle = workTitle,
    workType = workType,
    workLocation = workLocation,
    workContentSummary = workContentSummary,
    leaderId = leaderId,
    leaderName = leaderName,
    maintainerId = maintainerId,
    maintainerName = maintainerName,
    plannedStartTime = plannedStartTime,
    plannedEndTime = plannedEndTime,
    actualStartTime = actualStartTime,
    actualEndTime = actualEndTime,
    status = status,
    priority = priority,
    abnormalFlag = abnormalFlag ?: 0,
    createdAt = createdAt
)
