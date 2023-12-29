package com.kazakago.swr.compose.mutation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
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
public class RollbackOnErrorOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun noRollbackOnError() {
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
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().mutate(data = {
                    delay(100)
                    throw DummyException1
                }) {
                    optimisticData = "optimisticData"
                    rollbackOnError = false
                }
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched_1", "optimisticData")
        stateList.map { it.error } shouldBe listOf(null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, false)
        mutationError shouldBe DummyException1
    }
}
