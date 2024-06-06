package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class ErrorRetryIntervalOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun errorRetryIntervalOption5seconds() {
        val key = Random.nextInt().toString()
        val errorRetryIntervalList = mutableListOf<Duration>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                errorRetryInterval = 5.seconds
                onErrorRetry = { _, _, config, _, _ ->
                    errorRetryIntervalList += config.errorRetryInterval
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf(5.seconds), errorRetryIntervalList)
    }

    @Test
    fun errorRetryIntervalOption10seconds() {
        val key = Random.nextInt().toString()
        val errorRetryIntervalList = mutableListOf<Duration>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                errorRetryInterval = 10.seconds
                onErrorRetry = { _, _, config, _, _ ->
                    errorRetryIntervalList += config.errorRetryInterval
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf(10.seconds), errorRetryIntervalList)
    }
}
