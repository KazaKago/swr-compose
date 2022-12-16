package com.kazakago.swr.compose.mutation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.DummyException2
import com.kazakago.swr.compose.DummyException3
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
public class MutationTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun validate_mutation() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched_1" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
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
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched_1", "fetched_1", "fetched_2")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)
    }

    @Test
    public fun validate_mutationFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { throw DummyException1 }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, DummyException1)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)
    }

    @Test
    public fun validate_mutationWithData() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched_1" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
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
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched_1", "mutated", "fetched_2")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)
    }

    @Test
    public fun validate_mutationWithDataFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched" }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { throw IllegalArgumentException() }
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().mutate(data = {
                    delay(100)
                    throw DummyException1
                })
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)
        mutationError shouldBe DummyException1
    }

    @Test
    public fun validateFailed_mutation() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { "fetched" }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, null, null, "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, DummyException1, DummyException1, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)
    }

    @Test
    public fun validateFailed_mutationFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { throw DummyException2 }
        scope.launch { stateList.last().mutate() }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.error } shouldBe listOf(null, null, DummyException1, DummyException1, DummyException2)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)
    }

    @Test
    public fun validateFailed_mutationWithData() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { "fetched" }
        scope.launch {
            stateList.last().mutate(data = {
                delay(100)
                "mutated"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, null, "mutated", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, DummyException1, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)
    }

    @Test
    public fun validateFailed_mutationWithDataFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { throw DummyException1 }
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                result()
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        result = { throw DummyException2 }
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().mutate(data = {
                    delay(100)
                    throw DummyException3
                })
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, null)
        stateList.map { it.error } shouldBe listOf(null, null, DummyException1)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)
        mutationError shouldBe DummyException3
    }
}
