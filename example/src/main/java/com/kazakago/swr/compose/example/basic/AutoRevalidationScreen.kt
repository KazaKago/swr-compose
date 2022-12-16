package com.kazakago.swr.compose.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.seconds

private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    val sdf = SimpleDateFormat("yyyy/MM/dd, E, HH:mm:ss", Locale.getDefault())
    sdf.format(Date())
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AutoRevalidationScreen(navController: NavController) {
    val (data, _, _, isValidating) = useSWR(key = "/auto_revalidation", fetcher = fetcher) {
        revalidateOnMount = true     // default is null
        revalidateIfStale = true     // default is true
        revalidateOnFocus = true     // default is true
        revalidateOnReconnect = true // default is true
        refreshInterval = 10.seconds // default is 0.seconds (=disable)
        refreshWhenHidden = false    // default is false
        refreshWhenOffline = false   // default is false
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Revalidation") },
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
            contentAlignment = Alignment.TopCenter,
        ) {
            if (data == null) {
                LoadingContent()
                return@Box
            }
            if (isValidating) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(data)
            }
        }
    }
}
