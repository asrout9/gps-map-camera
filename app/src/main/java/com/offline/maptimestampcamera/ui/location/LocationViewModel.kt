package com.offline.maptimestampcamera.ui.location

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.offline.maptimestampcamera.data.preferences.AppPreferencesRepository
import com.offline.maptimestampcamera.data.preferences.UserPreferences
import com.offline.maptimestampcamera.data.repository.LocationRepository
import com.offline.maptimestampcamera.data.repository.SearchResultItem
import com.offline.maptimestampcamera.data.repository.SessionRepository
import com.offline.maptimestampcamera.model.CoordinateFormat
import com.offline.maptimestampcamera.model.LocationData
import com.offline.maptimestampcamera.utils.BitmapUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class LocationViewModel(
    private val context: Context,
    private val locationRepository: LocationRepository,
    private val sessionRepository: SessionRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = appPreferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, UserPreferences())

    private val _currentLocationData = MutableStateFlow(LocationData(latitude = 28.6139, longitude = 77.2090))
    val currentLocationData: StateFlow<LocationData> = _currentLocationData.asStateFlow()

    private val _isGeocoding = MutableStateFlow(false)
    val isGeocoding: StateFlow<Boolean> = _isGeocoding.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val recentSearches = locationRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var geocodeJob: Job? = null
    private var searchDebounceJob: Job? = null

    fun onMapCenterChanged(lat: Double, lon: Double) {
        geocodeJob?.cancel()
        geocodeJob = viewModelScope.launch {
            delay(300)
            _isGeocoding.value = true
            val data = locationRepository.reverseGeocode(lat, lon)
            _currentLocationData.value = data
            _isGeocoding.value = false
        }
    }

    fun useCurrentDeviceLocation(onLocationFound: (Double, Double) -> Unit) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return

            @Suppress("MissingPermission")
            var lastKnown: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnown == null) {
                @Suppress("MissingPermission")
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (lastKnown == null) {
                @Suppress("MissingPermission")
                lastKnown = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }

            if (lastKnown != null) {
                val lat = lastKnown.latitude
                val lon = lastKnown.longitude
                onLocationFound(lat, lon)
                onMapCenterChanged(lat, lon)
            } else {
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        onLocationFound(location.latitude, location.longitude)
                        onMapCenterChanged(location.latitude, location.longitude)
                        locationManager.removeUpdates(this)
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }
                @Suppress("MissingPermission")
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null)
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchDebounceJob?.cancel()
        if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchDebounceJob = viewModelScope.launch {
            delay(350)
            _isSearching.value = true
            val results = locationRepository.searchLocations(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun selectSearchResult(item: SearchResultItem, onMapMove: (Double, Double) -> Unit) {
        onMapMove(item.latitude, item.longitude)
        onMapCenterChanged(item.latitude, item.longitude)
        viewModelScope.launch {
            locationRepository.saveRecentSearch(item.displayName, item.latitude, item.longitude, item.displayName)
        }
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun setCoordinateFormat(format: CoordinateFormat) {
        viewModelScope.launch {
            appPreferencesRepository.setCoordinateFormat(format)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            locationRepository.clearSearchHistory()
        }
    }

    fun finalizeLocationAndCaptureMap(mapBitmap: Bitmap, onComplete: () -> Unit) {
        viewModelScope.launch {
            val croppedMap = BitmapUtils.cropCenterSquare(mapBitmap, scaleFactor = 0.45f)
            val fileName = "map_snapshot_${System.currentTimeMillis()}.png"
            val mapFile = File(context.cacheDir, fileName)
            BitmapUtils.saveBitmapToFile(croppedMap, mapFile, Bitmap.CompressFormat.PNG, 95)
            sessionRepository.updateMapSnapshotPath(mapFile.absolutePath)
            sessionRepository.updateLocation(_currentLocationData.value)
            onComplete()
        }
    }

    class Factory(
        private val context: Context,
        private val locationRepository: LocationRepository,
        private val sessionRepository: SessionRepository,
        private val appPreferencesRepository: AppPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocationViewModel(context, locationRepository, sessionRepository, appPreferencesRepository) as T
        }
    }
}
