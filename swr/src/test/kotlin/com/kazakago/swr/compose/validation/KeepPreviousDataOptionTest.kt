package com.kazakago.swr.compose.validation

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
public class KeepPreviousDataOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun noKeepPreviousData() {
        val key = object {}.javaClass.enclosingMethod?.name
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
        stateList.map { it.data } shouldBe listOf(null, null, "fetched_${key}_1")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)

        getKey = { "${key}_2" }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched_${key}_1", null, null, "fetched_${key}_2")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false)
    }

    @Test
    public fun withKeepPreviousData() {
        val key = object {}.javaClass.enclosingMethod?.name
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
        stateList.map { it.data } shouldBe listOf(null, null, "fetched_${key}_1")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)

        getKey = { "${key}_2" }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched_${key}_1", "fetched_${key}_1", "fetched_${key}_1", "fetched_${key}_2")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false)
    }
}
