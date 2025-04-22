package com.protech.ojtjournal.data

import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalEntryDao: JournalEntryDao) {
    
    val allJournalEntries: Flow<List<JournalEntry>> = journalEntryDao.getAllEntries()
    val entryCount: Flow<Int> = journalEntryDao.getEntryCount()
    
    suspend fun getEntryById(id: Long): JournalEntry? {
        return journalEntryDao.getEntryById(id)
    }
    
    suspend fun getEntryByDate(date: String): JournalEntry? {
        return journalEntryDao.getEntryByDate(date)
    }
    
    suspend fun insertEntry(entry: JournalEntry): Long {
        return journalEntryDao.insertEntry(entry)
    }
    
    suspend fun updateEntry(entry: JournalEntry) {
        journalEntryDao.updateEntry(entry)
    }
    
    suspend fun deleteEntry(entry: JournalEntry) {
        journalEntryDao.deleteEntry(entry)
    }
    
    suspend fun deleteEntryById(id: Long) {
        journalEntryDao.deleteEntryById(id)
    }
} 