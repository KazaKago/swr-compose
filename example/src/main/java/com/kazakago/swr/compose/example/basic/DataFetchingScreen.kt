package com.kazakago.swr.compose.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kazakago.swr.compose.example.ui.ErrorContent
import com.kazakago.swr.compose.example.ui.LoadingContent
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay

private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    "Hello world!"
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DataFetchingScreen(navController: NavController) {
    val (data, error) = useSWR(key = "/data_fetching", fetcher = fetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Fetching") },
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
            if (error != null) {
                ErrorContent()
            } else if (data == null) {
                LoadingContent()
            } else {
                Text(data)
            }
        }
    }
}
