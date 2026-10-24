package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CollectionRecord
import com.example.data.model.ColonyOverview
import com.example.data.model.DailySummary
import com.example.data.repository.CollectionRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CollectionRepository

    val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val houseTypes = listOf("G", "H", "F", "E", "D", "C", "B")

    // Theme Mode: Default to clean white light theme
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Active bottom navigation destination: 0 = Log Trash, 1 = Today's Report, 2 = History & Search
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Form State
    private val _selectedHouseType = MutableStateFlow("G")
    val selectedHouseType: StateFlow<String> = _selectedHouseType.asStateFlow()

    private val _houseNumber = MutableStateFlow("1")
    val houseNumber: StateFlow<String> = _houseNumber.asStateFlow()

    private val _isSegregated = MutableStateFlow(true)
    val isSegregated: StateFlow<Boolean> = _isSegregated.asStateFlow()

    private val _collectorNotes = MutableStateFlow("")
    val collectorNotes: StateFlow<String> = _collectorNotes.asStateFlow()

    private val _autoIncrement = MutableStateFlow(true)
    val autoIncrement: StateFlow<Boolean> = _autoIncrement.asStateFlow()

    // Feedback message
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    // Report selected date (defaults to today)
    private val _selectedReportDate = MutableStateFlow(todayDateString)
    val selectedReportDate: StateFlow<String> = _selectedReportDate.asStateFlow()

    // House search query in History tab
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).collectionDao()
        repository = CollectionRepository(dao)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(todayDateString)
        }
    }

    // Live list of records collected today
    val todayRecords: StateFlow<List<CollectionRecord>> = repository.getRecordsForDate(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Today's summary
    val todaySummary: StateFlow<DailySummary> = repository.getDailySummaryFlow(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailySummary(todayDateString, 0, 0, 0)
        )

    // Today's detailed colony overview (with type breakdown)
    val todayOverview: StateFlow<ColonyOverview> = repository.getColonyOverviewFlow(todayDateString)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ColonyOverview(todayDateString, 0, 0, 0, emptyList(), emptyList())
        )

    // Overview for the selected date in History
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDateOverview: StateFlow<ColonyOverview> = _selectedReportDate.flatMapLatest { date ->
        repository.getColonyOverviewFlow(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ColonyOverview(todayDateString, 0, 0, 0, emptyList(), emptyList())
    )

    // All distinct recorded dates
    val allRecordedDates: StateFlow<List<String>> = repository.allDates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(todayDateString)
        )

    // Daily summaries for the interactive stacked bar graph
    val allDailySummaries: StateFlow<List<DailySummary>> = repository.getAllDailySummaries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Check if the currently selected house was already recorded today
    val currentHouseExistingRecord: StateFlow<CollectionRecord?> = combine(
        _selectedHouseType,
        _houseNumber,
        todayRecords
    ) { type, number, records ->
        records.firstOrNull {
            it.houseType.equals(type, ignoreCase = true) &&
            it.houseNumber.trim().equals(number.trim(), ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Search results in history tab
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<CollectionRecord>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getRecordsForDate(todayDateString)
        } else {
            repository.searchRecords(query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun selectHouseType(type: String) {
        _selectedHouseType.value = type
    }

    fun setHouseNumber(number: String) {
        _houseNumber.value = number
    }

    fun appendDigitToHouseNumber(digit: String) {
        val current = _houseNumber.value
        if (current == "0" || current.isEmpty()) {
            _houseNumber.value = digit
        } else if (current.length < 5) {
            _houseNumber.value = current + digit
        }
    }

    fun backspaceHouseNumber() {
        val current = _houseNumber.value
        if (current.isNotEmpty()) {
            _houseNumber.value = current.dropLast(1)
        }
    }

    fun incrementHouseNumber() {
        val current = _houseNumber.value.toIntOrNull()
        if (current != null) {
            _houseNumber.value = (current + 1).toString()
        }
    }

    fun decrementHouseNumber() {
        val current = _houseNumber.value.toIntOrNull()
        if (current != null && current > 1) {
            _houseNumber.value = (current - 1).toString()
        }
    }

    fun setSegregated(segregated: Boolean) {
        _isSegregated.value = segregated
        triggerHaptic(50)
    }

    fun toggleSegregated() {
        _isSegregated.value = !_isSegregated.value
        triggerHaptic(50)
    }

    fun setNotes(notes: String) {
        _collectorNotes.value = notes
    }

    fun setAutoIncrement(enabled: Boolean) {
        _autoIncrement.value = enabled
    }

    fun selectReportDate(date: String) {
        _selectedReportDate.value = date
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun submitCollectionRecord() {
        val type = _selectedHouseType.value
        val number = _houseNumber.value.trim()

        if (number.isBlank()) {
            _feedbackMessage.value = "Please enter a house number"
            return
        }

        viewModelScope.launch {
            val record = repository.saveRecord(
                houseType = type,
                houseNumber = number,
                isSegregated = _isSegregated.value,
                dateString = todayDateString,
                notes = _collectorNotes.value
            )

            triggerHaptic(100)
            val statusText = if (record.isSegregated) "Segregated ✅" else "Mixed ❌"
            _feedbackMessage.value = "Logged ${record.fullAddress}: $statusText"

            // If auto-increment is enabled, automatically prepare the next house number
            if (_autoIncrement.value) {
                val numInt = number.toIntOrNull()
                if (numInt != null) {
                    _houseNumber.value = (numInt + 1).toString()
                }
            }

            // Reset notes
            _collectorNotes.value = ""
        }
    }

    fun toggleRecordSegregation(record: CollectionRecord) {
        viewModelScope.launch {
            repository.toggleSegregation(record)
            triggerHaptic(50)
            val newStatus = if (!record.isSegregated) "Segregated" else "Mixed"
            _feedbackMessage.value = "Updated ${record.fullAddress} to $newStatus"
        }
    }

    fun deleteRecord(record: CollectionRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            _feedbackMessage.value = "Removed ${record.fullAddress}"
        }
    }

    private fun triggerHaptic(durationMs: Long) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration is unavailable
        }
    }
}
