package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.DummyException2
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
public class OnErrorRetryOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun defaultOnErrorRetry() {
        val key = Random.nextInt().toString()
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(100)
        result = { throw DummyException2 }

        composeTestRule.mainClock.advanceTimeBy(15000)
        assertEquals(listOf(null, null, null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1, DummyException1, DummyException2), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    public fun customOnErrorRetry() {
        val key = Random.nextInt().toString()
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            }) {
                onErrorRetry = { _, key, _, revalidate, options ->
                    delay(100)
                    revalidate(key, options)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(100)
        result = { throw DummyException2 }

        composeTestRule.mainClock.advanceTimeBy(600)
        assertEquals(listOf(null, null, null, null, null, null, null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1, DummyException1, DummyException2, DummyException2, DummyException2, DummyException2, DummyException2), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false, false, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false, true, false, true, false), stateList.map { it.isValidating })
    }
}
