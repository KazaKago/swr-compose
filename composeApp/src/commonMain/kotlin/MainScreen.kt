import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import org.jetbrains.compose.ui.tooling.preview.Preview
import todolist.server.MockServer
import todolist.server.MockServerAllFailed
import todolist.server.MockServerLoadingSlow
import todolist.server.MockServerMutationFailed
import todolist.server.MockServerRandomFailed
import todolist.server.MockServerSucceed
import ui.theme.AppTheme

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
            TopAppBar(title = { Text("SWR Compose") })
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
