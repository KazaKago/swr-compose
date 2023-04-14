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
import java.util.*

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
