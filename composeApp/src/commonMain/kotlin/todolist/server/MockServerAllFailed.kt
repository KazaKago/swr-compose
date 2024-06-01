package todolist.server

import kotlinx.coroutines.delay
import todolist.server.MockServer

object MockServerAllFailed : MockServer {

    override suspend fun getToDoList(): List<String> {
        delay(1000)
        throw NullPointerException()
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
