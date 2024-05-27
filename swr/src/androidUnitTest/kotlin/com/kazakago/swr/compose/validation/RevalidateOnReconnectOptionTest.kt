package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
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
class RevalidateOnReconnectOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun withRevalidateOnReconnect() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateOnReconnect = true
            }
        }

        networkRule.changeNetwork(isConnected = true)

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })

        networkRule.changeNetwork(isConnected = true)

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched", "fetched", "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun noRevalidateOnReconnect() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateOnReconnect = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })

        networkRule.changeNetwork(isConnected = true)

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }
}
