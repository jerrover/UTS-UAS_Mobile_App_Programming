package com.example.uts_uasmobileappprogramming.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductRecommendationScreen(navController: NavController) {
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text("Product Recommendations", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Based on your acne scars & fine lines")
                Spacer(modifier = Modifier.height(16.dp))
                // Filter Chips
                Row {
                    AssistChip(onClick = {}, label = { Text("Price") })
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text("Brand") })
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text("Vegan") })
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Daftar Produk
            items(2) { index ->
                ProductCard(
                    productName = if (index == 0) "Niacinamide Serum" else "Hydrating Moisturizer",
                    suitability = if (index == 0) "Suitable for: Acne Scars" else "Suitable for: Early Fine Lines"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProductCard(productName: String, suitability: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Placeholder untuk gambar produk
            Spacer(modifier = Modifier.size(80.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(productName, fontWeight = FontWeight.Bold)
                Text(suitability, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* TODO: Navigasi ke detail produk */ }) {
                    Text("View Details")
                }
            }
        }
    }
}