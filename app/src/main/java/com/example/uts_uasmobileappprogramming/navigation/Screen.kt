package com.example.uts_uasmobileappprogramming.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object ProfileSetup : Screen("profile_setup")
    object Main : Screen("main") // Rute untuk layar utama dengan bottom nav
    object Home : Screen("home")
    object Camera : Screen("camera")
    object AnalysisResult : Screen("analysis_result")
    object ProductRecommendation : Screen("product_recommendation")
    object SkinJourney : Screen("skin_journey")
    object Profile : Screen("profile")
}