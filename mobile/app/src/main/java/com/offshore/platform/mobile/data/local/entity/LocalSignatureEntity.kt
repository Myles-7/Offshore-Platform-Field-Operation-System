package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of work_order_signature.
 *
 * Backend: WorkOrderSignature.java
 * Mobile API: POST /api/mobile/work-orders/{id}/signatures
 */
@Entity(
    tableName = "local_signature",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalSignatureEntity(
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
    val acceptanceId: Long? = null,
    val signerName: String? = null,
    val signerRole: String? = null,
    val signatureType: String? = null,
    val signatureTime: String? = null,
    val signatureDevice: String? = null,
    // local file path (signature image)
    val localFilePath: String? = null,
    val fileId: String? = null,
    val remark: String? = null,
    val createdAt: String? = null
)
