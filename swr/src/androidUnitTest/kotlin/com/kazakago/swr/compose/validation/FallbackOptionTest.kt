package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.config.SWRConfig
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
class FallbackOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun noFallback() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            SWRConfig(options = {
                fallback = emptyMap()
            }) {
                stateList += useSWR(key = key, fetcher = {
                    delay(100)
                    "fetched"
                })
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf(null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun withFallback() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            SWRConfig(options = {
                fallback = mapOf<Any, Any>(key to "fallback")
            }) {
                stateList += useSWR(key = key, fetcher = {
                    delay(100)
                    "fetched"
                })
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf("fallback"), stateList.map { it.data })
        assertEquals(listOf(null), stateList.map { it.error })
        assertEquals(listOf(false), stateList.map { it.isLoading })
        assertEquals(listOf(false), stateList.map { it.isValidating })
    }

    @Test
    fun withFallbackAndRevalidateOnMount() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            SWRConfig(options = {
                fallback = mapOf<Any, Any>(key to "fallback")
            }) {
                stateList += useSWR(key = key, fetcher = {
                    delay(100)
                    "fetched"
                }) {
                    revalidateOnMount = true
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf("fallback", "fallback", "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }
}
