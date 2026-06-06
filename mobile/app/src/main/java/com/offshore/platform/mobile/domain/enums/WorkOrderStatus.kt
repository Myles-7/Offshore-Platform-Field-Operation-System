package com.offshore.platform.mobile.domain.enums

import androidx.compose.ui.graphics.Color
import com.offshore.platform.mobile.ui.theme.*

/**
 * Work order status — must match backend and PC frontend workOrderStatusOptions.
 *
 * Backend: work_order.status
 * PC: src/constants/enums.ts workOrderStatusOptions
 */
enum class WorkOrderStatus(val code: String, val displayName: String, val color: Color) {
    DRAFT("DRAFT", "草稿", Color(0xFF9E9E9E)),
    PENDING("PENDING", "待派工", StatusWarning),
    ASSIGNED("ASSIGNED", "已派工", StatusPrimary),
    ACCEPTED("ACCEPTED", "已接收", StatusPrimary),
    IN_PROGRESS("IN_PROGRESS", "施工中", StatusWarning),
    PENDING_ACCEPTANCE("PENDING_ACCEPTANCE", "待验收", StatusWarning),
    COMPLETED("COMPLETED", "已完成", StatusSuccess),
    REJECTED("REJECTED", "已驳回", StatusDanger),
    CLOSED("CLOSED", "已关闭", StatusNeutral);

    companion object {
        fun fromCode(code: String): WorkOrderStatus =
            entries.find { it.code == code } ?: PENDING

        fun fromCodeOrNull(code: String?): WorkOrderStatus? =
            code?.let { entries.find { e -> e.code == it } }
    }
}
