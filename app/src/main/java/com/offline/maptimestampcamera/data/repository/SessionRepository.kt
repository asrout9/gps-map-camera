package com.offline.maptimestampcamera.data.repository

import com.offline.maptimestampcamera.model.DateTimeData
import com.offline.maptimestampcamera.model.LocationData
import com.offline.maptimestampcamera.model.OverlayConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRepository {

    private val _selectedLocation = MutableStateFlow(LocationData())
    val selectedLocation: StateFlow<LocationData> = _selectedLocation.asStateFlow()

    private val _selectedDateTime = MutableStateFlow(DateTimeData())
    val selectedDateTime: StateFlow<DateTimeData> = _selectedDateTime.asStateFlow()

    private val _mapSnapshotPath = MutableStateFlow<String?>(null)
    val mapSnapshotPath: StateFlow<String?> = _mapSnapshotPath.asStateFlow()

    private val _activeOverlayConfig = MutableStateFlow(OverlayConfig())
    val activeOverlayConfig: StateFlow<OverlayConfig> = _activeOverlayConfig.asStateFlow()

    fun updateLocation(location: LocationData) {
        _selectedLocation.value = location
    }

    fun updateDateTime(dateTime: DateTimeData) {
        _selectedDateTime.value = dateTime
    }

    fun updateMapSnapshotPath(path: String) {
        _mapSnapshotPath.value = path
    }

    fun updateOverlayConfig(config: OverlayConfig) {
        _activeOverlayConfig.value = config
    }
}
