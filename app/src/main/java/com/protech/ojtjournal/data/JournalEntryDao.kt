package com.protech.ojtjournal.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntry?
    
    @Query("SELECT * FROM entries WHERE date = :date")
    suspend fun getEntryByDate(date: String): JournalEntry?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long
    
    @Update
    suspend fun updateEntry(entry: JournalEntry)
    
    @Delete
    suspend fun deleteEntry(entry: JournalEntry)
    
    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
    
    @Query("SELECT COUNT(*) FROM entries")
    fun getEntryCount(): Flow<Int>
} 