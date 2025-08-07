package com.xdien.todoevent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.xdien.todoevent.ui.screens.EventListScreen
import com.xdien.todoevent.ui.theme.TodoEventTheme
import com.xdien.todoevent.ui.viewmodel.TodoViewModel
import com.xdien.todoevent.EventFormActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoEventTheme {
                val viewModel: TodoViewModel = hiltViewModel()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Danh sách sự kiện") },
                            actions = {
                                IconButton(onClick = { viewModel.refreshEvents() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val intent = Intent(this@MainActivity, EventFormActivity::class.java)
                                        startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Create,
                                        contentDescription = "Create New Event"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val intent = Intent(this@MainActivity, PersonalEventComposeActivity::class.java)
                                        startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Personal Events (Compose)"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    EventListScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}