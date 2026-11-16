package com.jagamata.app.data

import androidx.room.TypeConverter
import com.jagamata.app.detection.ViolationType

class Converters {
    @TypeConverter
    fun violationsToString(values: List<ViolationType>): String =
        values.joinToString("|") { it.name }

    @TypeConverter
    fun stringToViolations(value: String): List<ViolationType> =
        if (value.isBlank()) emptyList()
        else value.split("|").mapNotNull { runCatching { ViolationType.valueOf(it) }.getOrNull() }
}
