package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of work_order_material (requirements).
 *
 * Backend: WorkOrderMaterial.java
 * Mobile API: GET /api/mobile/work-orders/{id}/materials
 */
@Entity(
    tableName = "local_material_requirement",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["materialId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalMaterialRequirementEntity(
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
    val materialId: Long? = null,
    val materialCode: String? = null,
    val materialName: String = "",
    val materialSpec: String? = null,
    val unit: String? = null,
    val plannedQty: Double? = null,
    val actualQty: Double? = null,
    val prepareStatus: String? = null,
    val remark: String? = null,
    val createdAt: String? = null
)
