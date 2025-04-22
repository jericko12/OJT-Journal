package com.protech.ojtjournal.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.protech.ojtjournal.OJTJournalApplication
import com.protech.ojtjournal.ui.components.GradientBackground
import com.protech.ojtjournal.ui.components.ImagePicker
import com.protech.ojtjournal.ui.theme.accentGreen
import com.protech.ojtjournal.ui.theme.accentOrange
import com.protech.ojtjournal.ui.theme.accentPink
import com.protech.ojtjournal.ui.theme.primaryLight
import com.protech.ojtjournal.ui.viewmodel.JournalViewModel
import com.protech.ojtjournal.ui.viewmodel.JournalViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorScreen(
    entryId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val application = LocalContext.current.applicationContext as OJTJournalApplication
    val viewModel: JournalViewModel = viewModel(
        factory = JournalViewModelFactory(application.repository)
    )
    
    val currentEntry by viewModel.currentEntry.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Form state
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.parse(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString())) }
    var tasksDone by remember { mutableStateOf("") }
    var learnings by remember { mutableStateOf("") }
    var challenges by remember { mutableStateOf("") }
    var nextDayPlans by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    
    // Image state
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image selection launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedImages = selectedImages + uris
            }
        }
    )
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && currentPhotoUri != null) {
                selectedImages = selectedImages + currentPhotoUri!!
            }
        }
    )
    
    // Permission launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                takePhoto(context) { uri ->
                    currentPhotoUri = uri
                    cameraLauncher.launch(uri)
                }
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Camera permission is required to take photos"
                    )
                }
            }
        }
    )
    
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Storage permission is required to select images"
                    )
                }
            }
        }
    )
    
    // Add a state to track if user attempted to save
    var hasAttemptedSave by remember { mutableStateOf(false) }
    
    // Date picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = java.time.LocalDate.parse(selectedDate.toString()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    // Focus requesters
    val titleFocusRequester = remember { FocusRequester() }
    
    // Load entry data if editing an existing entry
    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.loadEntry(entryId)
        } else {
            viewModel.clearCurrentEntry()
        }
    }
    
    // Update form state with current entry data when available
    LaunchedEffect(currentEntry) {
        currentEntry?.let { entry ->
            title = entry.title
            content = entry.content
            selectedDate = LocalDate.parse(entry.date)
            tasksDone = entry.tasksDone
            learnings = entry.learnings
            challenges = entry.challenges
            nextDayPlans = entry.nextDayPlans
            location = entry.location
            latitude = entry.latitude
            longitude = entry.longitude
            
            // Load images if any
            if (entry.imagePaths.isNotBlank()) {
                selectedImages = entry.getImagePathsList().mapNotNull { path ->
                    try {
                        Uri.parse(path)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
    
    // Request focus on title when screen appears
    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            selectedDate = date
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = if (entryId == null) "New Entry" else "Edit Entry",
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
            },
            floatingActionButton = {
                // Check if required fields are filled
                val isEnabled = title.isNotBlank() && content.isNotBlank()
                
                FloatingActionButton(
                    onClick = {
                        // Set attempted save flag to true
                        hasAttemptedSave = true
                        
                        // Validate required fields
                        if (!isEnabled) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Title and content are required")
                            }
                            return@FloatingActionButton
                        }
                        
                        coroutineScope.launch {
                            // Convert image URIs to persistent strings
                            val persistentImagePaths = selectedImages.map { uri ->
                                persistImage(context, uri)
                            }.joinToString(",")
                            
                            if (entryId == null) {
                                viewModel.insertEntry(
                                    title = title,
                                    content = content,
                                    date = selectedDate,
                                    tasksDone = tasksDone,
                                    learnings = learnings,
                                    challenges = challenges,
                                    nextDayPlans = nextDayPlans,
                                    location = location,
                                    latitude = latitude,
                                    longitude = longitude,
                                    imagePaths = persistentImagePaths
                                )
                            } else {
                                viewModel.updateEntry(
                                    id = entryId,
                                    title = title,
                                    content = content,
                                    date = selectedDate,
                                    tasksDone = tasksDone,
                                    learnings = learnings,
                                    challenges = challenges,
                                    nextDayPlans = nextDayPlans,
                                    location = location,
                                    latitude = latitude,
                                    longitude = longitude,
                                    imagePaths = persistentImagePaths
                                )
                            }
                            onSaved()
                        }
                    },
                    containerColor = if (isEnabled) primaryLight else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save entry",
                        tint = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Title
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Title") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(titleFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = primaryLight,
                                    cursorColor = primaryLight
                                ),
                                isError = hasAttemptedSave && title.isBlank()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Date
                            Button(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryLight.copy(alpha = 0.1f),
                                    contentColor = primaryLight
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                val formatter = remember {
                                    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
                                }
                                val formattedDate = remember(selectedDate) {
                                    java.time.LocalDate.parse(selectedDate.toString()).format(formatter)
                                }
                                Text(text = formattedDate)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Location
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    // Icon in colored circle
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(primaryLight.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = primaryLight
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.size(8.dp))
                                    
                                    Text(
                                        text = "Location",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Enter location...") },
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = primaryLight,
                                        cursorColor = primaryLight
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Image picker
                    ImagePicker(
                        images = selectedImages,
                        onAddImageClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                galleryLauncher.launch("image/*")
                            } else {
                                // For older Android versions, we need READ_EXTERNAL_STORAGE permission
                                galleryPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        },
                        onTakePhotoClick = {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        onRemoveImage = { uri ->
                            selectedImages = selectedImages.filterNot { it == uri }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Journal Content
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Journal Content",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = content,
                                onValueChange = { content = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                placeholder = { Text("Write your journal entry here...") },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = primaryLight,
                                    cursorColor = primaryLight
                                ),
                                isError = hasAttemptedSave && content.isBlank()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Additional Fields
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Additional Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Tasks
                            SectionTextField(
                                value = tasksDone,
                                onValueChange = { tasksDone = it },
                                label = "Tasks Completed",
                                icon = Icons.Outlined.Task,
                                iconTint = accentGreen,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Learnings
                            SectionTextField(
                                value = learnings,
                                onValueChange = { learnings = it },
                                label = "Learnings",
                                icon = Icons.Outlined.Lightbulb,
                                iconTint = accentOrange,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Challenges
                            SectionTextField(
                                value = challenges,
                                onValueChange = { challenges = it },
                                label = "Challenges",
                                icon = Icons.Outlined.Psychology,
                                iconTint = accentPink,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Next Day Plans
                            SectionTextField(
                                value = nextDayPlans,
                                onValueChange = { nextDayPlans = it },
                                label = "Plans for Next Day",
                                icon = Icons.Outlined.PlaylistAdd,
                                iconTint = primaryLight,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }
                    }
                    
                    // Bottom spacing for FAB
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    iconTint: Color,
    keyboardOptions: KeyboardOptions
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // Icon in colored circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = iconTint
                )
            }
            
            Spacer(modifier = Modifier.size(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter $label...") },
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = iconTint,
                cursorColor = iconTint
            )
        )
    }
}

// Helper function to create a URI for saving camera photos
private fun takePhoto(context: Context, onUriCreated: (Uri) -> Unit) {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 and above
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/OJTJournal")
        }
        
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    } else {
        // For devices running Android < 10
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    uri?.let { onUriCreated(it) }
}

// Helper function to create persistent image URIs by copying images to app's private storage
private suspend fun persistImage(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    try {
        // Create a unique filename
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "IMG_${timestamp}_${uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast('\\') ?: "image"}.jpg"
        
        // Get the app's private pictures directory
        val picturesDir = File(context.filesDir, "Pictures").apply {
            if (!exists()) mkdirs()
        }
        
        // Create the destination file
        val destFile = File(picturesDir, filename)
        
        // Copy the image to private storage
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // Return the URI as a string that can be stored in the database
        "file://${destFile.absolutePath}"
    } catch (e: IOException) {
        // If we can't persist the image, just return the original URI
        uri.toString()
    }
} 