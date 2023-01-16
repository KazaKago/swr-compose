package com.kazakago.swr.compose.trigger

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRMutationState
import com.kazakago.swr.compose.useSWRMutation
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class UseSWRMutationTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun trigger() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRMutation(key = key, fetcher = { _, arg ->
                delay(100)
                arg
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null)
        stateList.map { it.error } shouldBe listOf(null)
        stateList.map { it.isMutating } shouldBe listOf(false)
        var triggerResult: String? = null
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().trigger("trigger")
            }.onSuccess {
                triggerResult = it
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "trigger")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isMutating } shouldBe listOf(false, true, false)
        triggerResult shouldBe "trigger"
        mutationError shouldBe null
    }

    @Test
    public fun triggerFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRMutation(key = key, fetcher = { _, _ ->
                delay(100)
                throw DummyException1
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null)
        stateList.map { it.error } shouldBe listOf(null)
        stateList.map { it.isMutating } shouldBe listOf(false)
        var triggerResult: String? = null
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().trigger("trigger")
            }.onSuccess {
                triggerResult = it
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, null)
        stateList.map { it.error } shouldBe listOf(null, null, DummyException1)
        stateList.map { it.isMutating } shouldBe listOf(false, true, false)
        triggerResult shouldBe null
        mutationError shouldBe DummyException1
    }

    @Test
    public fun trigger_reset() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRMutation(key = key, fetcher = { _, arg ->
                delay(100)
                arg
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null)
        stateList.map { it.error } shouldBe listOf(null)
        stateList.map { it.isMutating } shouldBe listOf(false)
        var triggerResult: String? = null
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().trigger("trigger")
            }.onSuccess {
                triggerResult = it
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "trigger")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isMutating } shouldBe listOf(false, true, false)
        triggerResult shouldBe "trigger"
        mutationError shouldBe null
        scope.launch {
            runCatching {
                stateList.last().reset()
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "trigger", null)
        stateList.map { it.error } shouldBe listOf(null, null, null, null)
        stateList.map { it.isMutating } shouldBe listOf(false, true, false, false)
    }

    @Test
    public fun triggerFailed_reset() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRMutation(key = key, fetcher = { _, _ ->
                delay(100)
                throw DummyException1
            })
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null)
        stateList.map { it.error } shouldBe listOf(null)
        stateList.map { it.isMutating } shouldBe listOf(false)
        var triggerResult: String? = null
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().trigger("trigger")
            }.onSuccess {
                triggerResult = it
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, null)
        stateList.map { it.error } shouldBe listOf(null, null, DummyException1)
        stateList.map { it.isMutating } shouldBe listOf(false, true, false)
        triggerResult shouldBe null
        mutationError shouldBe DummyException1
        scope.launch {
            runCatching {
                stateList.last().reset()
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, null, null)
        stateList.map { it.error } shouldBe listOf(null, null, DummyException1, null)
        stateList.map { it.isMutating } shouldBe listOf(false, true, false, false)
    }
}
