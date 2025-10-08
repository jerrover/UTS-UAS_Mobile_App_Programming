package com.example.uts_uasmobileappprogramming.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uts_uasmobileappprogramming.navigation.Screen

@Composable
fun AnalysisResultScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Analysis Result", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder untuk gambar wajah dengan overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Image with analysis overlay here")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AnalysisInfoCard("Acne Scars", "Inflamasi poripori yang memicu...")
            AnalysisInfoCard("Early Fine Lines", "Tanda awal penuaan...")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.navigate(Screen.ProductRecommendation.route) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("See Product Recommendations")
        }
    }
}

@Composable
fun AnalysisInfoCard(title: String, description: String) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, fontSize = 12.sp)
        }
    }
}