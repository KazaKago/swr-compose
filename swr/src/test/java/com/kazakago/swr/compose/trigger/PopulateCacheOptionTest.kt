package com.kazakago.swr.compose.trigger

import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRMutationState
import com.kazakago.swr.compose.useSWRMutation
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
public class PopulateCacheOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun trigger_withPopulateCache() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        lateinit var cache: MutableState<String?>
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            cache = LocalSWRCache.current.state(key)
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRMutation(key = key, fetcher = { _, arg ->
                delay(100)
                arg
            }) {
                populateCache = true
            }
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
        cache.value shouldBe "trigger"
    }

    @Test
    public fun trigger_noPopulateCache() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        lateinit var cache: MutableState<String?>
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            cache = LocalSWRCache.current.state(key)
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRMutation(key = key, fetcher = { _, arg ->
                delay(100)
                arg
            }) {
                populateCache = false
            }
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
        cache.value shouldBe null
    }
}