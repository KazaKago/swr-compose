package com.kazakago.swr.compose.trigger

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRMutationState
import com.kazakago.swr.compose.useSWRMutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ThrowOnErrorOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun trigger_withThrowOnError() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRMutation(key = key, fetcher = { _, _ ->
                delay(100)
                throw DummyException1
            }) {
                throwOnError = true
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null), stateList.map { it.data })
        assertEquals(listOf(null), stateList.map { it.error })
        assertEquals(listOf(false), stateList.map { it.isMutating })
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
        assertEquals(listOf(null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, DummyException1), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isMutating })
        assertEquals(null, triggerResult)
        assertEquals(DummyException1, mutationError)
    }

    @Test
    fun trigger_noThrowOnError() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWRMutation(key = key, fetcher = { _, _ ->
                delay(100)
                throw DummyException1
            }) {
                throwOnError = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null), stateList.map { it.data })
        assertEquals(listOf(null), stateList.map { it.error })
        assertEquals(listOf(false), stateList.map { it.isMutating })
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
        assertEquals(listOf(null, null, null), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isMutating })
        assertEquals(null, triggerResult)
        assertEquals(null, mutationError)
    }
}
