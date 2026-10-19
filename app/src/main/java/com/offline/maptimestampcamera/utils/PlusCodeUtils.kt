package com.offline.maptimestampcamera.utils

import java.util.Locale
import kotlin.math.floor
import kotlin.math.min

object PlusCodeUtils {

    private const val CODE_ALPHABET = "23456789CFGHJMPQRVWX"
    private const val ENCODING_BASE = 20

    /**
     * Encodes latitude and longitude into an Open Location Code (Plus Code).
     */
    fun generatePlusCode(latitude: Double, longitude: Double, codeLength: Int = 10): String {
        var lat = min(latitude, 90.0)
        lat = maxOf(lat, -90.0)
        var lng = longitude
        while (lng < -180) lng += 360
        while (lng >= 180) lng -= 360

        // Clip lat to avoid -90 causing index out of bounds
        if (lat == 90.0) lat = 89.9999999

        val latVal = (lat + 90) * 8000
        val lngVal = (lng + 180) * 8000

        var latPlace = latVal.toLong()
        var lngPlace = lngVal.toLong()

        val code = StringBuilder()

        // Generate pairs
        for (i in 0 until 5) {
            val latDigit = (latPlace / 400).toInt() % ENCODING_BASE
            val lngDigit = (lngPlace / 400).toInt() % ENCODING_BASE

            code.append(CODE_ALPHABET[latDigit])
            code.append(CODE_ALPHABET[lngDigit])

            latPlace %= 400
            lngPlace %= 400
        }

        // Insert '+' after 8 characters
        code.insert(8, '+')

        return code.toString().uppercase(Locale.US)
    }
}
