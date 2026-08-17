package com.example.thewatcher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit
) {
    val navController = rememberNavController()
    var showHistory by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onStartMonitoring = onStartMonitoring,
                onStopMonitoring = onStopMonitoring,
                onOpenHistory = { navController.navigate("history") }
            )
        }
        composable("history") {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
