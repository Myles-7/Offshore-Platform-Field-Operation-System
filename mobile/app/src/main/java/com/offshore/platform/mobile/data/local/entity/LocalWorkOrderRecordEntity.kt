package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of work_order_record.
 *
 * Backend: WorkOrderRecord.java
 * Mobile API: POST/PUT /api/mobile/work-orders/{id}/records
 */
@Entity(
    tableName = "local_work_order_record",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalWorkOrderRecordEntity(
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
    val projectId: Long? = null,
    val recordNo: String? = null,
    val recordType: String? = null,
    val constructionTime: String? = null,
    val constructionUserId: Long? = null,
    val constructionUserName: String? = null,
    val constructionDesc: String? = null,
    val siteCondition: String? = null,
    val abnormalFlag: Int = 0,
    val abnormalDesc: String? = null,
    val weather: String? = null,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val recordStatus: String? = null,
    val remark: String? = null,
    val createdAt: String? = null
)
