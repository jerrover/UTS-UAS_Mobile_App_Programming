package com.example.uts_uasmobileappprogramming.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
fun ProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Placeholder avatar
        Spacer(modifier = Modifier.size(90.dp))
        Text("Sarah M.", style = MaterialTheme.typography.headlineSmall)
        Text("Age 24")

        Spacer(modifier = Modifier.height(32.dp))

        ProfileInfoCard("Skin Type", "Combination")
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoCard("Product Preferences", "Vegan, Fragrance-Free")
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfoCard("My Routine", "Morning & Evening")

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = { /* TODO: Logout Logic */ }) {
            Text("Logout")
        }
    }
}

@Composable
fun ProfileInfoCard(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(content)
        }
    }
}