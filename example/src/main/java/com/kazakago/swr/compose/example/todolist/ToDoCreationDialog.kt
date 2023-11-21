package com.kazakago.swr.compose.example.todolist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kazakago.swr.compose.example.ui.theme.AppTheme

@Composable
fun ToDoCreationDialog(
    onSubmit: (text: String) -> Unit,
    onCancel: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = {}) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = text,
                    singleLine = true,
                    onValueChange = { text = it },
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSubmit(text) },
                ) {
                    Text(text = "OK")
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCancel,
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewToDoCreationDialog() {
    AppTheme {
        Surface {
            ToDoCreationDialog(
                onSubmit = {},
                onCancel = {},
            )
        }
    }
}
