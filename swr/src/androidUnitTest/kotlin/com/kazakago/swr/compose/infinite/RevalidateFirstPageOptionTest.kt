package com.kazakago.swr.compose.infinite

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRInfiniteState
import com.kazakago.swr.compose.useSWRInfinite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class RevalidateFirstPageOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun withRevalidateFirstPage() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        val fetchCountMap = mutableMapOf<String, Int>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRInfinite(getKey = { pageIndex, _ -> "${key}_${pageIndex}" }, fetcher = {
                delay(100)
                val fetchCount = fetchCountMap.getOrPut(it) { 0 } + 1
                fetchCountMap[it] = fetchCount
                "${it}_${fetchCount}"
            }) {
                revalidateFirstPage = true
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.last().apply {
            scope.launch { setSize(size + 1) }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, listOf("${key}_1_1"), listOf("${key}_1_1", null), listOf("${key}_1_1", null), listOf("${key}_1_2", "${key}_2_1")), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, false, true, false), stateList.map { it.isValidating })
        assertEquals(listOf(1, 1, 1, 2, 2, 2), stateList.map { it.size })

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.last().apply {
            scope.launch { setSize(size + 2) }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, listOf("${key}_1_1"), listOf("${key}_1_1", null), listOf("${key}_1_1", null), listOf("${key}_1_2", "${key}_2_1"), listOf("${key}_1_2", "${key}_2_1", null, null), listOf("${key}_1_2", "${key}_2_1", null, null), listOf("${key}_1_3", "${key}_2_1", "${key}_3_1", "${key}_4_1")), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false, false, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, false, true, false, false, true, false), stateList.map { it.isValidating })
        assertEquals(listOf(1, 1, 1, 2, 2, 2, 4, 4, 4), stateList.map { it.size })
    }

    @Test
    fun noRevalidateFirstPage() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        val fetchCountMap = mutableMapOf<String, Int>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRInfinite(getKey = { pageIndex, _ -> "${key}_${pageIndex}" }, fetcher = {
                delay(100)
                val fetchCount = fetchCountMap.getOrPut(it) { 0 } + 1
                fetchCountMap[it] = fetchCount
                "${it}_${fetchCount}"
            }) {
                revalidateFirstPage = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.last().apply {
            scope.launch { setSize(size + 1) }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, listOf("${key}_1_1"), listOf("${key}_1_1", null), listOf("${key}_1_1", null), listOf("${key}_1_1", "${key}_2_1")), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, false, true, false), stateList.map { it.isValidating })
        assertEquals(listOf(1, 1, 1, 2, 2, 2), stateList.map { it.size })

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.last().apply {
            scope.launch { setSize(size + 2) }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, listOf("${key}_1_1"), listOf("${key}_1_1", null), listOf("${key}_1_1", null), listOf("${key}_1_1", "${key}_2_1"), listOf("${key}_1_1", "${key}_2_1", null, null), listOf("${key}_1_1", "${key}_2_1", null, null), listOf("${key}_1_1", "${key}_2_1", "${key}_3_1", "${key}_4_1")), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false, false, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, false, true, false, false, true, false), stateList.map { it.isValidating })
        assertEquals(listOf(1, 1, 1, 2, 2, 2, 4, 4, 4), stateList.map { it.size })
    }
}
