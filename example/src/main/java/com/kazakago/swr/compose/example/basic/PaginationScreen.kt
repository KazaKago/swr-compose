package com.kazakago.swr.compose.example.basic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay

private val fetcher: suspend (key: PaginationKey) -> List<String> = { key ->
    delay(1000)
    List(10) { "${key.page} - $it" }
}

private data class PaginationKey(
    val page: Int,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PaginationScreen(navController: NavController) {
    var pageCount by rememberSaveable { mutableStateOf(1) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagination") },
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
            LazyColumn(Modifier.fillMaxSize()) {
                items(pageCount) { page ->
                    PaginationRow(page = page)
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        OutlinedButton(onClick = { pageCount += 1 }) {
                            Text("Load More")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginationRow(page: Int) {
    val (list) = useSWR(key = PaginationKey(page), fetcher = fetcher)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (list == null) {
            CircularProgressIndicator()
        } else {
            list.forEach { content ->
                Text(content, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
