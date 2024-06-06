package com.kazakago.swr.compose.mutation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.DummyException2
import com.kazakago.swr.compose.DummyException3
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class SWRMutateTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun validate_mutation() {
        val key = Random.nextInt().toString()
        var result: () -> String = { "fetched_1" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { "fetched_2" }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched_1", "fetched_1", "fetched_2"), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun validate_mutationFailed() {
        val key = Random.nextInt().toString()
        var result: () -> String = { "fetched" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { throw DummyException1 }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched", "fetched", "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, DummyException1), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun validate_mutationWithData() {
        val key = Random.nextInt().toString()
        var result: () -> String = { "fetched_1" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { "fetched_2" }
        scope.launch {
            stateList.last().mutate(data = {
                delay(100)
                "mutated"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched_1", "mutated", "fetched_2"), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun validate_mutationWithDataFailed() {
        val key = Random.nextInt().toString()
        var result: () -> String = { "fetched" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { throw IllegalArgumentException() }
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().mutate(data = {
                    delay(100)
                    throw DummyException1
                })
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
        assertEquals(DummyException1, mutationError)
    }

    @Test
    fun validateFailed_mutation() {
        val key = Random.nextInt().toString()
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { "fetched" }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1, DummyException1, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun validateFailed_mutationFailed() {
        val key = Random.nextInt().toString()
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { throw DummyException2 }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1, DummyException1, DummyException2), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun validateFailed_mutationWithData() {
        val key = Random.nextInt().toString()
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { "fetched" }
        scope.launch {
            stateList.last().mutate(data = {
                delay(100)
                "mutated"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, null, "mutated", "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun validateFailed_mutationWithDataFailed() {
        val key = Random.nextInt().toString()
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { throw DummyException2 }
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().mutate(data = {
                    delay(100)
                    throw DummyException3
                })
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
        assertEquals(DummyException3, mutationError)
    }
}
