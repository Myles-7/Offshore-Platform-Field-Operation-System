package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of work_order_pdf.
 *
 * Backend: WorkOrderPdf.java
 * Mobile API: POST /api/mobile/work-orders/{id}/pdf/metadata
 */
@Entity(
    tableName = "local_pdf",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalPdfEntity(
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
    val pdfNo: String? = null,
    val pdfTitle: String? = null,
    val pdfStatus: String = "GENERATING",
    val generatedAt: String? = null,
    val generatorName: String? = null,
    val pageCount: Int = 0,
    // local file path
    val localFilePath: String? = null,
    val fileId: String? = null,
    val fileSize: Long = 0,
    val archivedFlag: Int = 0,
    val remark: String? = null,
    val createdAt: String? = null
)
