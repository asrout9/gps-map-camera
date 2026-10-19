package com.offline.maptimestampcamera.ui.datetime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.offline.maptimestampcamera.data.repository.SessionRepository
import com.offline.maptimestampcamera.model.DateTimeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.TimeZone

class DateTimeViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _dateTimeData = MutableStateFlow(sessionRepository.selectedDateTime.value)
    val dateTimeData: StateFlow<DateTimeData> = _dateTimeData.asStateFlow()

    val availableTimezones: List<String> = TimeZone.getAvailableIDs().sorted()

    fun setUseCurrentDateTime(useCurrent: Boolean) {
        if (useCurrent) {
            _dateTimeData.value = _dateTimeData.value.copy(
                timestampMs = System.currentTimeMillis(),
                isCustom = false
            )
        } else {
            _dateTimeData.value = _dateTimeData.value.copy(isCustom = true)
        }
    }

    fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(_dateTimeData.value.timezoneId)).apply {
            timeInMillis = _dateTimeData.value.timestampMs
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        _dateTimeData.value = _dateTimeData.value.copy(
            timestampMs = calendar.timeInMillis,
            isCustom = true
        )
    }

    fun updateTime(hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(_dateTimeData.value.timezoneId)).apply {
            timeInMillis = _dateTimeData.value.timestampMs
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        _dateTimeData.value = _dateTimeData.value.copy(
            timestampMs = calendar.timeInMillis,
            isCustom = true
        )
    }

    fun setTimezone(tzId: String) {
        _dateTimeData.value = _dateTimeData.value.copy(timezoneId = tzId)
    }

    fun set24HourFormat(is24h: Boolean) {
        _dateTimeData.value = _dateTimeData.value.copy(is24HourFormat = is24h)
    }

    fun saveAndProceed(onComplete: () -> Unit) {
        sessionRepository.updateDateTime(_dateTimeData.value)
        onComplete()
    }

    class Factory(
        private val sessionRepository: SessionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DateTimeViewModel(sessionRepository) as T
        }
    }
}
