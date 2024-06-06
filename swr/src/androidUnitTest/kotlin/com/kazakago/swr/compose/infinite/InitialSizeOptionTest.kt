package com.kazakago.swr.compose.infinite

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRInfiniteState
import com.kazakago.swr.compose.useSWRInfinite
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class InitialSizeOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun initialSize1() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRInfinite(getKey = { pageIndex, _ -> "${key}_${pageIndex}" }, fetcher = {
                delay(100)
                "fetched"
            }) {
                initialSize = 1
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, listOf("fetched")), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }

    @Test
    fun initialSize3() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRInfinite(getKey = { pageIndex, _ -> "${key}_${pageIndex}" }, fetcher = {
                delay(100)
                "fetched"
            }) {
                initialSize = 3
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, listOf("fetched", "fetched", "fetched")), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }
}
