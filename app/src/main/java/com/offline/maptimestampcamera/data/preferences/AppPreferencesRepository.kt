package com.offline.maptimestampcamera.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.offline.maptimestampcamera.model.CoordinateFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

enum class AppTheme {
    SYSTEM, DARK, LIGHT
}

enum class UnitSystem {
    METRIC, IMPERIAL
}

data class UserPreferences(
    val coordinateFormat: CoordinateFormat = CoordinateFormat.DD,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val themeMode: AppTheme = AppTheme.SYSTEM,
    val jpegQuality: Int = 90,
    val showWatermark: Boolean = true,
    val watermarkText: String = "GPS Map Camera"
)

class AppPreferencesRepository(
    private val context: Context
) {
    private val datastore = context.dataStore

    private object Keys {
        val COORD_FORMAT = stringPreferencesKey("coord_format")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val JPEG_QUALITY = intPreferencesKey("jpeg_quality")
        val SHOW_WATERMARK = booleanPreferencesKey("show_watermark")
        val WATERMARK_TEXT = stringPreferencesKey("watermark_text")
    }

    val preferencesFlow: Flow<UserPreferences> = datastore.data.map { prefs ->
        UserPreferences(
            coordinateFormat = try {
                CoordinateFormat.valueOf(prefs[Keys.COORD_FORMAT] ?: CoordinateFormat.DD.name)
            } catch (e: Exception) {
                CoordinateFormat.DD
            },
            unitSystem = try {
                UnitSystem.valueOf(prefs[Keys.UNIT_SYSTEM] ?: UnitSystem.METRIC.name)
            } catch (e: Exception) {
                UnitSystem.METRIC
            },
            themeMode = try {
                AppTheme.valueOf(prefs[Keys.THEME_MODE] ?: AppTheme.SYSTEM.name)
            } catch (e: Exception) {
                AppTheme.SYSTEM
            },
            jpegQuality = prefs[Keys.JPEG_QUALITY] ?: 90,
            showWatermark = prefs[Keys.SHOW_WATERMARK] ?: true,
            watermarkText = prefs[Keys.WATERMARK_TEXT] ?: "GPS Map Camera"
        )
    }

    suspend fun setCoordinateFormat(format: CoordinateFormat) {
        datastore.edit { it[Keys.COORD_FORMAT] = format.name }
    }

    suspend fun setUnitSystem(unit: UnitSystem) {
        datastore.edit { it[Keys.UNIT_SYSTEM] = unit.name }
    }

    suspend fun setThemeMode(theme: AppTheme) {
        datastore.edit { it[Keys.THEME_MODE] = theme.name }
    }

    suspend fun setJpegQuality(quality: Int) {
        datastore.edit { it[Keys.JPEG_QUALITY] = quality.coerceIn(50, 100) }
    }

    suspend fun setShowWatermark(show: Boolean) {
        datastore.edit { it[Keys.SHOW_WATERMARK] = show }
    }

    suspend fun setWatermarkText(text: String) {
        datastore.edit { it[Keys.WATERMARK_TEXT] = text }
    }
}
