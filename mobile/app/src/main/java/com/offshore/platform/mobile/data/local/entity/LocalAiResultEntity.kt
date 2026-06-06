package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of ai_result.
 *
 * Backend: AiResult.java
 * Mobile API: GET /api/mobile/work-orders/{id}/ai-results
 */
@Entity(
    tableName = "local_ai_result",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalAiResultEntity(
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
    val attachmentId: Long? = null,
    val aiResultNo: String? = null,
    val modelId: Long? = null,
    val modelCode: String? = null,
    val modelVersion: String? = null,
    val defectType: String? = null,
    val confidence: Double = 0.0,
    val suspectedDefectFlag: Int = 0,
    val defectCount: Int = 0,
    val resultSummary: String? = null,
    val inferCostMs: Long? = null,
    val reviewStatus: String = "PENDING_REVIEW",
    val reviewedFlag: Int = 0,
    val auxiliaryNotice: String? = null,
    val boxesJson: String? = null,   // serialised List<AiDefectBox>
    val createdAt: String? = null
)
