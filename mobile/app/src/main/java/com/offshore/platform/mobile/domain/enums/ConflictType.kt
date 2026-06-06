package com.offshore.platform.mobile.domain.enums

import androidx.compose.ui.graphics.Color
import com.offshore.platform.mobile.ui.theme.*

/**
 * Sync conflict type — matches backend.
 *
 * Backend: sync_conflict.conflict_type
 * Database: database-docs/02_Status_Enum_Dictionary.md
 */
enum class ConflictType(val code: String, val displayName: String, val color: Color) {
    VERSION_CONFLICT("VERSION_CONFLICT", "版本冲突", StatusWarning),
    FIELD_CONFLICT("FIELD_CONFLICT", "字段冲突", StatusWarning),
    UPDATE_AFTER_DELETE("UPDATE_AFTER_DELETE", "删除后更新", StatusDanger),
    DELETE_AFTER_UPDATE("DELETE_AFTER_UPDATE", "更新后删除", StatusDanger),
    DUPLICATE_CREATE("DUPLICATE_CREATE", "重复创建", StatusWarning),
    PERMISSION_CONFLICT("PERMISSION_CONFLICT", "权限冲突", StatusDanger),
    FILE_META_CONFLICT("FILE_META_CONFLICT", "文件元数据冲突", StatusWarning),
    ACCEPTANCE_LOCKED_CONFLICT("ACCEPTANCE_LOCKED_CONFLICT", "验收锁定冲突", StatusDanger);

    companion object {
        fun fromCode(code: String): ConflictType =
            entries.find { it.code == code } ?: VERSION_CONFLICT

        fun fromCodeOrNull(code: String?): ConflictType? =
            code?.let { entries.find { e -> e.code == it } }
    }
}
