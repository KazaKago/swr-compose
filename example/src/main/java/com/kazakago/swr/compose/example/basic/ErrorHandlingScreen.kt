package com.kazakago.swr.compose.example.basic

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.kazakago.swr.compose.example.ui.ErrorContent
import com.kazakago.swr.compose.example.ui.LoadingContent
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    throw NullPointerException()
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ErrorHandlingScreen(navController: NavController) {
    val context = LocalContext.current
    val (data, error, isValidating, mutate) = useSWR(key = "/error_handling", fetcher = fetcher) {
        shouldRetryOnError = true           // default is true
        errorRetryCount = 3                 // default is null
        errorRetryInterval = 5.seconds      // default is 5.seconds
        onError = { error, key, config ->   // default is null
            Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
        }
//        onErrorRetry = { _, _, _, _, _ -> // default is `com.kazakago.compose.swr.retry.SWRRetryDefault.kt`
//        }
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Error Handling") },
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
            if (data == null && isValidating) {
                LoadingContent()
                return@Box
            }
            if (error != null) {
                ErrorContent {
                    scope.launch { mutate() }
                }
                return@Box
            }
            if (data == null) {
                LoadingContent()
                return@Box
            }
            Text(data)
        }
    }
}
