package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.cache.LocalSWRCache
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
class RevalidateIfStaleOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun withRevalidateIfStale() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            LocalSWRCache.current.state<String, String>(key = key).let {
                if (it.value == null) it.value = "cached"
            }
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateIfStale = true
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf("cached", "cached", "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun noRevalidateIfStale() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            LocalSWRCache.current.state<String, String>(key = key).let {
                if (it.value == null) it.value = "cached"
            }
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateIfStale = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf("cached"), stateList.map { it.data })
        assertEquals(listOf(null), stateList.map { it.error })
        assertEquals(listOf(false), stateList.map { it.isLoading })
        assertEquals(listOf(false), stateList.map { it.isValidating })
    }
}
