package com.protech.ojtjournal

import android.app.Application
import com.protech.ojtjournal.data.AppDatabase
import com.protech.ojtjournal.data.JournalRepository

class OJTJournalApplication : Application() {
    
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { JournalRepository(database.journalEntryDao()) }
    
    override fun onCreate() {
        super.onCreate()
    }
} 