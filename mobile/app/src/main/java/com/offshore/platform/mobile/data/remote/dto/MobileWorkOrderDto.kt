package com.offshore.platform.mobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ===================== Work Order =====================

/**
 * Maps backend mobile work-order list/detail fields.
 *
 * Backend: MobileWorkOrderListVO / MobileWorkOrderDetailVO
 * JSON from backend uses camelCase matching field names.
 */
@Serializable
data class MobileWorkOrderDTO(
    val id: Long = 0,
    val serverId: Long? = null,
    val localId: String? = null,
    val workOrderNo: String = "",
    val projectName: String? = null,
    val projectId: Long? = null,
    val workTitle: String = "",
    val workType: String? = null,
    val workLocation: String? = null,
    val workContentSummary: String? = null,
    val workContent: String? = null,
    val requiredMaterialDesc: String? = null,
    val status: String = "PENDING",
    val priority: String? = null,
    val leaderId: Long? = null,
    val leaderName: String? = null,
    val maintainerId: Long? = null,
    val maintainerName: String? = null,
    val plannedStartTime: String? = null,
    val plannedEndTime: String? = null,
    val actualStartTime: String? = null,
    val actualEndTime: String? = null,
    val version: Int? = null,
    val updatedAt: String? = null,
    val syncStatus: String? = null,
    val conflictFlag: Int? = null,
    val abnormalFlag: Int? = null,
    val acceptanceRequired: Int? = null,
    val sourceType: String? = null,
    val templateId: Long? = null,
    val rejectReason: String? = null,
    val closeReason: String? = null,
    val remark: String? = null,
    val createdAt: String? = null,
    // detail-only fields
    val attachments: List<MobileAttachmentDTO>? = null,
    val materials: List<MobileMaterialDTO>? = null,
    val statusFlow: List<MobileStatusFlowDTO>? = null
)

@Serializable
data class MobileAttachmentDTO(
    val id: Long = 0,
    val workOrderId: Long = 0,
    val recordId: Long? = null,
    val fileId: String? = null,
    val attachmentType: String? = null,
    val attachmentName: String? = null,
    val attachmentDesc: String? = null,
    val captureTime: String? = null,
    val captureUserName: String? = null,
    val uploadStatus: String? = null,
    val syncStatus: String? = null,
    val conflictFlag: Int? = null
)

@Serializable
data class MobileMaterialDTO(
    val id: Long = 0,
    val materialCode: String? = null,
    val materialName: String? = null,
    val materialSpec: String? = null,
    val unit: String? = null,
    val plannedQty: String? = null,
    val actualQty: String? = null,
    val prepareStatus: String? = null
)

@Serializable
data class MobileStatusFlowDTO(
    val id: Long = 0,
    val fromStatus: String? = null,
    val toStatus: String? = null,
    val operationType: String? = null,
    val operationDesc: String? = null,
    val operatorId: Long? = null,
    val operationTime: String? = null
)
