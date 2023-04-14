package com.kazakago.swr.compose.example.basic

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.example.ui.LoadingContent
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GlobalConfigurationScreen(navController: NavController) {
    val context = LocalContext.current
    SWRConfig(options = {
        refreshInterval = 3.seconds
        fetcher = { key ->
            delay(1000)
            "Response of $key"
        }
        onError = { error, _, _ ->
            Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
        }
    }) {
        val (events) = useSWR<String, String>(key = "/global_configuration/events")
        val (projects) = useSWR<String, String>(key = "/global_configuration/projects")
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Global Configuration") },
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
                if (events == null || projects == null) {
                    LoadingContent()
                } else {
                    Column {
                        Text(events)
                        Text(projects)
                    }
                }
            }
        }
    }
}
