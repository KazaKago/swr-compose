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
public class RevalidateFirstPageOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun withRevalidateFirstPage() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        val fetchCountMap = mutableMapOf<String, Int>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
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
        stateList.map { it.data } shouldBe listOf(null, null, listOf("${key}_1_1"), listOf("${key}_1_1", null), listOf("${key}_1_1", null), listOf("${key}_1_2", "${key}_2_1"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 2, 2, 2)

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.last().apply {
            scope.launch { setSize(size + 2) }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, listOf("${key}_1_1"), listOf("${key}_1_1", null), listOf("${key}_1_1", null), listOf("${key}_1_2", "${key}_2_1"), listOf("${key}_1_2", "${key}_2_1", null, null), listOf("${key}_1_2", "${key}_2_1", null, null), listOf("${key}_1_3", "${key}_2_1", "${key}_3_1", "${key}_4_1"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 2, 2, 2, 4, 4, 4)
    }

    @Test
    public fun noRevalidateFirstPage() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        val fetchCountMap = mutableMapOf<String, Int>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
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
        stateList.map { it.data } shouldBe listOf(null, null, listOf("${key}_1_1"), listOf("${key}_1_1", null), listOf("${key}_1_1", null), listOf("${key}_1_1", "${key}_2_1"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 2, 2, 2)

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.last().apply {
            scope.launch { setSize(size + 2) }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, listOf("${key}_1_1"), listOf("${key}_1_1", null), listOf("${key}_1_1", null), listOf("${key}_1_1", "${key}_2_1"), listOf("${key}_1_1", "${key}_2_1", null, null), listOf("${key}_1_1", "${key}_2_1", null, null), listOf("${key}_1_1", "${key}_2_1", "${key}_3_1", "${key}_4_1"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 2, 2, 2, 4, 4, 4)
    }
}
