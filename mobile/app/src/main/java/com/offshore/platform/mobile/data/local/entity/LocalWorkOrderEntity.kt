package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of work_order table.
 *
 * Backend: WorkOrder.java
 * Mobile API: GET /api/mobile/work-orders, MobileWorkOrderListVO
 */
@Entity(
    tableName = "local_work_order",
    indices = [
        Index(value = ["serverId"], unique = false),
        Index(value = ["localId"], unique = true),
        Index(value = ["syncStatus"], unique = false),
        Index(value = ["projectId"], unique = false),
        Index(value = ["status"], unique = false)
    ]
)
data class LocalWorkOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localId: String,
    val serverId: Long? = null,
    val version: Int = 0,
    val updatedAt: String? = null,
    val syncStatus: String = "PENDING",
    val deviceId: String = "",
    val operatorId: Long? = null,
    val deletedFlag: Int = 0,
    val conflictFlag: Int = 0,

    // business fields
    val workOrderNo: String = "",
    val projectId: Long? = null,
    val projectName: String? = null,
    val templateId: Long? = null,
    val workTitle: String = "",
    val workType: String? = null,
    val workLocation: String? = null,
    val workContentSummary: String? = null,
    val requiredMaterialDesc: String? = null,
    val leaderId: Long? = null,
    val leaderName: String? = null,
    val maintainerId: Long? = null,
    val maintainerName: String? = null,
    val plannedStartTime: String? = null,
    val plannedEndTime: String? = null,
    val actualStartTime: String? = null,
    val actualEndTime: String? = null,
    val status: String = "PENDING",
    val priority: String? = null,
    val rejectReason: String? = null,
    val closeReason: String? = null,
    val acceptanceRequired: Int = 0,
    val sourceType: String? = null,
    val abnormalFlag: Int = 0,
    val remark: String? = null,
    val createdAt: String? = null
)
