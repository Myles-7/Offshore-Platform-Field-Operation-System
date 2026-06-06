package com.offshore.platform.mobile.data.local

import com.offshore.platform.mobile.data.local.entity.*
import kotlinx.serialization.json.*
import java.util.UUID

/**
 * Extension functions to parse server-sent JSON (SyncPullItem payload)
 * into Room entities.
 *
 * Fields map: localId → auto-generated if absent, serverId ← serverId from pull item,
 * version/updatedAt/syncStatus ← server values, syncStatus forced to "SYNCED".
 */
fun JsonObject.toWorkOrderEntity(
    serverId: Long, version: Int, updatedAt: String,
    operatorId: Long, deviceId: String, deletedFlag: Int, now: String
): LocalWorkOrderEntity {
    val s = parser()
    return LocalWorkOrderEntity(
        localId = s("localId") ?: "wo-${UUID.randomUUID()}",
        serverId = serverId, version = version,
        updatedAt = updatedAt, syncStatus = "SYNCED",
        deviceId = deviceId, operatorId = operatorId,
        deletedFlag = deletedFlag, conflictFlag = 0,
        workOrderNo = s("workOrderNo") ?: "",
        projectId = s("projectId")?.toLongOrNull(),
        projectName = s("projectName"),
        workTitle = s("workTitle") ?: "",
        workType = s("workType"),
        workLocation = s("workLocation"),
        workContentSummary = s("workContentSummary"),
        leaderId = s("leaderId")?.toLongOrNull(),
        leaderName = s("leaderName"),
        maintainerId = s("maintainerId")?.toLongOrNull(),
        maintainerName = s("maintainerName"),
        plannedStartTime = s("plannedStartTime"),
        plannedEndTime = s("plannedEndTime"),
        status = s("status") ?: "PENDING",
        priority = s("priority"),
        abnormalFlag = s("abnormalFlag")?.toIntOrNull() ?: 0,
        createdAt = s("createdAt") ?: now
    )
}

fun JsonObject.toWorkRecordEntity(
    serverId: Long, version: Int, updatedAt: String,
    operatorId: Long, deviceId: String, deletedFlag: Int, now: String
): LocalWorkOrderRecordEntity {
    val s = parser()
    return LocalWorkOrderRecordEntity(
        localId = s("localId") ?: "rec-${UUID.randomUUID()}",
        serverId = serverId, version = version,
        updatedAt = updatedAt, syncStatus = "SYNCED",
        deviceId = deviceId, operatorId = operatorId,
        deletedFlag = deletedFlag, conflictFlag = 0,
        workOrderId = s("workOrderId")?.toLongOrNull() ?: 0,
        constructionTime = s("constructionTime"),
        constructionUserId = s("constructionUserId")?.toLongOrNull(),
        constructionUserName = s("constructionUserName"),
        constructionDesc = s("constructionDesc"),
        siteCondition = s("siteCondition"),
        abnormalFlag = s("abnormalFlag")?.toIntOrNull() ?: 0,
        abnormalDesc = s("abnormalDesc"),
        weather = s("weather"),
        temperature = s("temperature")?.toDoubleOrNull(),
        humidity = s("humidity")?.toDoubleOrNull(),
        locationName = s("locationName"),
        latitude = s("latitude")?.toDoubleOrNull(),
        longitude = s("longitude")?.toDoubleOrNull(),
        createdAt = s("createdAt") ?: now
    )
}

fun JsonObject.toAttachmentEntity(
    serverId: Long, version: Int, updatedAt: String,
    operatorId: Long, deviceId: String, deletedFlag: Int, now: String
): LocalWorkOrderAttachmentEntity {
    val s = parser()
    val l = { k: String -> s(k)?.toLongOrNull() }
    return LocalWorkOrderAttachmentEntity(
        localId = s("localId") ?: "att-${UUID.randomUUID()}",
        serverId = serverId, version = version,
        updatedAt = updatedAt, syncStatus = "SYNCED",
        deviceId = deviceId, operatorId = operatorId,
        deletedFlag = deletedFlag, conflictFlag = 0,
        workOrderId = l("workOrderId") ?: 0L,
        recordId = l("recordId"),
        fileId = s("fileId") ?: "",
        attachmentType = s("attachmentType") ?: "PHOTO",
        attachmentName = s("attachmentName") ?: "",
        attachmentDesc = s("attachmentDesc"),
        captureTime = s("captureTime"),
        captureUserName = s("captureUserName"),
        watermarkFlag = s("watermarkFlag")?.toIntOrNull() ?: 0,
        watermarkText = s("watermarkText"),
        durationSeconds = s("durationSeconds")?.toIntOrNull() ?: 0,
        mediaWidth = s("mediaWidth")?.toIntOrNull() ?: 0,
        mediaHeight = s("mediaHeight")?.toIntOrNull() ?: 0,
        uploadStatus = s("uploadStatus") ?: "UPLOADED",
        createdAt = s("createdAt") ?: now
    )
}

/** Helper to extract nullable string from JsonObject. */
@Suppress("NOTHING_TO_INLINE")
private inline fun parser(): JsonObject.(String) -> String? = { k ->
    (this[k] as? JsonPrimitive)?.content
}
