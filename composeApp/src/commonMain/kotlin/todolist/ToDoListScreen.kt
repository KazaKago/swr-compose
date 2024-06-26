package todolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kazakago.swr.compose.mutate.SWRMutate
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import todolist.server.LocalMockServer
import ui.ErrorContent
import ui.LoadingContent
import ui.theme.AppTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ToDoListScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    openToDoCreationDialog: MutableState<Boolean> = remember { mutableStateOf(false) },
    openToDoEditingDialog: MutableState<Pair<Int, String>?> = remember { mutableStateOf(null) },
    isMuting: MutableState<Boolean> = remember { mutableStateOf(false) },
    state: SWRState<String, List<String>> = useState(snackbarHostState),
    create: (text: String) -> Unit = useCreate(state.data, state.mutate, isMuting, snackbarHostState),
    edit: (index: Int, text: String) -> Unit = useEdit(state.data, state.mutate, isMuting, snackbarHostState),
    delete: (index: Int) -> Unit = useDeletion(state.data, state.mutate, isMuting, snackbarHostState),
    showSnackbar: (message: String) -> Unit = useShowSnackbar(snackbarHostState),
) {
    val (todoList, error, _, isValidating) = state
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ToDo List") },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { openToDoCreationDialog.value = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    ) { paddingValue ->
        val scope = rememberCoroutineScope()
//        val pullToRefreshState = rememberPullToRefreshState()
//        if (pullToRefreshState.isRefreshing) {
//            LaunchedEffect(Unit) {
//                state.mutate()
//                pullToRefreshState.endRefresh()
//            }
//        }
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .nestedScroll(pullToRefreshState.nestedScrollConnection)
                .padding(paddingValue),
        ) {
            if (todoList == null) {
                if (isValidating) {
                    LoadingContent()
                } else if (error != null) {
                    ErrorContent { scope.launch { state.mutate() } }
                }
            } else {
                if (isValidating || isMuting.value) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                ) {
                    items(todoList.size) { index ->
                        ToDoRow(index, todoList[index]) {
                            openToDoEditingDialog.value = index to it
                        }
                    }
                }
//                PullToRefreshContainer(
//                    modifier = Modifier.align(Alignment.TopCenter),
//                    state = pullToRefreshState,
//                )
                if (openToDoCreationDialog.value) {
                    ToDoCreationDialog(
                        onSubmit = { text ->
                            openToDoCreationDialog.value = false
                            create(text)
                        },
                        onCancel = {
                            openToDoCreationDialog.value = false
                        },
                    )
                }
                openToDoEditingDialog.value?.let { (index, value) ->
                    ToDoEditingDialog(initialText = value, onSubmit = { text ->
                        openToDoEditingDialog.value = null
                        edit(index, text)
                    }, onCancel = {
                        openToDoEditingDialog.value = null
                    }, onDelete = {
                        openToDoEditingDialog.value = null
                        delete(index)
                    })
                }
            }
        }
        LaunchedEffect(error) {
            if (error != null) showSnackbar("Validation failed.")
        }
    }
}

@Composable
private fun ToDoRow(
    index: Int,
    value: String,
    onClick: (value: String) -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onClick(value) },
    ) {
        Row(Modifier.padding(16.dp)) {
            Text(text = "$index:", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.size(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Preview
@Composable
fun PreviewToDoListScreen() {
    AppTheme {
        ToDoListScreen(
            navController = rememberNavController(),
            state = SWRState.empty(
                data = listOf(
                    "Remember the milk",
                    "Call bob at 5pm.",
                ),
            ),
            create = {},
            edit = { _, _ -> },
            delete = {},
        )
    }
}

@Composable
private fun useState(snackbarHostState: SnackbarHostState): SWRState<String, List<String>> {
    val mockServer = LocalMockServer.current
    val scope = rememberCoroutineScope()
    return useSWR("/get_todos/$mockServer", { mockServer.getToDoList() }) {
        onLoadingSlow = { _, _ ->
            scope.launch {
                snackbarHostState.showSnackbar("Loading is slow, Please wait..")
            }
        }
    }
}

@Composable
private fun useCreate(
    todoList: List<String>?,
    mutate: SWRMutate<String, List<String>>,
    isMuting: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState,
): (text: String) -> Unit {
    val scope = rememberCoroutineScope()
    val mockServer = LocalMockServer.current
    return { text ->
        scope.launch {
            isMuting.value = true
            runCatching {
                mutate(data = { mockServer.addToDo(text) }) {
                    revalidate = false
                    optimisticData = todoList?.toMutableList()?.apply {
                        add(text)
                    }
                }
            }.onFailure {
                launch { snackbarHostState.showSnackbar("Failed, Data was rollback.") }
            }
            isMuting.value = false
        }
    }
}

@Composable
private fun useEdit(
    todoList: List<String>?,
    mutate: SWRMutate<String, List<String>>,
    isMuting: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState,
): (index: Int, text: String) -> Unit {
    val scope = rememberCoroutineScope()
    val mockServer = LocalMockServer.current
    return { index, text ->
        scope.launch {
            isMuting.value = true
            runCatching {
                mutate(data = { mockServer.editToDo(index, text) }) {
                    revalidate = false
                    optimisticData = todoList?.toMutableList()?.apply {
                        set(index, text)
                    }
                }
            }.onFailure {
                launch { snackbarHostState.showSnackbar("Error, Data was rollback.") }
            }
            isMuting.value = false
        }
    }
}

@Composable
private fun useDeletion(
    todoList: List<String>?,
    mutate: SWRMutate<String, List<String>>,
    isMuting: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState,
): (index: Int) -> Unit {
    val scope = rememberCoroutineScope()
    val mockServer = LocalMockServer.current
    return { index ->
        scope.launch {
            isMuting.value = true
            runCatching {
                mutate(data = { mockServer.removeToDo(index) }) {
                    revalidate = false
                    optimisticData = todoList?.toMutableList()?.apply {
                        removeAt(index)
                    }
                }
            }.onFailure {
                launch { snackbarHostState.showSnackbar("Error, Data was rollback.") }
            }
            isMuting.value = false
        }
    }
}

@Composable
private fun useShowSnackbar(
    snackbarHostState: SnackbarHostState,
): (message: String) -> Unit {
    val scope = rememberCoroutineScope()
    return { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
}
