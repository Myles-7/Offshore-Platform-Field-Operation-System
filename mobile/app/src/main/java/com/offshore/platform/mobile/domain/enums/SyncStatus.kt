package com.offshore.platform.mobile.domain.enums

import androidx.compose.ui.graphics.Color
import com.offshore.platform.mobile.ui.theme.*

/**
 * Sync status — matches backend and PC frontend syncStatusOptions.
 *
 * All syncable entities carry this status.
 */
enum class SyncStatus(val code: String, val displayName: String, val color: Color) {
    LOCAL_ONLY("LOCAL_ONLY", "仅本地", StatusNeutral),
    PENDING("PENDING", "待同步", StatusWarning),
    SYNCING("SYNCING", "同步中", StatusPrimary),
    SYNCED("SYNCED", "已同步", StatusSuccess),
    FAILED("FAILED", "同步失败", StatusDanger),
    CONFLICT("CONFLICT", "冲突待复核", StatusDanger),
    DELETED("DELETED", "已删除", StatusNeutral),
    IGNORED("IGNORED", "已忽略", StatusNeutral);

    companion object {
        fun fromCode(code: String): SyncStatus =
            entries.find { it.code == code } ?: PENDING

        fun fromCodeOrNull(code: String?): SyncStatus? =
            code?.let { entries.find { e -> e.code == it } }
    }
}
