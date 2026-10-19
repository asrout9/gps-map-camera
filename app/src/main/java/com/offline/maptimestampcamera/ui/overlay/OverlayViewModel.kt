package com.offline.maptimestampcamera.ui.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.offline.maptimestampcamera.data.preferences.AppPreferencesRepository
import com.offline.maptimestampcamera.data.preferences.UserPreferences
import com.offline.maptimestampcamera.data.repository.OverlayPresetRepository
import com.offline.maptimestampcamera.data.repository.SessionRepository
import com.offline.maptimestampcamera.model.DateTimeData
import com.offline.maptimestampcamera.model.LocationData
import com.offline.maptimestampcamera.model.OverlayConfig
import com.offline.maptimestampcamera.model.WidgetConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OverlayViewModel(
    private val sessionRepository: SessionRepository,
    private val presetRepository: OverlayPresetRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val selectedLocation: StateFlow<LocationData> = sessionRepository.selectedLocation
    val selectedDateTime: StateFlow<DateTimeData> = sessionRepository.selectedDateTime
    val mapSnapshotPath: StateFlow<String?> = sessionRepository.mapSnapshotPath

    val userPreferences: StateFlow<UserPreferences> = appPreferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, UserPreferences())

    private val _currentConfig = MutableStateFlow(sessionRepository.activeOverlayConfig.value)
    val currentConfig: StateFlow<OverlayConfig> = _currentConfig.asStateFlow()

    private val _selectedWidgetId = MutableStateFlow<String?>(null)
    val selectedWidgetId: StateFlow<String?> = _selectedWidgetId.asStateFlow()

    val availablePresets = presetRepository.presets
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectWidget(widgetId: String) {
        _selectedWidgetId.value = widgetId
    }

    fun toggleWidgetEnabled(widgetId: String) {
        val updatedWidgets = _currentConfig.value.widgets.map { w ->
            if (w.id == widgetId) w.copy(enabled = !w.enabled) else w
        }
        _currentConfig.value = _currentConfig.value.copy(widgets = updatedWidgets)
    }

    fun updateWidgetConfig(updatedWidget: WidgetConfig) {
        val updatedWidgets = _currentConfig.value.widgets.map { w ->
            if (w.id == updatedWidget.id) updatedWidget else w
        }
        _currentConfig.value = _currentConfig.value.copy(widgets = updatedWidgets)
    }

    fun loadPreset(preset: OverlayConfig) {
        _currentConfig.value = preset
    }

    fun savePreset(name: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            val preset = _currentConfig.value.copy(
                id = "preset_${System.currentTimeMillis()}",
                name = name,
                isDefault = false
            )
            presetRepository.savePreset(preset)
            sessionRepository.updateOverlayConfig(preset)
            onSaved()
        }
    }

    fun applyAndProceed(onComplete: () -> Unit) {
        sessionRepository.updateOverlayConfig(_currentConfig.value)
        onComplete()
    }

    class Factory(
        private val sessionRepository: SessionRepository,
        private val presetRepository: OverlayPresetRepository,
        private val appPreferencesRepository: AppPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OverlayViewModel(sessionRepository, presetRepository, appPreferencesRepository) as T
        }
    }
}
