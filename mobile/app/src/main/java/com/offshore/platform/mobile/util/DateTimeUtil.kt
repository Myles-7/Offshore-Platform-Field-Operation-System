package com.offshore.platform.mobile.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Unified date/time formatting.
 * All dates use the backend format: yyyy-MM-dd HH:mm:ss.
 */
object DateTimeUtil {

    private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    fun format(dateTime: LocalDateTime): String = dateTime.format(DISPLAY_FORMATTER)

    fun formatOrNull(dateTime: LocalDateTime?): String? = dateTime?.format(DISPLAY_FORMATTER)

    fun parseOrNull(dateString: String?): LocalDateTime? {
        if (dateString.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(dateString, DISPLAY_FORMATTER)
        } catch (e: Exception) {
            null
        }
    }

    fun now(): LocalDateTime = LocalDateTime.now()

    fun nowFormatted(): String = format(now())

    fun fileNameTimestamp(): String = now().format(FILE_NAME_FORMATTER)
}
