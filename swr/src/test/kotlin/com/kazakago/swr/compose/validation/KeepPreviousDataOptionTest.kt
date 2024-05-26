package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
public class KeepPreviousDataOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun noKeepPreviousData() {
        val key = Random.nextInt().toString()
        var getKey = { "${key}_1" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR<String, String>(key = getKey, fetcher = {
                delay(100)
                "fetched_$it"
            }) {
                keepPreviousData = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched_${key}_1"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })

        getKey = { "${key}_2" }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(
            listOf(null, null, "fetched_${key}_1", null, null, "fetched_${key}_2"),
            stateList.map { it.data })
        assertEquals(
            listOf(null, null, null, null, null, null),
            stateList.map { it.error })
        assertEquals(
            listOf(false, true, false, false, true, false),
            stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, false, true, false), stateList.map { it.isValidating })
    }

    @Test
    public fun withKeepPreviousData() {
        val key = Random.nextInt().toString()
        var getKey = { "${key}_1" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR<String, String>(key = getKey, fetcher = {
                delay(100)
                "fetched_$it"
            }) {
                keepPreviousData = true
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched_${key}_1"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })

        getKey = { "${key}_2" }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched_${key}_1", "fetched_${key}_1", "fetched_${key}_1", "fetched_${key}_2"), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, false, true, false), stateList.map { it.isValidating })
    }
}
