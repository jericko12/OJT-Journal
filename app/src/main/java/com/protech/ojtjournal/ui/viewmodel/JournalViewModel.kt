package com.protech.ojtjournal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.protech.ojtjournal.data.JournalEntry
import com.protech.ojtjournal.data.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class JournalViewModel(private val repository: JournalRepository) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _allJournalEntries = repository.allJournalEntries
    val allJournalEntries = combine(_allJournalEntries, _searchQuery) { entries, query ->
        if (query.isBlank()) {
            entries
        } else {
            entries.filter { entry ->
                entry.title.contains(query, ignoreCase = true) ||
                entry.content.contains(query, ignoreCase = true) ||
                entry.tasksDone.contains(query, ignoreCase = true) ||
                entry.learnings.contains(query, ignoreCase = true) ||
                entry.challenges.contains(query, ignoreCase = true) ||
                entry.nextDayPlans.contains(query, ignoreCase = true) ||
                entry.location.contains(query, ignoreCase = true)
            }
        }
    }
    
    val entryCount: Flow<Int> = repository.entryCount
    
    private val _currentEntry = MutableStateFlow<JournalEntry?>(null)
    val currentEntry: StateFlow<JournalEntry?> = _currentEntry.asStateFlow()
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun loadEntry(id: Long) {
        viewModelScope.launch {
            _currentEntry.value = repository.getEntryById(id)
        }
    }
    
    fun insertEntry(
        title: String,
        content: String,
        date: LocalDate,
        tasksDone: String = "",
        learnings: String = "",
        challenges: String = "",
        nextDayPlans: String = "",
        location: String = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        imagePaths: String = ""
    ) {
        val newEntry = JournalEntry.createNew(
            title = title,
            content = content,
            date = date,
            tasksDone = tasksDone,
            learnings = learnings,
            challenges = challenges,
            nextDayPlans = nextDayPlans,
            location = location,
            latitude = latitude,
            longitude = longitude,
            imagePaths = imagePaths
        )
        
        viewModelScope.launch {
            val id = repository.insertEntry(newEntry)
            _currentEntry.value = newEntry.copy(id = id)
        }
    }
    
    fun updateEntry(
        id: Long,
        title: String,
        content: String,
        date: LocalDate,
        tasksDone: String = "",
        learnings: String = "",
        challenges: String = "",
        nextDayPlans: String = "",
        location: String = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        imagePaths: String = ""
    ) {
        val current = _currentEntry.value ?: return
        
        val updatedEntry = current.copy(
            id = id,
            title = title,
            content = content,
            date = date.toString(),
            tasksDone = tasksDone,
            learnings = learnings,
            challenges = challenges,
            nextDayPlans = nextDayPlans,
            location = location,
            latitude = latitude,
            longitude = longitude,
            imagePaths = imagePaths
        )
        
        viewModelScope.launch {
            repository.updateEntry(updatedEntry)
            _currentEntry.value = updatedEntry
        }
    }
    
    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteEntryById(id)
            if (_currentEntry.value?.id == id) {
                _currentEntry.value = null
            }
        }
    }
    
    fun clearCurrentEntry() {
        _currentEntry.value = null
    }
    
    fun getEntryByDate(date: LocalDate) {
        viewModelScope.launch {
            _currentEntry.value = repository.getEntryByDate(date.toString())
        }
    }
}

class JournalViewModelFactory(private val repository: JournalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JournalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 