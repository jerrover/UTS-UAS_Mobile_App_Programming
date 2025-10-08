package com.example.uts_uasmobileappprogramming // Sesuai dengan folder Anda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.uts_uasmobileappprogramming.navigation.AppNavigation
import com.example.uts_uasmobileappprogramming.ui.theme.DermaMindTheme // Nama tema yang digunakan

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DermaMindTheme { // Menggunakan tema DermaMind
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}