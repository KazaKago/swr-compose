package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class LoadingTimeoutOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun focusThrottleInterval3Seconds() {
        val key = Random.nextInt().toString()
        val keyList = mutableListOf<String>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(10.seconds)
                "fetched"
            }) {
                loadingTimeout = 3.seconds
                onLoadingSlow = { key, _ ->
                    keyList.add(key)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2000)
        assertEquals(emptyList(), keyList)
        composeTestRule.mainClock.advanceTimeBy(2000)
        assertEquals(listOf(key), keyList)
    }

    @Test
    fun focusThrottleInterval5Seconds() {
        val key = Random.nextInt().toString()
        val keyList = mutableListOf<String>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(10.seconds)
                "fetched"
            }) {
                loadingTimeout = 5.seconds
                onLoadingSlow = { key, _ ->
                    keyList.add(key)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2000)
        assertEquals(emptyList(), keyList)
        composeTestRule.mainClock.advanceTimeBy(2000)
        assertEquals(emptyList(), keyList)
    }

    @Test
    fun focusThrottleInterval0Seconds() {
        val key = Random.nextInt().toString()
        val keyList = mutableListOf<String>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(10.seconds)
                "fetched"
            }) {
                loadingTimeout = 0.seconds
                onLoadingSlow = { key, _ ->
                    keyList.add(key)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2000)
        assertEquals(listOf(key), keyList)
        composeTestRule.mainClock.advanceTimeBy(2000)
        assertEquals(listOf(key), keyList)
    }
}
