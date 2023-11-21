package com.kazakago.swr.compose.example.basic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kazakago.swr.compose.example.ui.LoadingContent
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val fetcher: suspend (key: String) -> String = {
    delay(1000)
    "Fetched Data"
}

private val mutator: suspend () -> String = {
    delay(1000)
    "Mutated Data"
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MutationScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val (data, _, _, _, mutate) = useSWR(key = "/mutation", fetcher = fetcher)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mutation") },
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
            if (data == null) {
                LoadingContent()
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(data)
                    Row {
                        Button(onClick = {
                            scope.launch {
                                mutate(data = mutator) {
                                    optimisticData = "Optimistic Data" // default is null
                                    revalidate = false                 // default is true
                                    populateCache = true               // default is true
                                    rollbackOnError = true             // default is true
                                }
                            }
                        }) {
                            Text("mutate")
                        }
                    }
                }
            }
        }
    }
}
