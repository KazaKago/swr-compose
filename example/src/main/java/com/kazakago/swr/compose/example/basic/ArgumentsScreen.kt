package com.kazakago.swr.compose.example.basic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kazakago.swr.compose.example.ui.LoadingContent
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay

private val fetcher: suspend (key: String) -> String = { key ->
    delay(1000)
    "Argument is '$key'"
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ArgumentsScreen(navController: NavController) {
    val (data1) = useSWR(key = "/arguments/google", fetcher = fetcher)
    val (data2) = useSWR(key = "/arguments/apple", fetcher = fetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arguments") },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center,
        ) {
            if (data1 == null || data2 == null) {
                LoadingContent()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(data1)
                    Text(data2)
                }
            }
        }
    }
}
