package basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kazakago.swr.compose.useSWRInfinite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val fetcher: suspend (key: String) -> List<String> = { key ->
    delay(1000)
    List(10) { "$key - $it" }
}

private val getKey: (pageIndex: Int, previousPageData: List<String>?) -> String? = { pageIndex, previousPageData ->
    if (previousPageData != null && previousPageData.isEmpty()) null
    else "/infinite_paginationKey/$pageIndex"
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InfinitePaginationScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val (data, error, isLoading, isValidating, mutate, size, setSize) = useSWRInfinite(getKey, fetcher) {
        initialSize = 2              // default is 1
//        revalidateAll = false      // default is false
//        revalidateFirstPage = true // default is true
//        persistSize = false        // default is false
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Infinite Pagination") },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { mutate() } }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            if (data == null) {
                CircularProgressIndicator()
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(data.size) { index ->
                        InfinitePaginationRow(data[index])
                    }
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            OutlinedButton(onClick = { setSize(size + 1) }) {
                                Text("Load More")
                            }
                            Spacer(Modifier.size(8.dp))
                            Text("${data.flatMap { it ?: emptyList() }.size} items listed")
                            Spacer(Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfinitePaginationRow(page: List<String>?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (page == null) {
            CircularProgressIndicator()
        } else {
            page.forEach { content ->
                Text(content, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
