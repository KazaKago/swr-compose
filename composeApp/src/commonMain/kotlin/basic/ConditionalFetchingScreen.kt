package basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import ui.LoadingContent
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay

private val userFetcher: suspend (key: String) -> String = {
    delay(1000)
    "User1"
}

private val projectsFetcher: suspend (key: String) -> List<String> = {
    delay(1000)
    listOf("Project1", "Project2", "Project3")
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConditionalFetchingScreen(navController: NavController) {
    val (user, _, _) = useSWR(key = "/confidential_fetching/user", fetcher = userFetcher)
    val (projects, _, _) = useSWR(key = if (user != null) "/confidential_fetching/$user/projects" else null, fetcher = projectsFetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conditional Fetching") },
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
            if (user == null) {
                LoadingContent()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(user)
                    if (projects == null) {
                        CircularProgressIndicator()
                    } else {
                        Text(projects.joinToString(", "))
                    }
                }
            }
        }
    }
}
