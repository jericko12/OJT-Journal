package com.protech.ojtjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.protech.ojtjournal.ui.navigation.NavGraph
import com.protech.ojtjournal.ui.theme.OJTJournalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make status bar transparent and enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            OJTJournalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Main content
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
} 