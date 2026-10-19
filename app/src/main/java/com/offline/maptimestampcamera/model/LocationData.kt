package com.offline.maptimestampcamera.model

import kotlinx.serialization.Serializable

enum class CoordinateFormat(val displayName: String) {
    DD("Decimal Degrees (DD)"),
    DMS("Degrees Minutes Seconds (DMS)"),
    DDM("Degrees Decimal Minutes (DDM)")
}

@Serializable
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val locality: String = "",
    val village: String = "",
    val subdistrict: String = "",
    val district: String = "",
    val state: String = "",
    val country: String = "",
    val countryCode: String = "",
    val postalCode: String = "",
    val plusCode: String = "",
    val flagEmoji: String = "🌐",
    val altitude: Double = 0.0
)
