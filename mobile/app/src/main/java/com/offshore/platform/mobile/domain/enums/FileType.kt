package com.offshore.platform.mobile.domain.enums

import androidx.compose.ui.graphics.Color
import com.offshore.platform.mobile.ui.theme.*

/**
 * File / attachment type enum — matches backend and PC frontend.
 *
 * Backend: file_storage.file_type
 * PC: src/constants/enums.ts (file type options)
 */
enum class FileType(val code: String, val displayName: String, val color: Color) {
    PHOTO("PHOTO", "施工照片", StatusPrimary),
    VIDEO("VIDEO", "施工视频", Color(0xFF7B1FA2)),
    AUDIO("AUDIO", "语音备注", Color(0xFF5D4037)),
    SIGNATURE("SIGNATURE", "签名图片", Color(0xFFC2185B)),
    PDF("PDF", "PDF验收单", StatusDanger),
    AI_IMAGE("AI_IMAGE", "AI识别结果图", Color(0xFF00BCD4)),
    CERT("CERT", "证书附件", Color(0xFF4CAF50)),
    QRCODE("QRCODE", "二维码图片", StatusNeutral),
    OTHER("OTHER", "其他", StatusNeutral);

    companion object {
        fun fromCode(code: String): FileType =
            entries.find { it.code == code } ?: OTHER

        fun fromCodeOrNull(code: String?): FileType? =
            code?.let { entries.find { e -> e.code == it } }
    }
}
