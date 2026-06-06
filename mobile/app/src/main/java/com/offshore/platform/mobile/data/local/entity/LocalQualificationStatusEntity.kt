package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of employee_certificate + qualification check results.
 *
 * Backend: EmployeeCertificate.java, WorkOrderQualificationCheck.java
 * Mobile API: GET /api/mobile/my/qualification-status
 */
@Entity(
    tableName = "local_qualification_status",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["employeeId"], unique = false),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalQualificationStatusEntity(
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
    val employeeId: Long? = null,
    val userId: Long? = null,
    val employeeNo: String? = null,
    val realName: String? = null,
    val qualificationTypeId: Long? = null,
    val certificateId: Long? = null,
    val certificateNo: String? = null,
    val certificateName: String? = null,
    val validTo: String? = null,
    val validStatus: String = "VALID",
    val warningLevel: String? = null,
    val checkResult: String? = null,
    val checkMessage: String? = null,
    val workOrderId: Long? = null,
    val fileId: String? = null,
    val createdAt: String? = null
)
