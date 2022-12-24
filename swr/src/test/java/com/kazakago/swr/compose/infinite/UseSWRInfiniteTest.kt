package com.kazakago.swr.compose.infinite

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRInfiniteState
import com.kazakago.swr.compose.useSWRInfinite
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class UseSWRInfiniteTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun validate() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRInfinite(getKey = { pageIndex, _ -> "${key}_${pageIndex}" }, fetcher = {
                delay(100)
                "fetched"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, listOf("fetched"))
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)
    }

    @Test
    public fun incrementSetSize() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRInfinite(getKey = { pageIndex, _ -> "${key}_${pageIndex}" }, fetcher = {
                delay(100)
                "fetched_$it"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        val (_, _, _, _, _, size, setSize) = stateList.last()
        scope.launch { setSize(size + 1) }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, listOf("fetched_${key}_1"), listOf("fetched_${key}_1", null), listOf("fetched_${key}_1", null), listOf("fetched_${key}_1", "fetched_${key}_2"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 2, 2, 2)
    }
}
