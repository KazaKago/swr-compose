package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
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
public class ShouldRetryOnErrorOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun withShouldRetryOnError() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                shouldRetryOnError = true
            }
        }

        composeTestRule.mainClock.advanceTimeBy(15000)
        assertEquals(listOf(null, null, null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1, DummyException1, DummyException1), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    public fun noShouldRetryOnError() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                shouldRetryOnError = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(15000)
        assertEquals(listOf(null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }
}
