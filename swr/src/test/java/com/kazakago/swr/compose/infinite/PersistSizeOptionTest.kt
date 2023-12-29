package com.kazakago.swr.compose.infinite

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
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
public class PersistSizeOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun noPersistSize() {
        val key = object {}.javaClass.enclosingMethod?.name
        var getKey: (Int, String?) -> String = { pageIndex, _ -> "${key}_${pageIndex}" }
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRInfinite(getKey = getKey, fetcher = {
                delay(100)
                "fetched"
            }) {
                persistSize = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.last().apply {
            scope.launch { setSize(size + 3) }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, listOf("fetched"), listOf("fetched", null, null, null), listOf("fetched", null, null, null), listOf("fetched", "fetched", "fetched", "fetched"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 4, 4, 4)

        getKey = { pageIndex, _ -> "${key}_${pageIndex}_v2" }
        stateList.last().apply {
            scope.launch { mutate() }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, listOf("fetched"), listOf("fetched", null, null, null), listOf("fetched", null, null, null), listOf("fetched", "fetched", "fetched", "fetched"), null, null, listOf("fetched"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false, false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 4, 4, 4, 1, 1, 1)
    }

    @Test
    public fun withPersistSize() {
        val key = object {}.javaClass.enclosingMethod?.name
        var getKey: (Int, String?) -> String = { pageIndex, _ -> "${key}_${pageIndex}" }
        val stateList = mutableListOf<SWRInfiniteState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRInfinite(getKey = getKey, fetcher = {
                delay(100)
                "fetched"
            }) {
                persistSize = true
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.last().apply {
            scope.launch { setSize(size + 3) }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, listOf("fetched"), listOf("fetched", null, null, null), listOf("fetched", null, null, null), listOf("fetched", "fetched", "fetched", "fetched"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 4, 4, 4)

        getKey = { pageIndex, _ -> "${key}_${pageIndex}_v2" }
        stateList.last().apply {
            scope.launch { mutate() }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, listOf("fetched"), listOf("fetched", null, null, null), listOf("fetched", null, null, null), listOf("fetched", "fetched", "fetched", "fetched"), null, null, listOf("fetched", "fetched", "fetched", "fetched"))
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false, false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false, true, false, false, true, false)
        stateList.map { it.size } shouldBe listOf(1, 1, 1, 4, 4, 4, 4, 4, 4)
    }
}
