package com.offshore.platform.mobile.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Room type converters for date, enum Strings and primitives.
 */
class RoomTypeConverters {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    // ---- LocalDateTime ----

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.format(formatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it, formatter) }

    // ---- Long / Int nullable (for version / conflictFlag) ----

    @TypeConverter
    fun fromInt(value: Int?): Int = value ?: 0

    @TypeConverter
    fun toInt(value: Int): Int? = value

    @TypeConverter
    fun fromLong(value: Long?): Long = value ?: 0L

    @TypeConverter
    fun toLong(value: Long): Long? = value
}
