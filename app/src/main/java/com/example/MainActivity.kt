package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.local.PrimeDatabase
import com.example.data.repository.PrimeRepository
import com.example.ui.PrimeCheckerScreen
import com.example.ui.PrimeViewModel
import com.example.ui.ViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Database & Repository
    val database = PrimeDatabase.getDatabase(applicationContext)
    val repository = PrimeRepository(database.primeCheckDao())
    val factory = ViewModelFactory(repository)
    
    // Obtain ViewModel instance
    val viewModel = ViewModelProvider(this, factory)[PrimeViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          PrimeCheckerScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
