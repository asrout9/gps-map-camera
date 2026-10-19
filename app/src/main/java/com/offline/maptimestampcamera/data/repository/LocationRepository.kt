package com.offline.maptimestampcamera.data.repository

import android.content.Context
import android.location.Geocoder
import com.offline.maptimestampcamera.data.local.dao.SearchHistoryDao
import com.offline.maptimestampcamera.data.local.entity.SearchHistoryEntity
import com.offline.maptimestampcamera.model.LocationData
import com.offline.maptimestampcamera.utils.FlagUtils
import com.offline.maptimestampcamera.utils.PlusCodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class SearchResultItem(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val category: String = ""
)

class LocationRepository(
    private val context: Context,
    private val searchHistoryDao: SearchHistoryDao
) {
    val recentSearches: Flow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentSearches()

    private val userAgentString = "OfflineMapTimestampCamera/1.0 (${context.packageName})"

    suspend fun reverseGeocode(lat: Double, lon: Double): LocationData = withContext(Dispatchers.IO) {
        val plusCode = PlusCodeUtils.generatePlusCode(lat, lon)

        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val fullAddress = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }.joinToString(", ")
                    val countryCode = addr.countryCode ?: ""
                    val flagEmoji = FlagUtils.countryCodeToEmojiFlag(countryCode)

                    return@withContext LocationData(
                        latitude = lat,
                        longitude = lon,
                        address = fullAddress.ifEmpty { "Lat: $lat, Lon: $lon" },
                        locality = addr.locality ?: addr.subAdminArea ?: "",
                        village = addr.subLocality ?: "",
                        subdistrict = addr.subAdminArea ?: "",
                        district = addr.adminArea ?: "",
                        state = addr.adminArea ?: "",
                        country = addr.countryName ?: "",
                        countryCode = countryCode,
                        postalCode = addr.postalCode ?: "",
                        plusCode = plusCode,
                        flagEmoji = flagEmoji
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=18&addressdetails=1"
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", userAgentString)
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObj = JSONObject(jsonStr)
                val displayName = jsonObj.optString("display_name", "")
                val addressObj = jsonObj.optJSONObject("address")

                var country = ""
                var countryCode = ""
                var state = ""
                var district = ""
                var locality = ""
                var village = ""
                var postcode = ""

                if (addressObj != null) {
                    country = addressObj.optString("country", "")
                    countryCode = addressObj.optString("country_code", "")
                    state = addressObj.optString("state", "")
                    district = addressObj.optString("state_district", addressObj.optString("county", ""))
                    locality = addressObj.optString("city", addressObj.optString("town", addressObj.optString("suburb", "")))
                    village = addressObj.optString("village", addressObj.optString("neighbourhood", ""))
                    postcode = addressObj.optString("postcode", "")
                }

                val flag = FlagUtils.countryCodeToEmojiFlag(countryCode)

                return@withContext LocationData(
                    latitude = lat,
                    longitude = lon,
                    address = displayName.ifEmpty { "Lat: %.5f, Lon: %.5f".format(lat, lon) },
                    locality = locality,
                    village = village,
                    subdistrict = district,
                    district = district,
                    state = state,
                    country = country,
                    countryCode = countryCode,
                    postalCode = postcode,
                    plusCode = plusCode,
                    flagEmoji = flag
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        LocationData(
            latitude = lat,
            longitude = lon,
            address = "Coordinates: %.6f, %.6f".format(lat, lon),
            plusCode = plusCode,
            flagEmoji = "🌐"
        )
    }

    suspend fun searchLocations(query: String): List<SearchResultItem> = withContext(Dispatchers.IO) {
        if (query.trim().isEmpty()) return@withContext emptyList()

        val coordMatch = parseCoordinates(query)
        if (coordMatch != null) {
            return@withContext listOf(
                SearchResultItem(
                    displayName = "Coordinates: ${coordMatch.first}, ${coordMatch.second}",
                    latitude = coordMatch.first,
                    longitude = coordMatch.second,
                    category = "Coordinates"
                )
            )
        }

        val results = mutableListOf<SearchResultItem>()
        val seenCoordinates = mutableSetOf<Pair<Double, Double>>()

        fun addResult(item: SearchResultItem) {
            val roundedKey = Pair(
                (item.latitude * 10000).toInt() / 10000.0,
                (item.longitude * 10000).toInt() / 10000.0
            )
            if (seenCoordinates.add(roundedKey)) {
                results.add(item)
            }
        }

        // 1. Android Native Geocoder (Google Play Services / Device System Geocoder)
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 10)
                addresses?.forEach { addr ->
                    val fullAddress = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }.joinToString(", ")
                    val name = fullAddress.ifEmpty {
                        listOfNotNull(addr.featureName, addr.subLocality, addr.locality, addr.adminArea, addr.countryName)
                            .distinct()
                            .joinToString(", ")
                    }
                    if (name.isNotEmpty()) {
                        addResult(
                            SearchResultItem(
                                displayName = name,
                                latitude = addr.latitude,
                                longitude = addr.longitude,
                                category = addr.featureName ?: "Place"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Photon API (komoot geocoding for fast village/hamlet/place search)
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://photon.komoot.io/api/?q=$encodedQuery&limit=10"
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", userAgentString)
                connectTimeout = 4000
                readTimeout = 4000
            }

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObj = JSONObject(jsonStr)
                val features = jsonObj.optJSONArray("features") ?: JSONArray()
                for (i in 0 until features.length()) {
                    val feat = features.getJSONObject(i)
                    val geometry = feat.optJSONObject("geometry")
                    val coords = geometry?.optJSONArray("coordinates")
                    val props = feat.optJSONObject("properties")

                    if (coords != null && coords.length() >= 2 && props != null) {
                        val lon = coords.getDouble(0)
                        val lat = coords.getDouble(1)

                        val nameParts = listOfNotNull(
                            props.optString("name", "").takeIf { it.isNotEmpty() },
                            props.optString("city", "").takeIf { it.isNotEmpty() }
                                ?: props.optString("village", "").takeIf { it.isNotEmpty() }
                                ?: props.optString("town", "").takeIf { it.isNotEmpty() },
                            props.optString("state", "").takeIf { it.isNotEmpty() },
                            props.optString("country", "").takeIf { it.isNotEmpty() }
                        ).distinct()

                        val name = nameParts.joinToString(", ")
                        if (name.isNotEmpty()) {
                            addResult(
                                SearchResultItem(
                                    displayName = name,
                                    latitude = lat,
                                    longitude = lon,
                                    category = props.optString("osm_value", "Location")
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Fallback to OpenStreetMap Nominatim
        if (results.size < 5) {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val urlString = "https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&limit=10&addressdetails=1&dedupe=1"
                val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", userAgentString)
                    connectTimeout = 4000
                    readTimeout = 4000
                }

                if (connection.responseCode == 200) {
                    val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonStr)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val lat = item.optString("lat").toDoubleOrNull() ?: continue
                        val lon = item.optString("lon").toDoubleOrNull() ?: continue
                        addResult(
                            SearchResultItem(
                                displayName = item.optString("display_name", ""),
                                latitude = lat,
                                longitude = lon,
                                category = item.optString("type", "Location")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        results
    }

    suspend fun saveRecentSearch(query: String, lat: Double, lon: Double, address: String) {
        searchHistoryDao.insertSearch(
            SearchHistoryEntity(
                query = query,
                latitude = lat,
                longitude = lon,
                address = address
            )
        )
    }

    suspend fun clearSearchHistory() {
        searchHistoryDao.clearHistory()
    }

    private fun parseCoordinates(query: String): Pair<Double, Double>? {
        val parts = query.split(",", " ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (parts.size == 2) {
            val lat = parts[0].toDoubleOrNull()
            val lon = parts[1].toDoubleOrNull()
            if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                return Pair(lat, lon)
            }
        }
        return null
    }
}
