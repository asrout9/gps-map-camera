package com.offline.maptimestampcamera.model

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Serializable
data class DateTimeData(
    val timestampMs: Long = System.currentTimeMillis(),
    val timezoneId: String = TimeZone.getDefault().id,
    val isCustom: Boolean = false,
    val is24HourFormat: Boolean = false
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(timezoneId)
        return sdf.format(Date(timestampMs))
    }

    fun getFormattedTime(): String {
        val pattern = if (is24HourFormat) "HH:mm:ss" else "hh:mm:ss a"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(timezoneId)
        return sdf.format(Date(timestampMs))
    }

    fun getDayOfWeekName(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone(timezoneId)
        return sdf.format(Date(timestampMs))
    }

    fun getFormattedShortDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(timezoneId)
        return sdf.format(Date(timestampMs))
    }

    fun getFormattedTimeShort(): String {
        val pattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
        val sdf = SimpleDateFormat(pattern, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone(timezoneId)
        return sdf.format(Date(timestampMs))
    }

    fun getGmtOffsetString(): String {
        val tz = TimeZone.getTimeZone(timezoneId)
        val offsetMs = tz.getOffset(timestampMs)
        val totalMinutes = offsetMs / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = kotlin.math.abs(totalMinutes % 60)
        val sign = if (hours >= 0) "+" else "-"
        return "GMT %s%02d:%02d".format(sign, kotlin.math.abs(hours), minutes)
    }

    fun getFormattedTimezone(): String {
        val tz = TimeZone.getTimeZone(timezoneId)
        return tz.getDisplayName(tz.inDaylightTime(Date(timestampMs)), TimeZone.SHORT, Locale.getDefault())
    }
}
