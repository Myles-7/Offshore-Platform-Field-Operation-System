package com.offshore.platform.mobile.data.local

import com.offshore.platform.mobile.data.local.dao.*
import com.offshore.platform.mobile.data.local.entity.*
import com.offshore.platform.mobile.data.remote.dto.SyncPullItem
import com.offshore.platform.mobile.util.AppLogger
import com.offshore.platform.mobile.util.DateTimeUtil
import com.offshore.platform.mobile.util.DeviceManager
import com.offshore.platform.mobile.util.TokenManager
import kotlinx.serialization.json.*
import java.util.UUID

/**
 * Maps [SyncPullItem] payloads → Room entities, then upserts into the appropriate DAO.
 *
 * Upsert rules:
 *  - serverId not found locally → insert
 *  - serverId found, local syncStatus == SYNCED → overwrite with server version
 *  - serverId found, local syncStatus in PENDING/FAILED/SYNCING → mark potential conflict
 *  - deletedFlag == true on server → local logical delete
 *  - conflictResolved → update local conflict status
 */
class SyncDataMapper(
    private val workOrderDao: WorkOrderDao,
    private val workOrderRecordDao: WorkOrderRecordDao,
    private val attachmentDao: AttachmentDao,
    private val materialRequirementDao: MaterialRequirementDao,
    private val qualificationStatusDao: QualificationStatusDao,
    private val aiResultDao: AiResultDao,
    private val knowledgeCaseDao: KnowledgeCaseDao,
    private val conflictHintDao: ConflictHintDao
) {

    /** Process a single pull item, dispatching by entityType. Returns true on success. */
    suspend fun processPullItem(item: SyncPullItem, operatorId: Long, deviceId: String, now: String): Boolean {
        val payload = item.payload ?: run {
            AppLogger.w("PullItem entityType=${item.entityType} serverId=${item.serverId} has null payload")
            return false
        }

        return try {
            when {
                item.entityType.startsWith("WORK_ORDER_ATTACHMENT") -> {
                    upsertAttachment(payload, item.serverId, item.version, item.updatedAt ?: now, operatorId, deviceId, now)
                }
                item.entityType.startsWith("WORK_ORDER_RECORD") -> {
                    upsertWorkRecord(payload, item.serverId, item.version, item.updatedAt ?: now, operatorId, deviceId, now)
                }
                item.entityType.startsWith("WORK_ORDER") -> {
                    upsertWorkOrder(payload, item.serverId, item.version, item.updatedAt ?: now, operatorId, deviceId, now)
                }
                item.entityType.startsWith("MATERIAL_REQUIREMENT") || item.entityType.startsWith("WORK_ORDER_MATERIAL") -> {
                    upsertMaterialRequirement(payload, item.serverId, item.version, item.updatedAt ?: now, operatorId, deviceId, now)
                }
                item.entityType.startsWith("QUALIFICATION") || item.entityType.startsWith("CERTIFICATE") -> {
                    upsertQualification(payload, item.serverId, item.version, item.updatedAt ?: now, operatorId, deviceId, now)
                }
                item.entityType.startsWith("AI_RESULT") -> {
                    upsertAiResult(payload, item.serverId, item.version, item.updatedAt ?: now, operatorId, deviceId, now)
                }
                item.entityType.startsWith("KNOWLEDGE") -> {
                    upsertKnowledge(payload, item.serverId, item.version, item.updatedAt ?: now, operatorId, deviceId, now)
                }
                item.entityType.startsWith("CONFLICT_RESOLVED") || item.entityType.startsWith("SYNC_CONFLICT") -> {
                    upsertConflictResolved(payload, item.serverId, item.version, item.updatedAt ?: now, operatorId, deviceId, now)
                }
                item.entityType.startsWith("DELETED") -> {
                    applyLogicalDelete(payload, item.serverId, item.updatedAt ?: now)
                }
                else -> {
                    AppLogger.d("Unhandled pull entityType: ${item.entityType}")
                    false
                }
            }
            true
        } catch (e: Exception) {
            AppLogger.e("Failed to process pull item entityType=${item.entityType} serverId=${item.serverId}", e)
            false
        }
    }

    // ---- upsert helpers ----

    private suspend fun upsertWorkOrder(
        payload: JsonObject, serverId: Long, version: Int, updatedAt: String,
        operatorId: Long, deviceId: String, now: String
    ) {
        val existing = workOrderDao.getByServerId(serverId)
        if (existing != null && existing.syncStatus != "SYNCED") {
            // Local has pending changes — potential conflict
            workOrderDao.insert(
                existing.copy(
                    conflictFlag = 1,
                    syncStatus = "CONFLICT",
                    updatedAt = now
                )
            )
            AppLogger.d("Pull WO#$serverId: local pending, marked CONFLICT")
            return
        }
        val deletedFlag = payload["deletedFlag"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
        val entity = payload.toWorkOrderEntity(serverId, version, updatedAt, operatorId, deviceId, deletedFlag, now)
        workOrderDao.insert(entity)
    }

    private suspend fun upsertWorkRecord(
        payload: JsonObject, serverId: Long, version: Int, updatedAt: String,
        operatorId: Long, deviceId: String, now: String
    ) {
        val existing = workOrderRecordDao.getByServerId(serverId)
        if (existing != null && existing.syncStatus != "SYNCED") {
            workOrderRecordDao.update(existing.copy(conflictFlag = 1, syncStatus = "CONFLICT", updatedAt = now))
            AppLogger.d("Pull Record#$serverId: local pending, marked CONFLICT")
            return
        }
        val deletedFlag = payload["deletedFlag"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
        val entity = payload.toWorkRecordEntity(serverId, version, updatedAt, operatorId, deviceId, deletedFlag, now)
        workOrderRecordDao.insert(entity)
    }

    private suspend fun upsertAttachment(
        payload: JsonObject, serverId: Long, version: Int, updatedAt: String,
        operatorId: Long, deviceId: String, now: String
    ) {
        val existing = attachmentDao.getByServerId(serverId)
        if (existing != null && existing.syncStatus != "SYNCED") {
            attachmentDao.update(existing.copy(conflictFlag = 1, syncStatus = "CONFLICT", updatedAt = now))
            AppLogger.d("Pull Attachment#$serverId: local pending, marked CONFLICT")
            return
        }
        val deletedFlag = payload["deletedFlag"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
        val entity = payload.toAttachmentEntity(serverId, version, updatedAt, operatorId, deviceId, deletedFlag, now)
        attachmentDao.insert(entity)
    }

    private suspend fun upsertMaterialRequirement(
        payload: JsonObject, serverId: Long, version: Int, updatedAt: String,
        operatorId: Long, deviceId: String, now: String
    ) {
        val s = { k: String -> (payload[k] as? JsonPrimitive)?.content }
        val l = { k: String -> (payload[k] as? JsonPrimitive)?.content?.toLongOrNull() }
        val entity = LocalMaterialRequirementEntity(
            localId = s("localId") ?: "mr-${UUID.randomUUID()}",
            serverId = serverId,
            version = version,
            updatedAt = updatedAt,
            syncStatus = "SYNCED",
            deviceId = deviceId,
            operatorId = operatorId,
            workOrderId = l("workOrderId") ?: 0,
            materialId = l("materialId"),
            materialCode = s("materialCode"),
            materialName = s("materialName") ?: "",
            materialSpec = s("materialSpec"),
            unit = s("unit"),
            plannedQty = s("plannedQty")?.toDoubleOrNull(),
            actualQty = s("actualQty")?.toDoubleOrNull(),
            prepareStatus = s("prepareStatus"),
            createdAt = s("createdAt") ?: now
        )
        materialRequirementDao.insertAll(listOf(entity))
    }

    private suspend fun upsertQualification(
        payload: JsonObject, serverId: Long, version: Int, updatedAt: String,
        operatorId: Long, deviceId: String, now: String
    ) {
        val s = { k: String -> (payload[k] as? JsonPrimitive)?.content }
        val l = { k: String -> (payload[k] as? JsonPrimitive)?.content?.toLongOrNull() }
        val entity = LocalQualificationStatusEntity(
            localId = s("localId") ?: "qual-${UUID.randomUUID()}",
            serverId = serverId,
            version = version,
            updatedAt = updatedAt,
            syncStatus = "SYNCED",
            deviceId = deviceId,
            operatorId = operatorId,
            employeeId = l("employeeId"),
            userId = l("userId") ?: l("id"),
            certificateName = s("certificateName"),
            certificateNo = s("certificateNo"),
            validTo = s("validTo"),
            validStatus = s("validStatus") ?: "VALID",
            checkResult = s("checkResult"),
            checkMessage = s("message"),
            workOrderId = l("workOrderId"),
            createdAt = s("createdAt") ?: now
        )
        qualificationStatusDao.insertAll(listOf(entity))
    }

    private suspend fun upsertAiResult(
        payload: JsonObject, serverId: Long, version: Int, updatedAt: String,
        operatorId: Long, deviceId: String, now: String
    ) {
        val s = { k: String -> (payload[k] as? JsonPrimitive)?.content }
        val l = { k: String -> (payload[k] as? JsonPrimitive)?.content?.toLongOrNull() }
        val entity = LocalAiResultEntity(
            localId = s("localId") ?: "ai-${UUID.randomUUID()}",
            serverId = serverId,
            version = version,
            updatedAt = updatedAt,
            syncStatus = "SYNCED",
            deviceId = deviceId,
            operatorId = operatorId,
            workOrderId = l("workOrderId") ?: 0,
            recordId = l("recordId"),
            attachmentId = l("attachmentId"),
            aiResultNo = s("aiResultNo"),
            modelId = l("modelId"),
            modelCode = s("modelCode"),
            modelVersion = s("modelVersion"),
            defectType = s("defectType"),
            confidence = s("confidence")?.toDoubleOrNull() ?: 0.0,
            suspectedDefectFlag = s("suspectedDefectFlag")?.toIntOrNull() ?: 0,
            defectCount = s("defectCount")?.toIntOrNull() ?: 0,
            resultSummary = s("resultSummary"),
            reviewStatus = s("reviewStatus") ?: "PENDING_REVIEW",
            createdAt = s("createdAt") ?: now
        )
        aiResultDao.insertAll(listOf(entity))
    }

    private suspend fun upsertKnowledge(
        payload: JsonObject, serverId: Long, version: Int, updatedAt: String,
        operatorId: Long, deviceId: String, now: String
    ) {
        val s = { k: String -> (payload[k] as? JsonPrimitive)?.content }
        val entity = LocalKnowledgeCaseEntity(
            localId = s("localId") ?: "kn-${UUID.randomUUID()}",
            serverId = serverId,
            version = version,
            updatedAt = updatedAt,
            syncStatus = "SYNCED",
            deviceId = deviceId,
            operatorId = operatorId,
            caseNo = s("caseNo"),
            title = s("title") ?: "",
            caseType = s("caseType"),
            description = s("description"),
            solution = s("solution"),
            keywords = s("keywords"),
            createdAt = s("createdAt") ?: now
        )
        knowledgeCaseDao.insertAll(listOf(entity))
    }

    private suspend fun upsertConflictResolved(
        payload: JsonObject, serverId: Long, version: Int, updatedAt: String,
        operatorId: Long, deviceId: String, now: String
    ) {
        val s = { k: String -> (payload[k] as? JsonPrimitive)?.content }
        val l = { k: String -> (payload[k] as? JsonPrimitive)?.content?.toLongOrNull() }
        val entity = LocalConflictHintEntity(
            localId = s("localId") ?: "ch-${UUID.randomUUID()}",
            serverId = serverId,
            version = version,
            updatedAt = updatedAt,
            syncStatus = "SYNCED",
            deviceId = deviceId,
            operatorId = operatorId,
            workOrderId = l("workOrderId"),
            conflictNo = s("conflictNo"),
            conflictType = s("conflictType"),
            entityType = s("entityType"),
            serverVersion = s("serverVersion")?.toIntOrNull() ?: 0,
            clientVersion = s("clientVersion")?.toIntOrNull() ?: 0,
            resolveStatus = s("resolveStatus") ?: "RESOLVED",
            clientPayload = s("clientPayload"),
            serverPayload = s("serverPayload"),
            message = s("message"),
            createdAt = s("createdAt") ?: now
        )
        conflictHintDao.insertAll(listOf(entity))
        AppLogger.d("Conflict resolved for serverId=$serverId")
    }

    private suspend fun applyLogicalDelete(
        payload: JsonObject, serverId: Long, now: String
    ) {
        val entityType = (payload["entityType"] as? JsonPrimitive)?.content ?: return
        when {
            entityType.startsWith("WORK_ORDER") -> {
                val wo = workOrderDao.getByServerId(serverId) ?: return
                workOrderDao.insert(wo.copy(deletedFlag = 1, updatedAt = now))
            }
            entityType.startsWith("WORK_ORDER_RECORD") -> {
                val rec = workOrderRecordDao.getByServerId(serverId) ?: return
                workOrderRecordDao.update(rec.copy(deletedFlag = 1, updatedAt = now))
            }
            entityType.startsWith("WORK_ORDER_ATTACHMENT") -> {
                val att = attachmentDao.getByServerId(serverId) ?: return
                attachmentDao.update(att.copy(deletedFlag = 1, updatedAt = now))
            }
            entityType.startsWith("KNOWLEDGE") -> {
                val all = knowledgeCaseDao.getAllList()
                val kn = all.find { it.serverId == serverId } ?: return
                knowledgeCaseDao.insertAll(listOf(kn.copy(deletedFlag = 1, updatedAt = now)))
            }
        }
        AppLogger.d("Logical delete applied: $entityType serverId=$serverId")
    }

    // DAO getByServerId queries are defined directly on each DAO interface
    // (WorkOrderDao, WorkOrderRecordDao, AttachmentDao, etc.) —
    // no extension functions needed here.
}
