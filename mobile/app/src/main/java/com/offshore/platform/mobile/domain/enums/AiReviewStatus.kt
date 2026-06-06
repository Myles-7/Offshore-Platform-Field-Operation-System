package com.offshore.platform.mobile.domain.enums

import androidx.compose.ui.graphics.Color
import com.offshore.platform.mobile.ui.theme.*

/**
 * AI review status — matches backend and PC frontend aiReviewStatusOptions.
 *
 * Backend: ai_result.review_status
 * PC: src/constants/enums.ts aiReviewStatusOptions
 */
enum class AiReviewStatus(val code: String, val displayName: String, val color: Color) {
    PENDING_REVIEW("PENDING_REVIEW", "待复核", StatusWarning),
    CONFIRMED("CONFIRMED", "已确认", StatusSuccess),
    FALSE_POSITIVE("FALSE_POSITIVE", "误报", Color(0xFF9E9E9E)),
    IGNORED("IGNORED", "已忽略", StatusNeutral);

    companion object {
        fun fromCode(code: String): AiReviewStatus =
            entries.find { it.code == code } ?: PENDING_REVIEW

        fun fromCodeOrNull(code: String?): AiReviewStatus? =
            code?.let { entries.find { e -> e.code == it } }
    }
}
