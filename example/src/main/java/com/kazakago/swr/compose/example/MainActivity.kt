package com.kazakago.swr.compose.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.example.basic.*
import com.kazakago.swr.compose.example.todolist.ToDoListScreen
import com.kazakago.swr.compose.example.todolist.server.*
import com.kazakago.swr.compose.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val mockServer: MutableState<MockServer> = remember { mutableStateOf(MockServerSucceed) }
                val isClearCache: MutableState<Boolean> = remember { mutableStateOf(true) }
                CompositionLocalProvider(LocalMockServer provides mockServer.value) {
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") { MainScreen(navController, mockServer, isClearCache) }
                        composable("data_fetching") { DataFetchingScreen(navController) }
                        composable("global_configuration") { GlobalConfigurationScreen(navController) }
                        composable("error_handling") { ErrorHandlingScreen(navController) }
                        composable("auto_revalidation") { AutoRevalidationScreen(navController) }
                        composable("conditional_fetching") { ConditionalFetchingScreen(navController) }
                        composable("arguments") { ArgumentsScreen(navController) }
                        composable("mutation") { MutationScreen(navController) }
                        composable("todolist") { ToDoListScreen(navController) }
                        composable("pagination") { PaginationScreen(navController) }
                        composable("infinite_pagination") { InfinitePaginationScreen(navController) }
                        composable("prefetching") { PrefetchingScreen(navController, scope) }
                        composable("prefetching_next") { PrefetchingNextScreen(navController, scope) }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(
    navController: NavController,
    mockServer: MutableState<MockServer>,
    isClearCache: MutableState<Boolean>,
) {
    val swrCache = LocalSWRCache.current
    val swrSystemCache = LocalSWRSystemCache.current
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) })
        },
    ) { paddingValue ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Clear cache before transition", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.size(16.dp))
                    Switch(checked = isClearCache.value, onCheckedChange = { isClearCache.value = !isClearCache.value })
                }
                Spacer(Modifier.size(8.dp))
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "Basic Example", style = MaterialTheme.typography.titleLarge)
                        ExampleButton("Data Fetching") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("data_fetching")
                        }
                        ExampleButton("Global Configuration") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("global_configuration")
                        }
                        ExampleButton("Error Handling") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("error_handling")
                        }
                        ExampleButton("Auto Revalidation") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("auto_revalidation")
                        }
                        ExampleButton("Conditional Fetching") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("conditional_fetching")
                        }
                        ExampleButton("Arguments") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("arguments")
                        }
                        ExampleButton("Mutation") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("mutation")
                        }
                        ExampleButton("Pagination") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("pagination")
                        }
                        ExampleButton("Infinite Pagination") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("infinite_pagination")
                        }
                        ExampleButton("Prefetching") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            navController.navigate("prefetching")
                        }
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = "ToDo List Example", style = MaterialTheme.typography.titleLarge)
                        ExampleButton("with All Succeed") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            mockServer.value = MockServerSucceed
                            navController.navigate("todolist")
                        }
                        ExampleButton("with All Failed") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            mockServer.value = MockServerAllFailed
                            navController.navigate("todolist")
                        }
                        ExampleButton("with Mutation Failed") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            mockServer.value = MockServerMutationFailed
                            navController.navigate("todolist")
                        }
                        ExampleButton("with Random Failed") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            mockServer.value = MockServerRandomFailed
                            navController.navigate("todolist")
                        }
                        ExampleButton("with Loading Slow") {
                            if (isClearCache.value) {
                                swrCache.clear()
                                swrSystemCache.clear()
                            }
                            mockServer.value = MockServerLoadingSlow
                            navController.navigate("todolist")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExampleButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick,
    ) {
        Text(text = text)
    }
}


@Preview
@Composable
fun PreviewMainScreen() {
    AppTheme {
        MainScreen(
            navController = rememberNavController(),
            mockServer = remember { mutableStateOf(MockServerSucceed) },
            isClearCache = remember { mutableStateOf(true) }
        )
    }
}
