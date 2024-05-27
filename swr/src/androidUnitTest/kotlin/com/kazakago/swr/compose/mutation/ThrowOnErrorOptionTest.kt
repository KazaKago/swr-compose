package com.kazakago.swr.compose.mutation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
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
class ThrowOnErrorOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun withThrowOnError() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().mutate(data = {
                    delay(100)
                    throw DummyException1
                }) {
                    throwOnError = true
                }
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
    fun noThrowOnError() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().mutate(data = {
                    delay(100)
                    throw DummyException1
                }) {
                    throwOnError = false
                }
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
        assertEquals(null, mutationError)
    }
}
