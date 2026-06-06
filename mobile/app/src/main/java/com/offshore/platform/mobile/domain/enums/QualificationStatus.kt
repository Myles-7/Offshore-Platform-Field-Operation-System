package com.offshore.platform.mobile.domain.enums

import androidx.compose.ui.graphics.Color
import com.offshore.platform.mobile.ui.theme.*

/**
 * Qualification / certificate validity status — matches backend and PC frontend.
 *
 * Backend: employee_certificate.valid_status
 * PC: src/constants/enums.ts qualificationStatusOptions
 */
enum class QualificationStatus(val code: String, val displayName: String, val color: Color) {
    VALID("VALID", "有效", StatusSuccess),
    EXPIRING_SOON("EXPIRING_SOON", "即将到期", StatusWarning),
    EXPIRED("EXPIRED", "已过期", StatusDanger),
    REVOKED("REVOKED", "已吊销", StatusDanger),
    MISSING("MISSING", "缺失", StatusNeutral);

    companion object {
        fun fromCode(code: String): QualificationStatus =
            entries.find { it.code == code } ?: MISSING

        fun fromCodeOrNull(code: String?): QualificationStatus? =
            code?.let { entries.find { e -> e.code == it } }
    }
}
