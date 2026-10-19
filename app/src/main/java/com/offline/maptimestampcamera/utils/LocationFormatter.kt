package com.offline.maptimestampcamera.utils

import com.offline.maptimestampcamera.model.CoordinateFormat
import java.util.Locale
import kotlin.math.abs

object LocationFormatter {

    fun formatCoordinates(lat: Double, lon: Double, format: CoordinateFormat): Pair<String, String> {
        return when (format) {
            CoordinateFormat.DD -> {
                val latStr = String.format(Locale.US, "%.6f°", lat)
                val lonStr = String.format(Locale.US, "%.6f°", lon)
                Pair(latStr, lonStr)
            }
            CoordinateFormat.DMS -> {
                val latDms = toDms(lat, true)
                val lonDms = toDms(lon, false)
                Pair(latDms, lonDms)
            }
            CoordinateFormat.DDM -> {
                val latDdm = toDdm(lat, true)
                val lonDdm = toDdm(lon, false)
                Pair(latDdm, lonDdm)
            }
        }
    }

    private fun toDms(decimal: Double, isLatitude: Boolean): String {
        val dir = if (isLatitude) {
            if (decimal >= 0) "N" else "S"
        } else {
            if (decimal >= 0) "E" else "W"
        }
        val absVal = abs(decimal)
        val degrees = absVal.toInt()
        val minutesDouble = (absVal - degrees) * 60
        val minutes = minutesDouble.toInt()
        val seconds = (minutesDouble - minutes) * 60

        return String.format(Locale.US, "%d°%02d'%04.1f\"%s", degrees, minutes, seconds, dir)
    }

    private fun toDdm(decimal: Double, isLatitude: Boolean): String {
        val dir = if (isLatitude) {
            if (decimal >= 0) "N" else "S"
        } else {
            if (decimal >= 0) "E" else "W"
        }
        val absVal = abs(decimal)
        val degrees = absVal.toInt()
        val minutes = (absVal - degrees) * 60

        return String.format(Locale.US, "%d°%.4f'%s", degrees, minutes, dir)
    }
}
