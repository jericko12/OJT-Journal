package com.protech.ojtjournal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.protech.ojtjournal.ui.screens.EntryDetailScreen
import com.protech.ojtjournal.ui.screens.EntryEditorScreen
import com.protech.ojtjournal.ui.screens.HomeScreen
import com.protech.ojtjournal.ui.screens.StatsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Stats : Screen("stats")
    object EntryDetail : Screen("entry/{entryId}") {
        fun createRoute(entryId: Long): String = "entry/$entryId"
    }
    object EntryEditor : Screen("editor?entryId={entryId}") {
        fun createRoute(entryId: Long? = null): String = if (entryId != null) {
            "editor?entryId=$entryId"
        } else {
            "editor"
        }
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNewEntry = {
                    navController.navigate(Screen.EntryEditor.createRoute())
                },
                onEntryClick = { entryId ->
                    navController.navigate(Screen.EntryDetail.createRoute(entryId))
                },
                onStatsClick = {
                    navController.navigate(Screen.Stats.route)
                }
            )
        }
        
        composable(route = Screen.Stats.route) {
            StatsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.EntryDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            EntryDetailScreen(
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.EntryEditor.createRoute(entryId)) }
            )
        }
        
        composable(
            route = Screen.EntryEditor.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            EntryEditorScreen(
                entryId = if (entryId == -1L) null else entryId,
                onBack = { navController.popBackStack() },
                onSaved = { 
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
} 