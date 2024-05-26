package com.kazakago.swr.compose.mutation

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
public class RevalidateOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun noRevalidate() {
        val key = Random.nextInt().toString()
        var result: () -> String = { "fetched_1" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { "fetched_2" }
        scope.launch {
            stateList.last().mutate(data = {
                delay(100)
                "mutated"
            }) {
                revalidate = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "fetched_1", "mutated"), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, false), stateList.map { it.isValidating })
    }
}
