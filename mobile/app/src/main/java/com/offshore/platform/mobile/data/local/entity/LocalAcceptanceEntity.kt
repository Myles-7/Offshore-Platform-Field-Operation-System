package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of work_order_acceptance.
 *
 * Backend: WorkOrderAcceptance.java
 * Mobile API: POST /api/mobile/work-orders/{id}/acceptance
 */
@Entity(
    tableName = "local_acceptance",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalAcceptanceEntity(
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
    val workOrderId: Long,
    val acceptorId: Long? = null,
    val acceptorName: String? = null,
    val acceptanceTime: String? = null,
    val acceptanceResult: String? = null,
    val acceptanceOpinion: String? = null,
    val acceptanceStatus: String = "PENDING",
    val reviewStatus: String? = null,
    val reviewOpinion: String? = null,
    val signatureFilePath: String? = null,
    val lockedFlag: Int = 0,
    val remark: String? = null,
    val createdAt: String? = null
)
