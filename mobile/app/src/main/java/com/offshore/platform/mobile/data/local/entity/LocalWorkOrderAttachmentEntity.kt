package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of work_order_attachment.
 *
 * Only stores metadata — file binary is on local filesystem.
 * Backend: WorkOrderAttachment.java
 */
@Entity(
    tableName = "local_attachment",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["workOrderId"], unique = false),
        Index(value = ["recordId"], unique = false),
        Index(value = ["fileId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalWorkOrderAttachmentEntity(
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
    val fileId: String = "",
    val attachmentType: String = "PHOTO",
    val attachmentName: String = "",
    val attachmentDesc: String? = null,
    val businessScene: String? = null,
    val captureTime: String? = null,
    val captureUserName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val watermarkFlag: Int = 1,
    val watermarkText: String? = null,
    val durationSeconds: Int = 0,
    val mediaWidth: Int = 0,
    val mediaHeight: Int = 0,
    // local file info
    val localFilePath: String? = null,
    val localThumbnailPath: String? = null,
    val fileSize: Long = 0,
    val mimeType: String? = null,
    val uploadStatus: String = "PENDING",
    val uploadRetryCount: Int = 0,
    val remark: String? = null,
    val createdAt: String? = null
)
