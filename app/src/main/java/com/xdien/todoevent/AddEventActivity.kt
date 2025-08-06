package com.xdien.todoevent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.xdien.todoevent.ui.screens.AddEventScreen
import com.xdien.todoevent.ui.theme.TodoEventTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEventActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoEventTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AddEventScreen(
                        onNavigateBack = {
                            finish()
                        }
                    )
                }
            }
        }
    }
} 