package com.kazakago.swr.compose.mutation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class PopulateCacheOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun noPopulateCache() {
        val key = object {}.javaClass.enclosingMethod?.name
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
                populateCache = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched_1", "fetched_1", "fetched_2")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)
    }
}
