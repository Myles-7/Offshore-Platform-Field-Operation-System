package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of work_order_material_usage.
 *
 * Backend: WorkOrderMaterialUsage.java
 * Mobile API: POST /api/mobile/work-orders/{id}/material-usage
 */
@Entity(
    tableName = "local_material_usage",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalMaterialUsageEntity(
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
    val recordId: Long? = null,
    val materialId: Long? = null,
    val materialCode: String? = null,
    val materialName: String = "",
    val unit: String? = null,
    val usageQty: Double = 0.0,
    val usageTime: String? = null,
    val batchNo: String? = null,
    val remark: String? = null,
    val createdAt: String? = null
)
