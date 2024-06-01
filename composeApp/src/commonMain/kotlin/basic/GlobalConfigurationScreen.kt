package basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.LoadingContent
import kotlin.time.Duration.Companion.seconds

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GlobalConfigurationScreen(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    SWRConfig(options = {
        refreshInterval = 3.seconds
        fetcher = { key ->
            delay(1000)
            "Response of $key"
        }
        onError = { error, _, _ ->
            scope.launch {
                snackbarHostState.showSnackbar(error.toString())
            }
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
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
