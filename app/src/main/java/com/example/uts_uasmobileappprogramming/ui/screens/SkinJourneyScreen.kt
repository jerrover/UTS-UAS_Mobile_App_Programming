package com.example.uts_uasmobileappprogramming.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SkinJourneyScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Skin Journey", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Great Progress!", fontWeight = FontWeight.SemiBold)
                Text("Dark spots reduced by 12% in the last weeks.")

                // Placeholder untuk Grafik
                Box(
                    modifier = Modifier.height(150.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Text("Graph placeholder")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Compare Photos", fontWeight = FontWeight.Bold)
        // Placeholder perbandingan foto
    }
}