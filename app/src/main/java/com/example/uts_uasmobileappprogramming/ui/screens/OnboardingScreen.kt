package com.example.uts_uasmobileappprogramming.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.uts_uasmobileappprogramming.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    // Inisialisasi pagerState dengan lambda untuk jumlah halaman
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "SkinGenie",
                    subtitle = "Your AI Skin Companion"
                )
                1 -> OnboardingPage(
                    title = "Scan & Understand",
                    subtitle = "Our AI analyzes your skin for personalized insights."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (pagerState.currentPage < pagerState.pageCount - 1) {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            } else {
                navController.popBackStack()
                navController.navigate(Screen.ProfileSetup.route)
            }
        }) {
            Text(
                text = if (pagerState.currentPage < pagerState.pageCount - 1) "Next" else "Get Started",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// --- FUNGSI YANG DIPERBAIKI DENGAN BOX ---
@Composable
fun OnboardingPage(title: String, subtitle: String) {
    // Menggunakan Box untuk memastikan alignment di tengah
    Box(
        modifier = Modifier.fillMaxSize(), // Memenuhi seluruh area halaman pager
        contentAlignment = Alignment.Center // Memusatkan semua elemen di dalamnya
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
            // Tidak perlu verticalArrangement karena sudah diatur oleh Box
        ) {
            // Anda dapat menambahkan komponen Image di sini
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}