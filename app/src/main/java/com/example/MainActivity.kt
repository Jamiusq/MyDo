package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TaskRepository
import com.example.data.TodoDatabase
import com.example.ui.TodoScreen
import com.example.ui.TodoViewModel
import com.example.ui.TodoViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          // Initialize local persistence database, repo and ViewModel
          val context = LocalContext.current
          val database = TodoDatabase.getDatabase(context)
          val repository = TaskRepository(database.taskDao())
          val todoViewModel: TodoViewModel = viewModel(
              factory = TodoViewModelFactory(repository)
          )

          TodoScreen(
              viewModel = todoViewModel,
              modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

