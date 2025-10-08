package com.example.uts_uasmobileappprogramming.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uts_uasmobileappprogramming.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }
        // Navigasi ke MainScreen yang berisi bottom nav
        composable(Screen.Main.route) {
            MainScreen(mainNavController = navController)
        }
        // Layar yang diakses dari dalam MainScreen
        composable(Screen.Camera.route) {
            CameraScreen(navController = navController)
        }
        composable(Screen.AnalysisResult.route) {
            AnalysisResultScreen(navController = navController)
        }
    }
}