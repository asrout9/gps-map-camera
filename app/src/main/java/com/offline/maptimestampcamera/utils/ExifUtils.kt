package com.offline.maptimestampcamera.utils

import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.offline.maptimestampcamera.model.LocationData
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object ExifUtils {

    fun writeExifMetadata(
        file: File,
        locationData: LocationData,
        timestampMs: Long,
        appVersion: String = "1.0"
    ) {
        try {
            val exif = ExifInterface(file.absolutePath)

            // Date & Time
            val exifDateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
            val dateTimeStr = exifDateFormat.format(Date(timestampMs))
            exif.setAttribute(ExifInterface.TAG_DATETIME, dateTimeStr)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, dateTimeStr)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, dateTimeStr)

            // GPS Coordinates
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertDecimalToDmsString(locationData.latitude))
            exif.setAttribute(
                ExifInterface.TAG_GPS_LATITUDE_REF,
                if (locationData.latitude >= 0) "N" else "S"
            )

            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertDecimalToDmsString(locationData.longitude))
            exif.setAttribute(
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                if (locationData.longitude >= 0) "E" else "W"
            )

            // Altitude if present
            if (locationData.altitude != 0.0) {
                exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, "${abs(locationData.altitude)}/1")
                exif.setAttribute(
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    if (locationData.altitude >= 0) "0" else "1"
                )
            }

            // Device Specs
            exif.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER.replaceFirstChar { it.uppercase() })
            exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL)
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, "Offline Map Timestamp Camera v$appVersion")

            // User Comment with full address details
            val comment = "Address: ${locationData.address}; PlusCode: ${locationData.plusCode}"
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, comment)

            exif.saveAttributes()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertDecimalToDmsString(decimal: Double): String {
        val absVal = abs(decimal)
        val degrees = absVal.toInt()
        val minutesDouble = (absVal - degrees) * 60
        val minutes = minutesDouble.toInt()
        val seconds = ((minutesDouble - minutes) * 60 * 1000).toInt()

        return "$degrees/1,$minutes/1,$seconds/1000"
    }
}
