package com.kazakago.swr.compose.prefetch

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import com.kazakago.swr.compose.useSWRPreload
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class UsePreloadTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun prefetch() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key) {
                revalidateOnMount = false
            }
            val preload = useSWRPreload(key = key, fetcher = {
                delay(100)
                "fetched"
            })
            LaunchedEffect(Unit) {
                preload()
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf(null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun prefetchFailed() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key) {
                revalidateOnMount = false
            }
            val preload = useSWRPreload(key = key, fetcher = {
                delay(100)
                throw DummyException1
            })
            LaunchedEffect(Unit) {
                preload()
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf(null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }
}
