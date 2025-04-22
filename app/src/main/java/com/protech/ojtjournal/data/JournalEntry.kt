package com.protech.ojtjournal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val date: String, // Stored as ISO-8601 string
    val timestamp: Long,
    val tasksDone: String = "", // Comma-separated tasks
    val learnings: String = "",
    val challenges: String = "",
    val nextDayPlans: String = "",
    val location: String = "", // Location name/description
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imagePaths: String = "" // Comma-separated list of image URIs
) {
    companion object {
        fun createNew(
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
        ): JournalEntry {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val nowString = now.toString()
            
            return JournalEntry(
                title = title,
                content = content,
                date = date.toString(),
                timestamp = Clock.System.now().toEpochMilliseconds() / 1000,
                tasksDone = tasksDone,
                learnings = learnings,
                challenges = challenges,
                nextDayPlans = nextDayPlans,
                location = location,
                latitude = latitude,
                longitude = longitude,
                imagePaths = imagePaths
            )
        }
    }

    // Helper method to get list of image paths
    fun getImagePathsList(): List<String> {
        return if (imagePaths.isBlank()) {
            emptyList()
        } else {
            imagePaths.split(",")
        }
    }

    fun getDateFormatted(): String {
        val dateObj = LocalDate.parse(date)
        return "${dateObj.dayOfMonth} ${dateObj.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${dateObj.year}"
    }
} 