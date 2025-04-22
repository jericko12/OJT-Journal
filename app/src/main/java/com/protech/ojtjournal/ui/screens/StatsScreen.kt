package com.protech.ojtjournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.protech.ojtjournal.OJTJournalApplication
import com.protech.ojtjournal.ui.components.GradientBackground
import com.protech.ojtjournal.ui.components.StatsBarChart
import com.protech.ojtjournal.ui.components.StatsLineChart
import com.protech.ojtjournal.ui.theme.accentGreen
import com.protech.ojtjournal.ui.theme.accentOrange
import com.protech.ojtjournal.ui.theme.accentPink
import com.protech.ojtjournal.ui.theme.primaryLight
import com.protech.ojtjournal.ui.viewmodel.JournalViewModel
import com.protech.ojtjournal.ui.viewmodel.JournalViewModelFactory
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as OJTJournalApplication
    val viewModel: JournalViewModel = viewModel(
        factory = JournalViewModelFactory(application.repository)
    )
    
    val allEntries by viewModel.allJournalEntries.collectAsState(initial = emptyList())
    val entryCount by viewModel.entryCount.collectAsState(initial = 0)
    
    // Calculate stats from entries
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    // Sort entries by date for streak calculation and graphs
    val sortedEntries = remember(allEntries) { 
        allEntries.sortedByDescending { LocalDate.parse(it.date) }
    }
    
    // Calculate day streak
    val streak = remember(sortedEntries, currentDate) {
        var currentStreak = 0
        var checkDate = currentDate
        
        sortedEntries.groupBy { LocalDate.parse(it.date) }.forEach { (date, _) ->
            // If this date is the one we're looking for in the streak
            if (date == checkDate) {
                currentStreak++
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            } else if (date < checkDate) {
                // We missed some days, but can continue counting older entries
                checkDate = date.minus(1, DateTimeUnit.DAY)
                currentStreak++
            } else {
                // Date is in the future or we already counted it, skip
                return@forEach
            }
        }
        
        currentStreak
    }
    
    // Calculate this week's entries
    val oneWeekAgo = currentDate.minus(7, DateTimeUnit.DAY)
    val thisWeekEntries = remember(sortedEntries, currentDate) {
        sortedEntries.count { 
            val entryDate = LocalDate.parse(it.date)
            entryDate >= oneWeekAgo && entryDate <= currentDate
        }
    }
    
    // Calculate total word count across all entries
    val totalWords = remember(allEntries) {
        allEntries.sumOf { entry -> 
            // Only count words from the main journal content
            entry.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        }
    }
    
    // Prepare data for weekly chart (entries per day for last 7 days)
    val weeklyData = remember(sortedEntries, currentDate) {
        val data = LinkedHashMap<LocalDate, Int>()
        
        // Initialize all 7 days with 0 entries - starting from oldest to newest
        for (i in 6 downTo 0) {
            val date = currentDate.minus(i, DateTimeUnit.DAY)
            data[date] = 0
        }
        
        // Count entries for each day
        sortedEntries.forEach { entry ->
            val entryDate = LocalDate.parse(entry.date)
            if (entryDate >= oneWeekAgo && entryDate <= currentDate) {
                data[entryDate] = (data[entryDate] ?: 0) + 1
            }
        }
        
        // Convert to list of pairs for the chart, oldest date first (left to right)
        data.entries
            .sortedBy { it.key } // Sort by date, oldest first
            .map { 
                val dayName = it.key.dayOfWeek.name.take(3).lowercase().replaceFirstChar { char -> char.uppercase() }
                Pair(dayName, it.value.toFloat())
            }
    }
    
    // Prepare monthly data (entries per month)
    val monthlyData = remember(sortedEntries) {
        val data = sortedEntries
            .groupBy { LocalDate.parse(it.date).month }
            .mapValues { it.value.size.toFloat() }
            .toList()
            .sortedBy { it.first.ordinal } // Sort by month number, January first
            .map { Pair(it.first.name.take(3), it.second) }
            
        if (data.isEmpty()) {
            listOf(Pair("No Data", 0f))
        } else {
            data
        }
    }
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Your Stats",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Current Streak
                    StatCard(
                        icon = Icons.Default.Schedule,
                        iconTint = primaryLight,
                        title = "Streak",
                        value = streak.toString(),
                        subtitle = "days",
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Total Entries
                    StatCard(
                        icon = Icons.Outlined.FormatListNumbered,
                        iconTint = accentOrange,
                        title = "Total",
                        value = entryCount.toString(),
                        subtitle = "entries",
                        modifier = Modifier.weight(1f)
                    )
                    
                    // This Week
                    StatCard(
                        icon = Icons.Default.CalendarMonth,
                        iconTint = accentPink,
                        title = "This Week",
                        value = thisWeekEntries.toString(),
                        subtitle = "entries",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Word Count Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(accentGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TextFields,
                                contentDescription = null,
                                tint = accentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = "Total Words",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = totalWords.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentGreen
                            )
                            
                            Text(
                                text = "written across all entries",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Weekly Activity Graph
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Insights,
                                contentDescription = null,
                                tint = primaryLight
                            )
                            
                            Spacer(modifier = Modifier.size(8.dp))
                            
                            Text(
                                text = "This Week's Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (weeklyData.all { it.second == 0f }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No entries this week",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            StatsLineChart(
                                data = weeklyData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                lineColor = primaryLight
                            )
                        }
                    }
                }
                
                // Monthly Activity Graph
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = accentOrange
                            )
                            
                            Spacer(modifier = Modifier.size(8.dp))
                            
                            Text(
                                text = "Monthly Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (monthlyData.all { it.first == "No Data" }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No monthly data available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            StatsLineChart(
                                data = monthlyData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                lineColor = accentOrange
                            )
                        }
                    }
                }
                
                // Summary Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Your Journal Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val averageWordsPerEntry = if (entryCount > 0) totalWords / entryCount else 0
                        
                        Text(
                            text = "You've written an average of $averageWordsPerEntry words per entry.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        if (streak > 0) {
                            Text(
                                text = "Keep up your $streak-day streak! 🔥",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Write an entry today to start a streak! ✍️",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 