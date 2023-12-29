package com.kazakago.swr.compose.trigger

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.config.SWRConfig
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
public class RevalidateOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun trigger_withRevalidate() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        lateinit var cache: MutableState<String?>
        composeTestRule.setContent {
            SWRConfig(options = {
                fetcher = { "fetcher" }
            }) {
                scope = rememberCoroutineScope()
                cache = LocalSWRCache.current.state(key)
                SWRGlobalScope = rememberCoroutineScope()
                stateList += useSWRMutation(key = key, fetcher = { _, arg ->
                    delay(100)
                    arg
                }) {
                    revalidate = true
                }
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
        cache.value shouldBe "fetcher"
    }

    @Test
    public fun trigger_noRevalidate() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        lateinit var cache: MutableState<String?>
        composeTestRule.setContent {
            SWRConfig(options = {
                fetcher = { "fetcher" }
            }) {
                scope = rememberCoroutineScope()
                cache = LocalSWRCache.current.state(key)
                SWRGlobalScope = rememberCoroutineScope()
                stateList += useSWRMutation(key = key, fetcher = { _, arg ->
                    delay(100)
                    arg
                }) {
                    revalidate = false
                }
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
