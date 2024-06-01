package basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ui.LoadingContent
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
            } else {
                Text(data)
            }
        }
    }
}
