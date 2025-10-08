package com.example.uts_uasmobileappprogramming.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uts_uasmobileappprogramming.navigation.Screen

@Composable
fun ProfileSetupScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Tell Us About Your Skin", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("This helps us tailor your recommendations.", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Skin Type", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        SelectableChipGroup(items = listOf("Oily", "Dry", "Combination", "Normal"))

        Spacer(modifier = Modifier.height(24.dp))

        Text("Product Preferences", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        SelectableChipGroup(items = listOf("Fragrance-Free", "Vegan", "Local", "Halal"))

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                navController.popBackStack()
                navController.navigate(Screen.Main.route)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Complete Setup")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun SelectableChipGroup(items: List<String>) {
    var selectedItem by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            SelectableChip(text = item, selected = selectedItem == item) {
                selectedItem = item
            }
        }
    }
}