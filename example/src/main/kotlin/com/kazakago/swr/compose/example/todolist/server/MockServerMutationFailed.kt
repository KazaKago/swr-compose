package com.kazakago.swr.compose.example.todolist.server

import kotlinx.coroutines.delay

object MockServerMutationFailed : MockServer {

    private var todoList: List<String> = listOf(
        "Remember the milk",
        "Call bob at 5pm.",
    )

    override suspend fun getToDoList(): List<String> {
        delay(1000)
        return todoList
    }

    override suspend fun addToDo(value: String): List<String> {
        delay(1000)
        throw NullPointerException()
    }

    override suspend fun editToDo(index: Int, value: String): List<String> {
        delay(1000)
        throw NullPointerException()
    }

    override suspend fun removeToDo(index: Int): List<String> {
        delay(1000)
        throw NullPointerException()
    }
}
