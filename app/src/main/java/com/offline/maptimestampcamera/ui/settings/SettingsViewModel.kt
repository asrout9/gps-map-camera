package com.offline.maptimestampcamera.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.offline.maptimestampcamera.data.preferences.AppPreferencesRepository
import com.offline.maptimestampcamera.data.preferences.AppTheme
import com.offline.maptimestampcamera.data.preferences.UnitSystem
import com.offline.maptimestampcamera.data.preferences.UserPreferences
import com.offline.maptimestampcamera.model.CoordinateFormat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, UserPreferences())

    fun setCoordinateFormat(format: CoordinateFormat) {
        viewModelScope.launch { preferencesRepository.setCoordinateFormat(format) }
    }

    fun setUnitSystem(unit: UnitSystem) {
        viewModelScope.launch { preferencesRepository.setUnitSystem(unit) }
    }

    fun setThemeMode(theme: AppTheme) {
        viewModelScope.launch { preferencesRepository.setThemeMode(theme) }
    }

    fun setJpegQuality(quality: Int) {
        viewModelScope.launch { preferencesRepository.setJpegQuality(quality) }
    }

    fun setShowWatermark(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowWatermark(show) }
    }

    fun setWatermarkText(text: String) {
        viewModelScope.launch { preferencesRepository.setWatermarkText(text) }
    }

    class Factory(
        private val preferencesRepository: AppPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesRepository) as T
        }
    }
}
