package com.kazakago.swr.compose.example.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay

private val fetcher: suspend (key: String) -> List<String> = { key ->
    delay(1000)
    List(10) { "$key - $it" }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PaginationScreen(navController: NavController) {
    var pageCount by rememberSaveable { mutableIntStateOf(1) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagination") },
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
    val (list) = useSWR(key = "/pagination/$page", fetcher = fetcher)
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
