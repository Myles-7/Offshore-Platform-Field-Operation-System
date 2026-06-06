package com.offshore.platform.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local mirror of knowledge_case.
 *
 * Backend: KnowledgeCase.java (if exists)
 * Mobile API: GET /api/mobile/knowledge/cases
 */
@Entity(
    tableName = "local_knowledge_case",
    indices = [
        Index(value = ["localId"], unique = true),
        Index(value = ["syncStatus"], unique = false)
    ]
)
data class LocalKnowledgeCaseEntity(
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
    val caseNo: String? = null,
    val title: String = "",
    val caseType: String? = null,
    val description: String? = null,
    val solution: String? = null,
    val keywords: String? = null,
    val createdAt: String? = null
)
