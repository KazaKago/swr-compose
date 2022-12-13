package com.kazakago.swr.compose.example.basic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kazakago.swr.compose.example.ui.LoadingContent
import com.kazakago.swr.compose.useSWR
import com.kazakago.swr.compose.useSWRPreload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val fetcher: suspend (key: String) -> String = {
    delay(3000)
    "Hello world!"
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PrefetchingScreen(navController: NavController, scope: CoroutineScope) {
    val preload = useSWRPreload(key = "/prefetching", fetcher = fetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prefetching") },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { scope.launch { preload() } }) {
                Text("Start prefetch (3s)")
            }
            Spacer(Modifier.size(16.dp))
            Button(onClick = { navController.navigate("prefetching_next") }) {
                Text("Move to next Screen")
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PrefetchingNextScreen(navController: NavController, scope: CoroutineScope) {
    val (data) = useSWR(key = "/prefetching", fetcher = fetcher, scope = scope)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prefetching Next") },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
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
            if (data == null) {
                LoadingContent()
                return@Box
            }
            Text(data)
        }
    }
}
