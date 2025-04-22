package com.protech.ojtjournal.ui.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.protech.ojtjournal.OJTJournalApplication
import com.protech.ojtjournal.ui.components.GradientBackground
import com.protech.ojtjournal.ui.theme.accentGreen
import com.protech.ojtjournal.ui.theme.accentOrange
import com.protech.ojtjournal.ui.theme.accentPink
import com.protech.ojtjournal.ui.theme.primaryLight
import com.protech.ojtjournal.ui.viewmodel.JournalViewModel
import com.protech.ojtjournal.ui.viewmodel.JournalViewModelFactory
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entryId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as OJTJournalApplication
    val viewModel: JournalViewModel = viewModel(
        factory = JournalViewModelFactory(application.repository)
    )
    
    val currentEntry by viewModel.currentEntry.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this journal entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry(entryId)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(
                        text = "Entry Details", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete entry"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onEdit,
                    containerColor = primaryLight,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit entry",
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                currentEntry?.let { entry ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header card with title and date
                        HeaderCard(
                            title = entry.title,
                            date = LocalDate.parse(entry.date)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Main content
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Journal Notes",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = entry.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Additional sections
                        if (entry.tasksDone.isNotEmpty()) {
                            EntrySectionCard(
                                title = "Tasks Completed",
                                content = entry.tasksDone,
                                icon = Icons.Outlined.Task,
                                iconTint = accentGreen
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        if (entry.learnings.isNotEmpty()) {
                            EntrySectionCard(
                                title = "Learnings",
                                content = entry.learnings,
                                icon = Icons.Outlined.Lightbulb,
                                iconTint = accentOrange
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        if (entry.challenges.isNotEmpty()) {
                            EntrySectionCard(
                                title = "Challenges",
                                content = entry.challenges,
                                icon = Icons.Outlined.Psychology,
                                iconTint = accentPink
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        if (entry.nextDayPlans.isNotEmpty()) {
                            EntrySectionCard(
                                title = "Plans for Next Day",
                                content = entry.nextDayPlans,
                                icon = Icons.Outlined.PlaylistAdd,
                                iconTint = primaryLight
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        if (entry.location.isNotEmpty()) {
                            EntrySectionCard(
                                title = "Location",
                                content = entry.location,
                                icon = Icons.Default.LocationOn,
                                iconTint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Display images if available
                        val context = LocalContext.current
                        val images = entry.getImagePathsList().mapNotNull { path ->
                            parseImageUri(context, path)
                        }
                        
                        if (images.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Circular icon
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(primaryLight.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = primaryLight,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.size(12.dp))
                                        
                                        Text(
                                            text = "Images",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    var previewImage by remember { mutableStateOf<Uri?>(null) }
                                    
                                    // Display image gallery
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        images.forEach { uri ->
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(uri)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Image",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { previewImage = uri }
                                            )
                                        }
                                    }
                                    
                                    // Image preview dialog
                                    if (previewImage != null) {
                                        Dialog(onDismissRequest = { previewImage = null }) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .padding(8.dp)
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(previewImage)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "Full size image",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(24.dp)
                                                )
                                                
                                                IconButton(
                                                    onClick = { previewImage = null },
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Close preview",
                                                        tint = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Bottom spacing for FAB
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Entry not found",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCard(title: String, date: LocalDate) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(primaryLight, accentOrange)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
                
                Text(
                    text = "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EntrySectionCard(
    title: String,
    content: String,
    icon: ImageVector,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                
                Spacer(modifier = Modifier.size(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MetadataItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.size(8.dp))
        
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    try {
        val instant = Instant.fromEpochMilliseconds(timestamp * 1000)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.date} at ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        return "Unknown date"
    }
}

private fun parseImageUri(context: Context, path: String): Uri? {
    return try {
        val uri = Uri.parse(path)
        // If it's a file URI from our app storage, ensure it's accessible
        if (uri.scheme == "file" && uri.path?.startsWith(context.filesDir.absolutePath) == true) {
            // For file URIs, we can use them directly
            uri
        } else {
            // For content URIs, they might be temporary, so try to use as-is
            uri
        }
    } catch (e: Exception) {
        null
    }
} 