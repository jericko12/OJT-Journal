package com.protech.ojtjournal.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.protech.ojtjournal.OJTJournalApplication
import com.protech.ojtjournal.data.JournalEntry
import com.protech.ojtjournal.ui.components.GradientBackground
import com.protech.ojtjournal.ui.components.JournalCard
import com.protech.ojtjournal.ui.theme.accentGreen
import com.protech.ojtjournal.ui.theme.primaryLight
import com.protech.ojtjournal.ui.viewmodel.JournalViewModel
import com.protech.ojtjournal.ui.viewmodel.JournalViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    onNewEntry: () -> Unit,
    onEntryClick: (Long) -> Unit,
    onStatsClick: () -> Unit
) {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as OJTJournalApplication
    val viewModel: JournalViewModel = viewModel(
        factory = JournalViewModelFactory(application.repository)
    )
    
    val entries by viewModel.allJournalEntries.collectAsState(initial = emptyList())
    val entryCount by viewModel.entryCount.collectAsState(initial = 0)
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    // Search state
    var showSearch by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    
    // Click outside to dismiss search
    val interactionSource = remember { MutableInteractionSource() }
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    TopAppBar(
                        title = { 
                            Text(
                                text = "OJT Journal",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            ) 
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(top = 4.dp),
                        actions = {
                            // Search icon
                            IconButton(
                                onClick = { 
                                    showSearch = !showSearch
                                    if (showSearch) {
                                        coroutineScope.launch {
                                            // Small delay to ensure animation completes
                                            kotlinx.coroutines.delay(100)
                                            focusRequester.requestFocus()
                                        }
                                    } else {
                                        viewModel.setSearchQuery("")
                                        keyboardController?.hide()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            
                            // Stats icon
                            IconButton(onClick = onStatsClick) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "View Stats",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    )
                    
                    // Search bar
                    AnimatedVisibility(visible = showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .focusRequester(focusRequester),
                            placeholder = { Text("Search journal entries...") },
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.setSearchQuery("") }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = primaryLight,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { keyboardController?.hide() }
                            ),
                            singleLine = true
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNewEntry,
                    containerColor = primaryLight,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new entry",
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            if (showSearch) {
                                keyboardController?.hide()
                            }
                        }
                    )
            ) {
                if (entries.isEmpty() && searchQuery.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        onCreateNew = onNewEntry
                    )
                } else if (entries.isEmpty() && searchQuery.isNotEmpty()) {
                    // No search results
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No results found for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Try a different search term",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Only show summary card when not searching
                        if (searchQuery.isEmpty()) {
                            SummaryCard(entryCount = entryCount)
                        } else {
                            // Search results header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Found ${entries.size} ${if (entries.size == 1) "result" else "results"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Journal entries
                        JournalEntryList(
                            entries = entries,
                            onEntryClick = onEntryClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(entryCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        primaryLight,
                        accentGreen
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left side: Icon
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(8.dp)
            )
            
            // Right side: Text
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "Total Entries",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Text(
                    text = "$entryCount",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun JournalEntryList(
    entries: List<JournalEntry>,
    onEntryClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries) { entry ->
            JournalCard(
                entry = entry,
                onClick = { onEntryClick(entry.id) }
            )
        }
        
        // Add extra space at bottom
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onCreateNew: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with colorful background
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryLight.copy(alpha = 0.7f),
                            primaryLight.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your Journal Awaits",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start documenting your OJT journey by creating your first entry",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        FloatingActionButton(
            onClick = onCreateNew,
            containerColor = primaryLight,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new entry",
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.size(8.dp))
                
                Text(
                    text = "Create First Entry",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
} 