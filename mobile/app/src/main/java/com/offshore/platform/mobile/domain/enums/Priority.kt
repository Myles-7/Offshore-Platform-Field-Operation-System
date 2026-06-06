package com.offshore.platform.mobile.domain.enums

import androidx.compose.ui.graphics.Color
import com.offshore.platform.mobile.ui.theme.*

/**
 * Work order priority — matches backend and PC frontend priorityStatusOptions.
 *
 * Backend: work_order.priority
 * PC: src/constants/enums.ts priorityStatusOptions
 */
enum class Priority(val code: String, val displayName: String, val color: Color) {
    URGENT("URGENT", "紧急", StatusDanger),
    HIGH("HIGH", "高", StatusWarning),
    NORMAL("NORMAL", "普通", StatusPrimary),
    LOW("LOW", "低", StatusNeutral);

    companion object {
        fun fromCode(code: String): Priority =
            entries.find { it.code == code } ?: NORMAL

        fun fromCodeOrNull(code: String?): Priority? =
            code?.let { entries.find { e -> e.code == it } }
    }
}
