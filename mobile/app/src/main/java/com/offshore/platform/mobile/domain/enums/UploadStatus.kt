package com.offshore.platform.mobile.domain.enums

import androidx.compose.ui.graphics.Color
import com.offshore.platform.mobile.ui.theme.*

/**
 * Upload status — matches backend and PC frontend.
 *
 * Backend: file_storage.upload_status
 * PC: database-docs/02_Status_Enum_Dictionary.md §7
 */
enum class UploadStatus(val code: String, val displayName: String, val color: Color) {
    PENDING("PENDING", "待上传", StatusWarning),
    UPLOADING("UPLOADING", "上传中", StatusPrimary),
    UPLOADED("UPLOADED", "已上传", StatusSuccess),
    FAILED("FAILED", "上传失败", StatusDanger);

    companion object {
        fun fromCode(code: String): UploadStatus =
            entries.find { it.code == code } ?: PENDING

        fun fromCodeOrNull(code: String?): UploadStatus? =
            code?.let { entries.find { e -> e.code == it } }
    }

    val isTerminal: Boolean get() = this == UPLOADED || this == FAILED
}
